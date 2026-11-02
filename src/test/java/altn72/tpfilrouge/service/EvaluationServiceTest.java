package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.EvaluationInterditeException;
import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
}
