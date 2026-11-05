package stationops.api;

import stationops.modele.*;
import stationops.repository.EquipementRepository;
import stationops.repository.IncidentRepository;
import stationops.repository.StationRepository;
import stationops.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApiHttpTest {

    @Autowired
    private WebApplicationContext contexte;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private Clock horloge;

    private MockMvc mockMvc;
    private Station station;
    private Equipement equipement;
    private Incident incident;

    @BeforeEach
    void preparer()
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(contexte)
                .apply(springSecurity())
                .build();

        station = stationRepository.save(
                new Station("Station API"));

        equipement = equipementRepository.save(new Equipement(
                "API-ASC-001",
                "Ascenseur API",
                TypeEquipement.ASCENSEUR,
                station));

        Utilisateur agent = utilisateurRepository.save(new Utilisateur(
                "agent-api@stationops.test",
                "hash-non-utilise-dans-ces-tests",
                "Agent API",
                Profil.AGENT_TERRAIN));

        incident = incidentRepository.save(new Incident(
                "Ascenseur bloqué",
                equipement,
                agent,
                LocalDateTime.now(horloge).minusHours(2)));

        equipement.mettreHorsService();
    }

    @Test
    void consultationAnonymeRedirigeVersLaConnexion() throws Exception
    {
        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void agentPeutConsulterLesStations() throws Exception
    {
        mockMvc.perform(get("/api/stations")
                        .with(user("agent").roles("AGENT_TERRAIN")))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[?(@.id == "
                        + station.getId() + ")].nom")
                        .value("Station API"));
    }

    @Test
    void agentPeutConsulterLesEquipements() throws Exception
    {
        mockMvc.perform(get(
                        "/api/stations/{id}/equipements",
                        station.getId())
                        .with(user("agent").roles("AGENT_TERRAIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(equipement.getId()))
                .andExpect(jsonPath("$[0].reference")
                        .value("API-ASC-001"));
    }

    @Test
    void agentPeutConsulterUneFicheEquipement() throws Exception
    {
        mockMvc.perform(get("/api/equipements/{id}", equipement.getId())
                        .with(user("agent").roles("AGENT_TERRAIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(equipement.getId()))
                .andExpect(jsonPath("$.stationNom").value("Station API"))
                .andExpect(jsonPath("$.incidents[0].id")
                        .value(incident.getId()));
    }

    @Test
    void equipementAbsentRetourne404EnJson() throws Exception
    {
        mockMvc.perform(get("/api/equipements/{id}", -1)
                        .with(user("agent").roles("AGENT_TERRAIN")))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statut").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void identifiantInvalideRetourne400EnJson() throws Exception
    {
        mockMvc.perform(get("/api/equipements/abc")
                        .with(user("agent").roles("AGENT_TERRAIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statut").value(400));
    }

    @Test
    void agentNePeutPasResoudreUnIncident() throws Exception
    {
        mockMvc.perform(post(
                        "/api/incidents/{id}/resolution",
                        incident.getId())
                        .with(user("agent").roles("AGENT_TERRAIN"))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statut").value(403));

        assertEquals(
                StatutIncident.NON_AFFECTE,
                incidentRepository.findById(incident.getId())
                        .orElseThrow().getStatut());
    }

    @Test
    void resolutionSansCsrfEstRefusee() throws Exception
    {
        mockMvc.perform(post(
                        "/api/incidents/{id}/resolution",
                        incident.getId())
                        .with(user("responsable")
                                .roles("RESPONSABLE_EXPLOITATION")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statut").value(403));

        assertEquals(
                StatutIncident.NON_AFFECTE,
                incidentRepository.findById(incident.getId())
                        .orElseThrow().getStatut());
    }

    @Test
    void resolutionAnonymeAvecCsrfRedirigeVersLaConnexion() throws Exception
    {
        mockMvc.perform(post(
                        "/api/incidents/{id}/resolution",
                        incident.getId())
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void responsablePeutResoudreUnIncident() throws Exception
    {
        mockMvc.perform(post(
                        "/api/incidents/{id}/resolution",
                        incident.getId())
                        .with(user("responsable")
                                .roles("RESPONSABLE_EXPLOITATION"))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Incident resultat = incidentRepository
                .findById(incident.getId()).orElseThrow();

        assertEquals(StatutIncident.RESOLU, resultat.getStatut());
        assertNotNull(resultat.getResoluLe());

        assertEquals(
                EtatEquipement.EN_SERVICE,
                equipementRepository.findById(equipement.getId())
                        .orElseThrow().getEtat());
    }

    @Test
    void resolutionIncidentAbsentRetourne404() throws Exception
    {
        mockMvc.perform(post("/api/incidents/{id}/resolution", -1)
                        .with(user("responsable")
                                .roles("RESPONSABLE_EXPLOITATION"))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statut").value(404));
    }

    @Test
    void deuxiemeResolutionRetourne409() throws Exception
    {
        mockMvc.perform(post(
                        "/api/incidents/{id}/resolution",
                        incident.getId())
                        .with(user("responsable")
                                .roles("RESPONSABLE_EXPLOITATION"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(
                        "/api/incidents/{id}/resolution",
                        incident.getId())
                        .with(user("responsable")
                                .roles("RESPONSABLE_EXPLOITATION"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statut").value(409));
    }
}