package altn72.tpfilrouge.configuration;

import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@Profile("demo")
@Order(20)
public class InitialisationDonneesDemo implements CommandLineRunner {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitialisationDonneesDemo.class);

    private final StationRepository stationRepository;
    private final EquipementRepository equipementRepository;
    private final PrestataireRepository prestataireRepository;
    private final IncidentRepository incidentRepository;
    private final EvaluationRepository evaluationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final Clock horloge;

    public InitialisationDonneesDemo(
            StationRepository stationRepository,
            EquipementRepository equipementRepository,
            PrestataireRepository prestataireRepository,
            IncidentRepository incidentRepository,
            EvaluationRepository evaluationRepository,
            UtilisateurRepository utilisateurRepository,
            Clock horloge) {

        this.stationRepository = stationRepository;
        this.equipementRepository = equipementRepository;
        this.prestataireRepository = prestataireRepository;
        this.incidentRepository = incidentRepository;
        this.evaluationRepository = evaluationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.horloge = horloge;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (contientDesDonneesMetier()) {
            LOGGER.info(
                    "Initialisation métier ignorée : "
                            + "des données existent déjà");
            return;
        }

        Utilisateur declarant = utilisateurRepository
                .findByEmail("agent@stationops.test")
                .orElseThrow(() -> new IllegalStateException(
                        "Le compte agent doit être créé "
                                + "avant les données de démonstration"));

        LocalDateTime maintenant = LocalDateTime.now(horloge);

        Station republique = stationRepository.save(
                new Station("République"));

        Station nation = stationRepository.save(
                new Station("Nation"));

        Station bastille = stationRepository.save(
                new Station("Bastille"));

        Prestataire prestataire = prestataireRepository.save(
                new Prestataire(
                        "Mobilité Maintenance",
                        "contact@mobilite-maintenance.test",
                        "0100000000"
                ));

        Equipement ascenseur = creerEquipement(
                "REP-ASC-001", "Ascenseur accès principal",
                TypeEquipement.ASCENSEUR, republique);

        Equipement escalier = creerEquipement(
                "REP-ESC-001", "Escalier mécanique quai",
                TypeEquipement.ESCALIER_MECANIQUE, republique);

        Equipement portique = creerEquipement(
                "NAT-POR-001", "Portique entrée nord",
                TypeEquipement.PORTIQUE, nation);

        Equipement distributeur = creerEquipement(
                "NAT-DIS-001", "Distributeur de titres",
                TypeEquipement.DISTRIBUTEUR_TITRES, nation);

        Equipement ecran = creerEquipement(
                "BAS-ECR-001", "Écran information voyageurs",
                TypeEquipement.ECRAN_INFORMATION, bastille);

        Equipement ascenseurSecondaire = creerEquipement(
                "BAS-ASC-001", "Ascenseur accès secondaire",
                TypeEquipement.ASCENSEUR, bastille);

        creerIncident(
                "Ascenseur immobilisé",
                ascenseur, declarant, maintenant.minusHours(47));

        creerIncident(
                "Escalier mécanique arrêté",
                escalier, declarant, maintenant.minusHours(48));

        creerIncident(
                "Portique bloqué",
                portique, declarant, maintenant.minusHours(71));

        creerIncident(
                "Paiement indisponible",
                distributeur, declarant, maintenant.minusHours(72));

        Incident incidentAffecte = creerIncident(
                "Écran sans affichage",
                ecran, declarant, maintenant.minusHours(80));

        incidentAffecte.affecterPrestataire(prestataire);

        Incident incidentResolu = creerIncident(
                "Porte de l’ascenseur bloquée",
                ascenseurSecondaire,
                declarant,
                maintenant.minusDays(5));

        incidentResolu.affecterPrestataire(prestataire);
        incidentResolu.resoudre(maintenant.minusDays(2));
        ascenseurSecondaire.remettreEnService();

        evaluationRepository.save(new Evaluation(
                incidentResolu,
                4,
                "Intervention efficace.",
                maintenant.minusDays(1)
        ));

        LOGGER.info("Données métier de démonstration créées");
    }

    private boolean contientDesDonneesMetier() {
        return stationRepository.count() > 0
                || equipementRepository.count() > 0
                || prestataireRepository.count() > 0
                || incidentRepository.count() > 0
                || evaluationRepository.count() > 0;
    }

    private Equipement creerEquipement(
            String reference,
            String libelle,
            TypeEquipement type,
            Station station) {

        return equipementRepository.save(
                new Equipement(reference, libelle, type, station));
    }

    private Incident creerIncident(
            String description,
            Equipement equipement,
            Utilisateur declarant,
            LocalDateTime ouvertLe) {

        equipement.mettreHorsService();

        return incidentRepository.save(new Incident(
                description,
                equipement,
                declarant,
                ouvertLe
        ));
    }
}
