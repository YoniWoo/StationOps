package altn72.tpfilrouge.dto;

import altn72.tpfilrouge.modele.StatutIncident;

import java.time.LocalDateTime;

public record IncidentResumeDto (
        Integer id,
        String description,
        StatutIncident statut,
        LocalDateTime ouvertLe,
        LocalDateTime resoluLe){

}
