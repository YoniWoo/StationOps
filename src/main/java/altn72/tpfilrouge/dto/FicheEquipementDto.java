package altn72.tpfilrouge.dto;

import altn72.tpfilrouge.modele.EtatEquipement;
import altn72.tpfilrouge.modele.TypeEquipement;

import java.util.List;

public record FicheEquipementDto(
        Integer id,
        String reference,
        String libelle,
        TypeEquipement type,
        EtatEquipement etat,
        Integer stationId,
        String stationNom,
        List<IncidentResumeDto> incidents) {
}
