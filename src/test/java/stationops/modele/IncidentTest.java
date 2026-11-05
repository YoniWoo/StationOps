package stationops.modele;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class IncidentTest {
    private static final LocalDateTime OUVERTURE =
            LocalDateTime.of(2026, 10, 20, 8, 0);

    private final Equipement equipement = new Equipement(
            "NAT-POR-001", "Portique entrée nord",
            TypeEquipement.PORTIQUE, new Station("Nation"));
    private final Utilisateur agent = new Utilisateur(
            "agent@stationops.test", "hash-de-test",
            "Agent", Profil.AGENT_TERRAIN);

    @Test
    void uneDescriptionTropLongueEstRefuseeAvecLeBonMessage()
    {
        String description = "a".repeat(2001);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Incident(description, equipement, agent, OUVERTURE));

        assertEquals("La description ne doit pas dépasser 2000 caractères", exception.getMessage());
    }

    @Test
    void lesEspacesAutourDeLaDescriptionSontRetires()
    {
        Incident incident = new Incident("  Portique bloqué  ", equipement, agent, OUVERTURE);

        assertEquals("Portique bloqué", incident.getDescription());
    }
}
