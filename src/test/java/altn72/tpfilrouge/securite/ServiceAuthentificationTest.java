package altn72.tpfilrouge.securite;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServiceAuthentificationTest {
    private UtilisateurRepository utilisateurRepository;
    private ServiceAuthentification serviceAuthentification;

    @BeforeEach
    void preparer()
    {
        utilisateurRepository = mock(UtilisateurRepository.class);
        serviceAuthentification =
                new ServiceAuthentification(utilisateurRepository);
    }

    @Test
    void responsableExistantPossedeLesInformationsEtLeRoleAttendus() {
        Utilisateur utilisateur = new Utilisateur(
                "responsable@stationops.test",
                "hash-de-test",
                "Yoni",
                Profil.RESPONSABLE_EXPLOITATION
        );

        when(utilisateurRepository.findByEmail(utilisateur.getEmail()))
                .thenReturn(Optional.of(utilisateur));

        UserDetails resultat = serviceAuthentification
                .loadUserByUsername(utilisateur.getEmail());

        assertEquals(utilisateur.getEmail(), resultat.getUsername());
        assertEquals("hash-de-test", resultat.getPassword());

        assertTrue(resultat.getAuthorities().contains(
                new SimpleGrantedAuthority(
                        "ROLE_RESPONSABLE_EXPLOITATION")));

        UtilisateurConnecte utilisateurConnecte =
                assertInstanceOf(UtilisateurConnecte.class, resultat);

        assertEquals("Yoni", utilisateurConnecte.getPrenom());
        assertEquals(
                Profil.RESPONSABLE_EXPLOITATION,
                utilisateurConnecte.getProfil()
        );
    }
    @Test
    void agentExistantNePossedePasLeRoleResponsable() {
        Utilisateur utilisateur = new Utilisateur(
                "agent@stationops.test",
                "hash-de-test",
                "Agent",
                Profil.AGENT_TERRAIN
        );

        when(utilisateurRepository.findByEmail(utilisateur.getEmail()))
                .thenReturn(Optional.of(utilisateur));

        UserDetails resultat = serviceAuthentification
                .loadUserByUsername(utilisateur.getEmail());

        assertEquals(
                1,
                resultat.getAuthorities().size()
        );

        assertTrue(resultat.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_AGENT_TERRAIN")));

        assertFalse(resultat.getAuthorities().contains(
                new SimpleGrantedAuthority(
                        "ROLE_RESPONSABLE_EXPLOITATION")));
    }
    @Test
    void emailInconnuProvoqueUneExceptionAuthentification() {
        String email = "inconnu@stationops.test";

        when(utilisateurRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceAuthentification.loadUserByUsername(email)
        );
    }
}
