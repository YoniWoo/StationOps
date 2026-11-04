package altn72.tpfilrouge.api;

import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import altn72.tpfilrouge.securite.UtilisateurConnecte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// F12 : consultation des incidents par l'API (liste des actifs et detail)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IncidentApiConsultationTest {

    @Autowired
    private WebApplicationContext contexte;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private EquipementRepository equipementRepository;
    @Autowired
    private IncidentRepository incidentRepository;

    private MockMvc mockMvc;
    private UtilisateurConnecte agent;
    private Incident actif;
    private Incident resolu;

    @BeforeEach
    void preparer()
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(contexte)
                .apply(springSecurity())
                .build();

        Utilisateur compte = utilisateurRepository.save(new Utilisateur(
                "agent-api@stationops.test", "hash-de-test",
                "Agent", Profil.AGENT_TERRAIN));
        agent = new UtilisateurConnecte(compte);

        Station station = stationRepository.save(new Station("Nation"));
        Equipement equipement = equipementRepository.save(new Equipement(
                "API-POR-001", "Portique entrée nord",
                TypeEquipement.PORTIQUE, station));

        actif = incidentRepository.save(new Incident(
                "Portique bloqué", equipement, compte,
                LocalDateTime.now().minusHours(5)));

        resolu = new Incident(
                "Lecteur en panne", equipement, compte,
                LocalDateTime.now().minusHours(30));
        resolu.resoudre(LocalDateTime.now().minusHours(2));
        resolu = incidentRepository.save(resolu);
    }

    @Test
    void laListeContientLesIncidentsActifsSeulement() throws Exception
    {
        mockMvc.perform(get("/api/incidents").with(user(agent)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[?(@.id == " + actif.getId() + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + resolu.getId() + ")]").doesNotExist());
    }

    @Test
    void leDetailDonneLaStationEtLUrgence() throws Exception
    {
        mockMvc.perform(get("/api/incidents/{id}", actif.getId()).with(user(agent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Portique bloqué"))
                .andExpect(jsonPath("$.stationNom").value("Nation"))
                .andExpect(jsonPath("$.urgence").value("VERT"));
    }

    @Test
    void unIncidentInexistantDonneUne404EnJson() throws Exception
    {
        mockMvc.perform(get("/api/incidents/{id}", 999999).with(user(agent)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void sansConnexionOnEstRedirigeVersLaConnexion() throws Exception
    {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().is3xxRedirection());
    }
}
