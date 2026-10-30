package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

// Service qui gere le cycle de vie des incidents
// les regles sur un seul incident sont dans le modele, ici c'est ce qui touche plusieurs entitées
@Service
public class IncidentService
{
    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final UtilisateurRepository utilisateurRepository;

    // horloge injectée pour pouvoir fixer l'heure dans les tests
    private final Clock horloge;

    public IncidentService(
            IncidentRepository incidentRepository,
            EquipementRepository equipementRepository,
            UtilisateurRepository utilisateurRepository,
            Clock horloge)
    {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.horloge = horloge;
    }

    public Incident trouverIncident(Integer incidentId)
    {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun incident ne correspond à l'identifiant " + incidentId));
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
}
