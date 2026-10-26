package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.StatutIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository
    extends JpaRepository<Incident, Integer> {
    List<Incident> findByEquipementIdOrderByOuvertLeDesc(Integer equipementId);

    boolean existsByEquipementIdAndStatutNot(
            Integer equipementid,
            StatutIncident statut);

    List<Incident> findByStatutOrderByResoluLeDesc(
            StatutIncident statut);
}
