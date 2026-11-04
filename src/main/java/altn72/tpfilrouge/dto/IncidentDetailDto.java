package altn72.tpfilrouge.dto;

import altn72.tpfilrouge.modele.StatutIncident;

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
        long dureeHeures,
        long dureeMinutes) {
}
