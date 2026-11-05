package stationops.dto;

import stationops.modele.NiveauUrgence;
import stationops.modele.StatutIncident;

import java.time.LocalDateTime;

// ce que la page detail affiche (F5), comme ca la vue touche pas aux entités JPA
public record IncidentDetailDto(
        Integer id,
        String description,
        StatutIncident statut,
        LocalDateTime ouvertLe,
        LocalDateTime resoluLe,
        Integer equipementId,
        String equipementReference,
        String equipementLibelle,
        String stationNom,
        String prestataireNom,
        boolean prestataireSousSurveillance,
        long dureeHeures,
        long dureeMinutes,
        NiveauUrgence urgence) {
}
