package stationops.dto;

import stationops.modele.TypeEquipement;

public record DelaiResolutionDto(
        TypeEquipement type,
        double moyenneHeures,
        long nombreIncidents
) {
}
