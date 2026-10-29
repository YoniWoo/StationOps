package altn72.tpfilrouge.modele;

import altn72.tpfilrouge.exception.EvaluationInterditeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTest {
    private static final LocalDateTime OUVERTURE =
            LocalDateTime.of(2026, 10, 20, 8, 0);
    private static final LocalDateTime RESOLUTION =
            LocalDateTime.of(2026, 10, 22, 8, 0);

    private Incident incident;

    @BeforeEach
    void preparer()
    {
        Station station = new Station("Nation");
        Equipement equipement = new Equipement(
                "NAT-POR-001", "Portique entrée nord",
                TypeEquipement.PORTIQUE, station);
        Utilisateur declarant = new Utilisateur(
                "agent@stationops.test", "hash-de-test",
                "Agent", Profil.AGENT_TERRAIN);
        Prestataire prestataire = new Prestataire(
                "Mobilité Maintenance", "contact@mobilite-maintenance.test",
                "0100000000");

        incident = new Incident(
                "Portique bloqué", equipement, declarant, OUVERTURE);
        incident.affecterPrestataire(prestataire);
        incident.resoudre(RESOLUTION);
    }

    @Test
    void evaluationApresLaResolutionEstAcceptee()
    {
        Evaluation evaluation = new Evaluation(
                incident, 4, "Bien", RESOLUTION.plusHours(1));

        assertEquals(4, evaluation.getNote());
    }

    // avant le bug comparait a l'ouverture donc cette date passait alors quelle est avant la resolution
    @Test
    void evaluationEntreOuvertureEtResolutionEstRefusee()
    {
        LocalDateTime avantResolution = OUVERTURE.plusHours(1);

        assertThrows(
                EvaluationInterditeException.class,
                () -> new Evaluation(incident, 4, null, avantResolution)
        );
    }
}
