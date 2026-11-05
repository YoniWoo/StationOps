package stationops.dto;

import stationops.modele.StatutIncident;

import java.time.LocalDateTime;

public record IncidentResumeDto (
        Integer id,
        String description,
        StatutIncident statut,
        LocalDateTime ouvertLe,
        LocalDateTime resoluLe){

}
