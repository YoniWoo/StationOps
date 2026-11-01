package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.StatutIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidentRepository
    extends JpaRepository<Incident, Integer> {
    List<Incident> findByEquipementIdOrderByOuvertLeDesc(Integer equipementId);

    boolean existsByEquipementIdAndStatutNot(
            Integer equipementid,
            StatutIncident statut);

    // F9 : JOIN FETCH pour avoir l'equipement, sa station et le prestataire en une seule requete
    // sinon avec open-in-view=false la page plante en LazyInitializationException
    // LEFT JOIN pour le prestataire car une resolution interne en a pas
    @Query("SELECT i FROM Incident i "
            + "JOIN FETCH i.equipement e "
            + "JOIN FETCH e.station "
            + "LEFT JOIN FETCH i.prestataire "
            + "WHERE i.statut = :statut "
            + "ORDER BY i.resoluLe DESC")
    List<Incident> trouverHistorique(@Param("statut") StatutIncident statut);

    @Query("SELECT i FROM Incident i "
            + "JOIN FETCH i.equipement e "
            + "JOIN FETCH e.station "
            + "LEFT JOIN FETCH i.prestataire "
            + "WHERE i.statut <> :statutExclu "
            + "ORDER BY i.ouvertLe ASC, i.id ASC")
    List<Incident> trouverIncidentsActifs(
            @Param("statutExclu") StatutIncident statutExclu);
}
