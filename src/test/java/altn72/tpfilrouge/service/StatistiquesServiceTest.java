package altn72.tpfilrouge.service;

import altn72.tpfilrouge.dto.DelaiResolutionDto;
import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatistiquesServiceTest {

    private static final LocalDateTime OUVERTURE =
            LocalDateTime.of(2026, 11, 1, 8, 0);

    private IncidentRepository incidentRepository;
    private StatistiquesService statistiquesService;

    @BeforeEach
    void preparer()
    {
        incidentRepository = mock(IncidentRepository.class);
        statistiquesService = new StatistiquesService(incidentRepository);
    }

    @Test
    void moyenneEstCalculeeParTypeAvecLeNombreDIncidents()
    {
        when(incidentRepository.trouverHistorique(StatutIncident.RESOLU))
                .thenReturn(List.of(
                        creerIncidentResolu(TypeEquipement.ASCENSEUR, 60),
                        creerIncidentResolu(TypeEquipement.ASCENSEUR, 120),
                        creerIncidentResolu(TypeEquipement.PORTIQUE, 240)));

        var resultats = statistiquesService.calculerDelaisMoyensParType();

        assertEquals(2, resultats.size());

        DelaiResolutionDto ascenseurs = resultats.stream()
                .filter(resultat ->
                        resultat.type() == TypeEquipement.ASCENSEUR)
                .findFirst()
                .orElseThrow();

        assertEquals(1.5, ascenseurs.moyenneHeures(), 0.001);
        assertEquals(2L, ascenseurs.nombreIncidents());

        DelaiResolutionDto portiques = resultats.stream()
                .filter(resultat ->
                        resultat.type() == TypeEquipement.PORTIQUE)
                .findFirst()
                .orElseThrow();

        assertEquals(4.0, portiques.moyenneHeures(), 0.001);
        assertEquals(1L, portiques.nombreIncidents());
    }

    @Test
    void aucunIncidentResoluProduitUneListeVide()
    {
        when(incidentRepository.trouverHistorique(StatutIncident.RESOLU))
                .thenReturn(List.of());

        assertTrue(
                statistiquesService.calculerDelaisMoyensParType().isEmpty());
    }

    private Incident creerIncidentResolu(
            TypeEquipement type,
            long dureeMinutes)
    {
        Equipement equipement = new Equipement(
                "TEST-" + type.name(),
                "Équipement de test",
                type,
                new Station("Station de test"));

        Utilisateur agent = new Utilisateur(
                "agent@stationops.test",
                "hash-de-test",
                "Agent",
                Profil.AGENT_TERRAIN);

        Incident incident = new Incident(
                "Panne de test",
                equipement,
                agent,
                OUVERTURE);

        incident.resoudre(OUVERTURE.plusMinutes(dureeMinutes));

        return incident;
    }
}