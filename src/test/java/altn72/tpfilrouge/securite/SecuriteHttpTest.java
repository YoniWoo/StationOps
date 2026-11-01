package altn72.tpfilrouge.securite;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SecuriteHttpTest {

    private static final String EMAIL_AGENT =
            "agent-http@stationops.test";
    private static final String EMAIL_RESPONSABLE =
            "responsable-http@stationops.test";
    private static final String MOT_DE_PASSE =
            "MotDePasse-Test-123!";

    @Autowired
    private WebApplicationContext contexte;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void preparer()
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(contexte)
                .apply(springSecurity())
                .build();

        utilisateurRepository.save(new Utilisateur(
                EMAIL_AGENT,
                passwordEncoder.encode(MOT_DE_PASSE),
                "Agent",
                Profil.AGENT_TERRAIN));

        utilisateurRepository.save(new Utilisateur(
                EMAIL_RESPONSABLE,
                passwordEncoder.encode(MOT_DE_PASSE),
                "Responsable",
                Profil.RESPONSABLE_EXPLOITATION));
    }

    @Test
    void connexionEstAccessibleSansAuthentification() throws Exception
    {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("connexion"))
                .andExpect(unauthenticated());
    }

    @Test
    void visiteurEstRedirigeVersLaConnexion() throws Exception
    {
        mockMvc.perform(get("/stations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(unauthenticated());
    }

    @Test
    void identifiantsValidesConnectentUtilisateur() throws Exception
    {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", EMAIL_AGENT)
                        .param("password", MOT_DE_PASSE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername(EMAIL_AGENT));
    }

    @Test
    void mauvaisMotDePasseEstRefuse() throws Exception
    {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", EMAIL_AGENT)
                        .param("password", "mauvais-mot-de-passe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void agentNePeutPasAccederALaSupervision() throws Exception
    {
        MockHttpSession session = connecter(EMAIL_AGENT);

        mockMvc.perform(get("/supervision").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void responsablePeutAccederALaSupervision() throws Exception
    {
        MockHttpSession session = connecter(EMAIL_RESPONSABLE);

        mockMvc.perform(get("/supervision").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("supervision"));
    }

    @Test
    void deconnexionInvalideLaSession() throws Exception
    {
        MockHttpSession session = connecter(EMAIL_AGENT);

        mockMvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());

        assertTrue(session.isInvalid());

        mockMvc.perform(get("/stations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void connexionSansJetonCsrfEstRefusee() throws Exception
    {
        mockMvc.perform(post("/login")
                        .param("username", EMAIL_AGENT)
                        .param("password", MOT_DE_PASSE))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
    }

    @Test
    void deconnexionSansJetonCsrfEstRefusee() throws Exception
    {
        MockHttpSession session = connecter(EMAIL_AGENT);

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().isForbidden())
                .andExpect(authenticated().withUsername(EMAIL_AGENT));
    }

    private MockHttpSession connecter(String email) throws Exception
    {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", email)
                        .param("password", MOT_DE_PASSE))
                .andExpect(authenticated().withUsername(email))
                .andReturn()
                .getRequest()
                .getSession(false);
    }
}