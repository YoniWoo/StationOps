package altn72.tpfilrouge.dto;

import altn72.tpfilrouge.modele.EtatEquipement;
import altn72.tpfilrouge.modele.TypeEquipement;

public record EquipementResumeDto(
        Integer id,
        String reference,
        String libelle,
        TypeEquipement type,
        EtatEquipement etat
) {
}
