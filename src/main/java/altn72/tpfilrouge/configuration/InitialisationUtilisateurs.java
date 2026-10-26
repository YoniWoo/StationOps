package altn72.tpfilrouge.configuration;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(10)
public class InitialisationUtilisateurs implements CommandLineRunner {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final String motDePasseResponsable;
    private final String motDePasseAgent;

    public InitialisationUtilisateurs(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            @Value("${DEMO_RESPONSABLE_PASSWORD}")
            String motDePasseResponsable,
            @Value("${DEMO_AGENT_PASSWORD}")
            String motDePasseAgent)
    {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.motDePasseResponsable = motDePasseResponsable;
        this.motDePasseAgent = motDePasseAgent;
    }

    @Override
    @Transactional
    public void run(String... args)
    {
        creerSiAbsent(
                "responsable@stationops.test",
                motDePasseResponsable,
                "Yoni",
                Profil.RESPONSABLE_EXPLOITATION
        );
        creerSiAbsent(
                "agent@stationops.test",
                motDePasseAgent,
                "Agent",
                Profil.AGENT_TERRAIN
        );
    }

    private void creerSiAbsent(
            String email,
            String motDePasse,
            String prenom,
            Profil profil
    )
    {
        if (motDePasse == null || motDePasse.isBlank())
        {
            throw new IllegalArgumentException(
                    "Le mot de passe de démonstration doit être renseigné");
        }
        if (utilisateurRepository.findByEmail(email).isPresent())
        {
            return;
        }
        Utilisateur utilisateur = new Utilisateur(
                email,
                passwordEncoder.encode(motDePasse),
                prenom,
                profil
        );
        utilisateurRepository.save(utilisateur);
    }
}
