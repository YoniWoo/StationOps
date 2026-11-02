package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// test avec une vraie base H2 pour verifier les requetes JPQL, pas juste des mocks
@DataJpaTest
@ActiveProfiles("test")
public class IncidentRepositoryTest {
    private static final LocalDateTime DEBUT =
            LocalDateTime.of(2026, 10, 20, 8, 0);

    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private EquipementRepository equipementRepository;
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private PrestataireRepository prestataireRepository;

    private Equipement equipement;
    private Utilisateur agent;
    private Prestataire prestataire;

    @BeforeEach
    void preparer()
    {
        Station station = stationRepository.save(new Station("Nation"));
        equipement = equipementRepository.save(new Equipement(
                "NAT-POR-001", "Portique entrée nord",
                TypeEquipement.PORTIQUE, station));
        agent = utilisateurRepository.save(new Utilisateur(
                "agent@stationops.test", "hash-de-test",
                "Agent", Profil.AGENT_TERRAIN));
        prestataire = prestataireRepository.save(new Prestataire(
                "Mobilité Maintenance", "contact@mm.test", null));
    }

    private Incident creerIncidentResolu(String description, LocalDateTime resoluLe)
    {
        Incident incident = new Incident(description, equipement, agent, DEBUT);
        incident.affecterPrestataire(prestataire);
        incident.resoudre(resoluLe);
        return incidentRepository.save(incident);
    }

    @Test
    void historiqueEstTrieDuPlusRecentAuPlusAncien()
    {
        Incident ancien = creerIncidentResolu("Portique bloqué", DEBUT.plusDays(1));
        Incident recent = creerIncidentResolu("Lecteur en panne", DEBUT.plusDays(3));
        Incident milieu = creerIncidentResolu("Porte coincée", DEBUT.plusDays(2));

        List<Incident> historique = incidentRepository.trouverHistorique(StatutIncident.RESOLU);

        assertEquals(List.of(recent, milieu, ancien), historique);
    }

    @Test
    void historiqueNeContientPasLesIncidentsActifs()
    {
        Incident resolu = creerIncidentResolu("Portique bloqué", DEBUT.plusDays(1));
        incidentRepository.save(new Incident("Écran noir", equipement, agent, DEBUT));

        List<Incident> historique = incidentRepository.trouverHistorique(StatutIncident.RESOLU);

        assertEquals(List.of(resolu), historique);
    }

    @Test
    void detailChargeLaStationEtLePrestataire()
    {
        Incident incident = creerIncidentResolu("Portique bloqué", DEBUT.plusDays(1));

        Optional<Incident> detail = incidentRepository.trouverDetail(incident.getId());

        assertTrue(detail.isPresent());
        assertEquals("Nation", detail.get().getEquipement().getStation().getNom());
        assertEquals("Mobilité Maintenance", detail.get().getPrestataire().getNom());
    }

    @Test
    void supervisionExclutLesIncidentsResolus()
    {
        Incident nonAffecte = incidentRepository.save(
                new Incident(
                        "Ascenseur bloqué",
                        equipement,
                        agent,
                        DEBUT));

        Incident affecte = new Incident(
                "Bruit inhabituel",
                equipement,
                agent,
                DEBUT.plusHours(1));

        affecte.affecterPrestataire(prestataire);
        affecte = incidentRepository.save(affecte);

        creerIncidentResolu(
                "Ancienne panne réparée",
                DEBUT.plusDays(1));

        List<Incident> incidentsActifs = incidentRepository
                .trouverIncidentsActifs(StatutIncident.RESOLU);

        assertEquals(
                List.of(nonAffecte, affecte),
                incidentsActifs);
    }
    @Test
    void comptageNatifCompteSeulementLesIncidentsActifsDeLEquipement()
    {
        incidentRepository.save(new Incident(
                "Première panne active",
                equipement,
                agent,
                DEBUT));

        incidentRepository.save(new Incident(
                "Deuxième panne active",
                equipement,
                agent,
                DEBUT.plusHours(1)));

        creerIncidentResolu(
                "Ancienne panne réparée",
                DEBUT.plusDays(1));

        Equipement autreEquipement = equipementRepository.save(
                new Equipement(
                        "TEST-AUTRE-001",
                        "Autre équipement",
                        TypeEquipement.ASCENSEUR,
                        equipement.getStation()));

        incidentRepository.save(new Incident(
                "Panne sur un autre équipement",
                autreEquipement,
                agent,
                DEBUT));

        assertEquals(
                2L,
                incidentRepository.compterIncidentsActifsParEquipement(
                        equipement.getId()));
    }
}
