package stationops.repository;

import stationops.modele.Evaluation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository
    extends JpaRepository<Evaluation, Integer>
{
    boolean existsByIncidentId(Integer incidentId);
    Optional<Evaluation> findByIncidentId(Integer incidentId);

    // PS1 : notes du prestataire de la plus récente a la plus ancienne (date puis id si meme date)
    // le nombre de notes a prendre est donné par le Pageable, comme ca la taille de la fenetre
    // reste dans SurveillancePrestataireService (PS6) et pas dans le nom de la methode
    @Query("SELECT e.note FROM Evaluation e "
            + "WHERE e.incident.prestataire.id = :prestataireId "
            + "ORDER BY e.dateEvaluation DESC, e.id DESC")
    List<Integer> trouverDernieresNotes(
            @Param("prestataireId") Integer prestataireId,
            Pageable page);
}
