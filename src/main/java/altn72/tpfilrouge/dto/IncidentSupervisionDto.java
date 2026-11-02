package altn72.tpfilrouge.dto;

import altn72.tpfilrouge.modele.NiveauUrgence;
import altn72.tpfilrouge.modele.StatutIncident;

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
