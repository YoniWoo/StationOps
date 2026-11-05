package stationops.repository;

import stationops.modele.Equipement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipementRepository
    extends JpaRepository<Equipement, Integer> {
    Optional<Equipement> findByReference(String reference);

    @EntityGraph(attributePaths = "station")
    List<Equipement> findByStationIdOrderByReferenceAsc(
            Integer stationId);

    @Override
    @EntityGraph(attributePaths = "station")
    Optional<Equipement> findById(Integer id);
}
