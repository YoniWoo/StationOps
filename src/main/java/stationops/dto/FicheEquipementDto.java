package stationops.dto;

import stationops.modele.EtatEquipement;
import stationops.modele.TypeEquipement;

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
