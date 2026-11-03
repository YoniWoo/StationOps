package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.*;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// tests des pages web des incidents : on passe par les vraies URL avec un utilisateur connecté
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IncidentWebTest {

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
    @Autowired
    private PrestataireRepository prestataireRepository;

    private MockMvc mockMvc;
    private UtilisateurConnecte agent;
    private UtilisateurConnecte responsable;
    private Equipement equipement;
    private Incident incident;

    @BeforeEach
    void preparer()
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(contexte)
                .apply(springSecurity())
                .build();

        Utilisateur compteAgent = utilisateurRepository.save(new Utilisateur(
                "agent-web@stationops.test", "hash-de-test",
                "Agent", Profil.AGENT_TERRAIN));
        Utilisateur compteResponsable = utilisateurRepository.save(new Utilisateur(
                "responsable-web@stationops.test", "hash-de-test",
                "Responsable", Profil.RESPONSABLE_EXPLOITATION));
        agent = new UtilisateurConnecte(compteAgent);
        responsable = new UtilisateurConnecte(compteResponsable);

        Station station = stationRepository.save(new Station("Nation"));
        equipement = equipementRepository.save(new Equipement(
                "WEB-POR-001", "Portique entrée nord",
                TypeEquipement.PORTIQUE, station));
        equipement.mettreHorsService();
        incident = incidentRepository.save(new Incident(
                "Portique bloqué", equipement, compteAgent,
                LocalDateTime.now().minusHours(5)));
        prestataireRepository.save(new Prestataire(
                "Mobilité Maintenance", "contact@mm.test", null));
    }

    @Test
    void detailDUnIncidentEstAffiche() throws Exception
    {
        mockMvc.perform(get("/incidents/{id}", incident.getId()).with(user(agent)))
                .andExpect(status().isOk())
                .andExpect(view().name("incident"))
                .andExpect(content().string(containsString("Portique bloqué")));
    }

    @Test
    void incidentInexistantDonneUne404() throws Exception
    {
        mockMvc.perform(get("/incidents/{id}", 999999).with(user(agent)))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erreur/404"));
    }

    @Test
    void unAgentNePeutPasResoudreUnIncident() throws Exception
    {
        mockMvc.perform(post("/incidents/{id}/resolution", incident.getId())
                        .with(user(agent)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void leResponsableResoutPuisRevientSurLeDetail() throws Exception
    {
        mockMvc.perform(post("/incidents/{id}/resolution", incident.getId())
                        .with(user(responsable)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/incidents/" + incident.getId()));
    }

    @Test
    void resoudreDeuxFoisDonneUne409() throws Exception
    {
        mockMvc.perform(post("/incidents/{id}/resolution", incident.getId())
                .with(user(responsable)).with(csrf()));

        mockMvc.perform(post("/incidents/{id}/resolution", incident.getId())
                        .with(user(responsable)).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(view().name("erreur/action-impossible"));
    }

    @Test
    void leResponsableVoitLeFormulaireDAffectation() throws Exception
    {
        mockMvc.perform(get("/incidents/{id}", incident.getId()).with(user(responsable)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mobilité Maintenance")));
    }

    @Test
    void unAgentDeclareUnIncidentEtArriveSurSonDetail() throws Exception
    {
        mockMvc.perform(post("/incidents/nouveau")
                        .param("equipementId", equipement.getId().toString())
                        .param("description", "Lecteur de carte en panne")
                        .with(user(agent)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/incidents/*"));
    }

    @Test
    void uneDescriptionVideReafficheLeFormulaireAvecUnMessage() throws Exception
    {
        mockMvc.perform(post("/incidents/nouveau")
                        .param("equipementId", equipement.getId().toString())
                        .param("description", "  ")
                        .with(user(agent)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("incidentNouveau"))
                .andExpect(model().attributeHasFieldErrors("formulaire", "description"));
    }

    @Test
    void leFormulaireDeDeclarationSAffiche() throws Exception
    {
        mockMvc.perform(get("/incidents/nouveau")
                        .param("equipementId", equipement.getId().toString())
                        .with(user(agent)))
                .andExpect(status().isOk())
                .andExpect(view().name("incidentNouveau"));
    }

    @Test
    void modifierLaDescriptionRevientSurLeDetail() throws Exception
    {
        mockMvc.perform(post("/incidents/{id}/modifier", incident.getId())
                        .param("description", "Portique bloqué en position fermée")
                        .with(user(agent)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/incidents/" + incident.getId()));
    }

    @Test
    void leFormulaireDeModificationEstPreRempli() throws Exception
    {
        mockMvc.perform(get("/incidents/{id}/modifier", incident.getId()).with(user(agent)))
                .andExpect(status().isOk())
                .andExpect(view().name("incidentModifier"))
                .andExpect(model().attribute("formulaire",
                        hasProperty("description", is("Portique bloqué"))));
    }

    @Test
    void lHistoriqueEstAccessibleAUnAgent() throws Exception
    {
        mockMvc.perform(get("/incidents/historique").with(user(agent)))
                .andExpect(status().isOk())
                .andExpect(view().name("historique"));
    }

    // on prepare un incident résolu avec un prestataire pour pouvoir l'evaluer
    private void resoudreAvecPrestataire()
    {
        incident.affecterPrestataire(prestataireRepository.findAllByOrderByNomAsc().get(0));
        incident.resoudre(LocalDateTime.now());
    }

    @Test
    void leResponsableEvalueUnIncidentResolu() throws Exception
    {
        resoudreAvecPrestataire();

        mockMvc.perform(post("/incidents/{id}/evaluation", incident.getId())
                        .param("note", "4")
                        .param("commentaire", "Intervention rapide")
                        .with(user(responsable)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/incidents/" + incident.getId()));
    }

    @Test
    void uneNoteManquanteReafficheLeFormulaire() throws Exception
    {
        resoudreAvecPrestataire();

        mockMvc.perform(post("/incidents/{id}/evaluation", incident.getId())
                        .with(user(responsable)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("incidentEvaluation"))
                .andExpect(model().attributeHasFieldErrors("formulaire", "note"));
    }

    @Test
    void evaluerUnIncidentNonResoluDonneUne409() throws Exception
    {
        mockMvc.perform(post("/incidents/{id}/evaluation", incident.getId())
                        .param("note", "4")
                        .with(user(responsable)).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void unAgentNePeutPasOuvrirLeFormulaireDEvaluation() throws Exception
    {
        mockMvc.perform(get("/incidents/{id}/evaluation", incident.getId()).with(user(agent)))
                .andExpect(status().isForbidden());
    }

    @Test
    void leFormulaireDEvaluationSAffichePourLeResponsable() throws Exception
    {
        resoudreAvecPrestataire();

        mockMvc.perform(get("/incidents/{id}/evaluation", incident.getId()).with(user(responsable)))
                .andExpect(status().isOk())
                .andExpect(view().name("incidentEvaluation"));
    }
}
