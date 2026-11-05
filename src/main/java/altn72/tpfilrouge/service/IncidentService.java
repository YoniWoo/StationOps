package altn72.tpfilrouge.service;

import altn72.tpfilrouge.dto.IncidentDetailDto;
import altn72.tpfilrouge.exception.AffectationAConfirmerException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.StatutIncident;
import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

// Service qui gere le cycle de vie des incidents
// les regles sur un seul incident sont dans le modele, ici c'est ce qui touche plusieurs entitées
@Service
public class IncidentService
{
    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PrestataireRepository prestataireRepository;

    // pour la couleur de la page detail, les seuils 48 h / 72 h restent a un seul endroit
    private final CalculUrgenceService calculUrgenceService;
    private final SurveillancePrestataireService surveillanceService;

    // horloge injectée pour pouvoir fixer l'heure dans les tests
    private final Clock horloge;

    public IncidentService(
            IncidentRepository incidentRepository,
            EquipementRepository equipementRepository,
            UtilisateurRepository utilisateurRepository,
            PrestataireRepository prestataireRepository,
            CalculUrgenceService calculUrgenceService,
            SurveillancePrestataireService surveillanceService,
            Clock horloge)
    {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.prestataireRepository = prestataireRepository;
        this.calculUrgenceService = calculUrgenceService;
        this.surveillanceService = surveillanceService;
        this.horloge = horloge;
    }

    public Incident trouverIncident(Integer incidentId)
    {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> incidentIntrouvable(incidentId));
    }

    // F5 : version qui charge aussi l'equipement, la station et le prestataire pour la page
    public Incident trouverDetailIncident(Integer incidentId)
    {
        return incidentRepository.trouverDetail(incidentId)
                .orElseThrow(() -> incidentIntrouvable(incidentId));
    }

    // F5 : duree depuis l'ouverture, ou jusqu'a la resolution si l'incident est résolu
    // calculée a chaque affichage avec l'horloge, jamais stockée en base
    public Duration calculerDureeOuverture(Incident incident)
    {
        LocalDateTime fin = LocalDateTime.now(horloge);
        if (incident.estResolu())
        {
            fin = incident.getResoluLe();
        }
        return Duration.between(incident.getOuvertLe(), fin);
    }

    // F5 : tout ce qu'il faut pour la page detail, la durée est recalculée a chaque affichage
    public IncidentDetailDto consulterDetail(Integer incidentId)
    {
        Incident incident = trouverDetailIncident(incidentId);
        Duration duree = calculerDureeOuverture(incident);

        String prestataireNom = null;
        if (incident.getPrestataire() != null)
        {
            prestataireNom = incident.getPrestataire().getNom();
        }

        return new IncidentDetailDto(
                incident.getId(),
                incident.getDescription(),
                incident.getStatut(),
                incident.getOuvertLe(),
                incident.getResoluLe(),
                incident.getEquipement().getId(),
                incident.getEquipement().getReference(),
                incident.getEquipement().getLibelle(),
                incident.getEquipement().getStation().getNom(),
                prestataireNom,
                duree.toHours(),
                duree.toMinutesPart(),
                calculUrgenceService.calculer(incident));
    }

    // F4 : l'incident est cree sans prestataire (RG1) et l'equipement passe hors service
    // @Transactional : les 2 changements sont enregistrés ensemble ou pas du tout (RG10)
    @Transactional
    public Incident declarerIncident(
            Integer equipementId,
            String description,
            Integer declarantId)
    {
        Equipement equipement = trouverEquipement(equipementId);
        Utilisateur declarant = trouverUtilisateur(declarantId);

        Incident incident = new Incident(
                description,
                equipement,
                declarant,
                LocalDateTime.now(horloge));

        equipement.mettreHorsService();
        incidentRepository.save(incident);
        return incident;
    }

    // F6 : seulement la description est modifiable, et tant que l'incident est pas résolu
    // pas besoin de save, hibernate voit le changement a la fin de la transaction
    @Transactional
    public void modifierDescription(Integer incidentId, String description)
    {
        Incident incident = trouverIncident(incidentId);
        incident.modifierDescription(description);
    }

    // F7 : marche aussi pour reaffecter, le modele refuse si l'incident est deja résolu (RG5)
    // PS5 : si le prestataire est sous surveillance il faut confirmation=true,
    // sinon on leve l'exception AVANT de toucher a l'incident donc rien est enregistré (RG10)
    @Transactional
    public void affecterPrestataire(Integer incidentId, Integer prestataireId, boolean confirmation)
    {
        Incident incident = trouverIncident(incidentId);
        Prestataire prestataire = trouverPrestataire(prestataireId);

        Double moyenne = surveillanceService.calculerMoyenne(prestataireId);
        if (surveillanceService.estSousSurveillance(moyenne) && !confirmation)
        {
            throw new AffectationAConfirmerException(
                    prestataireId,
                    prestataire.getNom() + " est sous surveillance (moyenne de "
                            + String.format(Locale.FRANCE, "%.1f", moyenne)
                            + " sur ses " + SurveillancePrestataireService.TAILLE_FENETRE
                            + " dernières évaluations). Confirmez l'affectation pour continuer.");
        }

        incident.affecterPrestataire(prestataire);
    }

    // F8 : on enregistre l'heure de resolution, puis l'equipement repasse en service
    // seulement si il a plus aucun incident actif (il peut en avoir plusieurs en meme temps)
    // @Transactional : si la remise en service plante, l'incident reste non résolu (RG10)
    @Transactional
    public void resoudreIncident(Integer incidentId)
    {
        Incident incident = trouverIncident(incidentId);
        incident.resoudre(LocalDateTime.now(horloge));

        Equipement equipement = incident.getEquipement();
        long incidentsActifs = incidentRepository
                .compterIncidentsActifsParEquipement(equipement.getId());

        if (incidentsActifs == 0)
        {
            equipement.remettreEnService();
        }
    }

    // F9 : du plus récent au plus ancien par date de resolution (RG7)
    public List<Incident> listerHistorique()
    {
        return incidentRepository.trouverHistorique(StatutIncident.RESOLU);
    }

    private RessourceIntrouvableException incidentIntrouvable(Integer incidentId)
    {
        return new RessourceIntrouvableException(
                "Aucun incident ne correspond à l'identifiant " + incidentId);
    }

    private Equipement trouverEquipement(Integer equipementId)
    {
        return equipementRepository.findById(equipementId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun équipement ne correspond à l'identifiant " + equipementId));
    }

    private Utilisateur trouverUtilisateur(Integer utilisateurId)
    {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun utilisateur ne correspond à l'identifiant " + utilisateurId));
    }

    private Prestataire trouverPrestataire(Integer prestataireId)
    {
        return prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun prestataire ne correspond à l'identifiant " + prestataireId));
    }
}
