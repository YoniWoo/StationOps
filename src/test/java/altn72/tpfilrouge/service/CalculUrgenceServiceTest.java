package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculUrgenceServiceTest {

    private CalculUrgenceService service;
    private LocalDateTime maintenant;
    private Equipement equipement;
    private Utilisateur agent;

    @BeforeEach
    void preparer()
    {
        Clock horloge = Clock.fixed(
                Instant.parse("2026-11-01T11:00:00Z"),
                ZoneId.of("Europe/Paris"));

        maintenant = LocalDateTime.now(horloge);
        service = new CalculUrgenceService(horloge);

        Station station = new Station("Nation");

        equipement = new Equipement(
                "TEST-ASC-001",
                "Ascenseur de test",
                TypeEquipement.ASCENSEUR,
                station);

        agent = new Utilisateur(
                "agent@stationops.test",
                "hash-de-test",
                "Agent",
                Profil.AGENT_TERRAIN);
    }

    @Test
    void incidentDe47HeuresEstVert()
    {
        Incident incident = creerIncident(maintenant.minusHours(47));

        assertEquals(NiveauUrgence.VERT, service.calculer(incident));
    }

    @Test
    void incidentDe48HeuresEstOrange()
    {
        Incident incident = creerIncident(maintenant.minusHours(48));

        assertEquals(NiveauUrgence.ORANGE, service.calculer(incident));
    }

    @Test
    void incidentJusteAvant72HeuresEstOrange()
    {
        Incident incident = creerIncident(
                maintenant.minusHours(72).plusSeconds(1));

        assertEquals(NiveauUrgence.ORANGE, service.calculer(incident));
    }

    @Test
    void incidentDe72HeuresEstRouge()
    {
        Incident incident = creerIncident(maintenant.minusHours(72));

        assertEquals(NiveauUrgence.ROUGE, service.calculer(incident));
    }

    @Test
    void incidentAffecteConserveSaPresentationApres72Heures()
    {
        Incident incident = creerIncident(maintenant.minusHours(80));

        incident.affecterPrestataire(new Prestataire(
                "Maintenance",
                "contact@maintenance.test",
                null));

        assertEquals(NiveauUrgence.AFFECTE, service.calculer(incident));
    }

    @Test
    void incidentResoluEstIdentifieCommeResolu()
    {
        Incident incident = creerIncident(maintenant.minusHours(80));
        incident.resoudre(maintenant.minusHours(1));

        assertEquals(NiveauUrgence.RESOLU, service.calculer(incident));
    }

    private Incident creerIncident(LocalDateTime ouverture)
    {
        return new Incident(
                "Panne de test",
                equipement,
                agent,
                ouverture);
    }
}