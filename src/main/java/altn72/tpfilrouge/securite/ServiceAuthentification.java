package altn72.tpfilrouge.securite;

import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServiceAuthentification implements UserDetailsService {
    private final UtilisateurRepository utilisateurRepository;

    public ServiceAuthentification(
            UtilisateurRepository utilisateurRepository)
    {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
    {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Identifiants invalides"));

        return new UtilisateurConnecte(utilisateur);
    }
}
