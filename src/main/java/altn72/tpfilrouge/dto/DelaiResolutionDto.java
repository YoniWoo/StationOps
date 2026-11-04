package altn72.tpfilrouge.dto;

import altn72.tpfilrouge.modele.TypeEquipement;

public record DelaiResolutionDto(
        TypeEquipement type,
        double moyenneHeures,
        long nombreIncidents
) {
}
