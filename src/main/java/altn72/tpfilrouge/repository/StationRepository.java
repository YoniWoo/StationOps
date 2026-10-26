package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository
        extends JpaRepository<Station, Integer> {
    List<Station> findAllByOrderByNomAsc();
}
