package stationops.dto;

import stationops.modele.EtatEquipement;
import stationops.modele.TypeEquipement;

public record EquipementResumeDto(
        Integer id,
        String reference,
        String libelle,
        TypeEquipement type,
        EtatEquipement etat
) {
}
