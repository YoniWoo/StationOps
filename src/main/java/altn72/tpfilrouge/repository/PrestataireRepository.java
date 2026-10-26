package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestataireRepository
    extends JpaRepository<Prestataire, Integer> {
    List<Prestataire> findAllByOrderByNomAsc();
}
