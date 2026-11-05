package stationops.dto;

import stationops.modele.NiveauUrgence;
import stationops.modele.StatutIncident;

import java.time.LocalDateTime;

public record IncidentSupervisionDto(
        Integer id,
        Integer equipementId,
        String equipementReference,
        String stationNom,
        String description,
        StatutIncident statut,
        LocalDateTime ouvertLe,
        long ancienneteHeures,
        String prestataireNom,
        NiveauUrgence urgence
) {
}
