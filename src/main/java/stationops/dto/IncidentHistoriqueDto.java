package stationops.dto;

import java.time.LocalDateTime;

// une ligne de l'historique (F9), note est null si l'incident a pas été evalué
public record IncidentHistoriqueDto(
        Integer id,
        String description,
        String stationNom,
        String equipementReference,
        String prestataireNom,
        LocalDateTime ouvertLe,
        LocalDateTime resoluLe,
        Integer note) {
}
