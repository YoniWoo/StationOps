package stationops.service;

import stationops.dto.IncidentHistoriqueDto;
import stationops.exception.EvaluationInterditeException;
import stationops.modele.*;
import stationops.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EvaluationServiceTest {
    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT =
            LocalDateTime.of(2026, 10, 30, 10, 0);

    private EvaluationRepository evaluationRepository;
    private IncidentService incidentService;
    private EvaluationService evaluationService;

    private Incident incident;

    @BeforeEach
    void preparer()
    {
        evaluationRepository = mock(EvaluationRepository.class);
        incidentService = mock(IncidentService.class);
        Clock horloge = Clock.fixed(MAINTENANT.atZone(ZONE).toInstant(), ZONE);

        evaluationService = new EvaluationService(
                evaluationRepository,
                incidentService,
                horloge);

        Equipement equipement = new Equipement(
                "REP-ASC-001", "Ascenseur accès principal",
                TypeEquipement.ASCENSEUR, new Station("République"));
        Utilisateur agent = new Utilisateur(
                "agent@stationops.test", "hash-de-test",
                "Agent", Profil.AGENT_TERRAIN);
        incident = new Incident(
                "Ascenseur immobilisé", equipement, agent, MAINTENANT.minusDays(2));
    }

    private void resoudreAvecPrestataire()
    {
        incident.affecterPrestataire(
                new Prestataire("Mobilité Maintenance", "contact@mm.test", null));
        incident.resoudre(MAINTENANT.minusDays(1));
    }

    @Test
    void evaluerUnIncidentResoluEnregistreLEvaluation()
    {
        resoudreAvecPrestataire();
        when(incidentService.trouverIncident(1)).thenReturn(incident);
        when(evaluationRepository.existsByIncidentId(1)).thenReturn(false);

        Evaluation evaluation = evaluationService.evaluerPrestataire(1, 5, "Rapide");

        assertEquals(5, evaluation.getNote());
        assertEquals(MAINTENANT, evaluation.getDateEvaluation());
        verify(evaluationRepository).save(evaluation);
    }

    @Test
    void evaluerDeuxFoisLeMemeIncidentEstRefuse()
    {
        resoudreAvecPrestataire();
        when(incidentService.trouverIncident(1)).thenReturn(incident);
        when(evaluationRepository.existsByIncidentId(1)).thenReturn(true);

        assertThrows(
                EvaluationInterditeException.class,
                () -> evaluationService.evaluerPrestataire(1, 4, null)
        );
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void evaluerUnIncidentNonResoluEstRefuse()
    {
        when(incidentService.trouverIncident(1)).thenReturn(incident);
        when(evaluationRepository.existsByIncidentId(1)).thenReturn(false);

        assertThrows(
                EvaluationInterditeException.class,
                () -> evaluationService.evaluerPrestataire(1, 4, null)
        );
    }

    @Test
    void uneNoteEnDehorsDe1a5EstRefusee()
    {
        resoudreAvecPrestataire();
        when(incidentService.trouverIncident(1)).thenReturn(incident);
        when(evaluationRepository.existsByIncidentId(1)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> evaluationService.evaluerPrestataire(1, 6, null)
        );
    }

    @Test
    void historiqueDonneLaNoteQuandLIncidentEstEvalue()
    {
        resoudreAvecPrestataire();
        Evaluation evaluation = new Evaluation(incident, 4, null, MAINTENANT);
        when(incidentService.listerHistorique()).thenReturn(List.of(incident));
        when(evaluationRepository.findByIncidentId(any())).thenReturn(Optional.of(evaluation));

        List<IncidentHistoriqueDto> historique = evaluationService.listerHistoriqueAvecNotes();

        assertEquals(1, historique.size());
        assertEquals(4, historique.get(0).note());
        assertEquals("Mobilité Maintenance", historique.get(0).prestataireNom());
    }

    @Test
    void historiqueSansEvaluationLaisseLaNoteVide()
    {
        resoudreAvecPrestataire();
        when(incidentService.listerHistorique()).thenReturn(List.of(incident));
        when(evaluationRepository.findByIncidentId(any())).thenReturn(Optional.empty());

        List<IncidentHistoriqueDto> historique = evaluationService.listerHistoriqueAvecNotes();

        assertNull(historique.get(0).note());
    }
}
