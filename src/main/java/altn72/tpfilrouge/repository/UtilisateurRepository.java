package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.Optional;

public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
}
