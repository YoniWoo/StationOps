package stationops.repository;

import stationops.modele.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// PS1 : on verifie l'ordre et le filtre de la requete sur une vraie base H2
@DataJpaTest
@ActiveProfiles("test")
public class EvaluationRepositoryTest {
    private static final LocalDateTime DEBUT =
            LocalDateTime.of(2026, 10, 1, 8, 0);

    @Autowired
    private EvaluationRepository evaluationRepository;
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
    private Prestataire surveille;
    private Prestataire autre;

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
        surveille = prestataireRepository.save(new Prestataire("Dupont Réseaux", "contact@dupont.test", null));
        autre = prestataireRepository.save(new Prestataire("Mobilité Maintenance", "contact@mm.test", null));
    }

    // un incident résolu par ce prestataire puis evalué a la date donnée
    private void evaluer(Prestataire prestataire, int note, LocalDateTime dateEvaluation)
    {
        Incident incident = new Incident("Panne", equipement, agent, DEBUT);
        incident.affecterPrestataire(prestataire);
        incident.resoudre(DEBUT.plusHours(1));
        incidentRepository.save(incident);
        evaluationRepository.save(new Evaluation(incident, note, null, dateEvaluation));
    }

    @Test
    void lesNotesSontDeLaPlusRecenteALaPlusAncienne()
    {
        evaluer(surveille, 1, DEBUT.plusDays(1));
        evaluer(surveille, 2, DEBUT.plusDays(3));
        evaluer(surveille, 3, DEBUT.plusDays(2));

        List<Integer> notes = evaluationRepository.trouverDernieresNotes(
                surveille.getId(), PageRequest.of(0, 5));

        assertEquals(List.of(2, 3, 1), notes);
    }

    // meme date d'evaluation : c'est l'id le plus grand (la derniere saisie) qui passe en premier
    @Test
    void aDateEgaleLaDerniereSaisieEstPrisePremiere()
    {
        evaluer(surveille, 4, DEBUT.plusDays(1));
        evaluer(surveille, 2, DEBUT.plusDays(1));

        List<Integer> notes = evaluationRepository.trouverDernieresNotes(
                surveille.getId(), PageRequest.of(0, 5));

        assertEquals(List.of(2, 4), notes);
    }

    @Test
    void seulementLesNotesDuPrestataireEtPasPlusQueLaFenetre()
    {
        for (int jour = 1; jour <= 6; jour++)
        {
            evaluer(surveille, 3, DEBUT.plusDays(jour));
        }
        evaluer(autre, 5, DEBUT.plusDays(10));

        List<Integer> notes = evaluationRepository.trouverDernieresNotes(
                surveille.getId(), PageRequest.of(0, 5));

        assertEquals(List.of(3, 3, 3, 3, 3), notes);
    }
}
