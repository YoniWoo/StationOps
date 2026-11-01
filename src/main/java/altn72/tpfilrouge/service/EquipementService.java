package altn72.tpfilrouge.service;

import altn72.tpfilrouge.dto.FicheEquipementDto;
import altn72.tpfilrouge.dto.IncidentResumeDto;
import altn72.tpfilrouge.exception.EquipementIntrouvableException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipementService {
    private final EquipementRepository equipementRepository;
    private final StationService stationService;
    private final IncidentRepository incidentRepository;

    public EquipementService(
            EquipementRepository equipementRepository,
            StationService stationService,
            IncidentRepository incidentRepository) {
        this.equipementRepository = equipementRepository;
        this.stationService = stationService;
        this.incidentRepository = incidentRepository;
    }

    public List<Equipement> listerParStation(Integer stationId)
    {
        stationService.trouverStation(stationId);
        return equipementRepository
                .findByStationIdOrderByReferenceAsc(stationId);
    }

    public  Equipement trouverEquipement(Integer id)
    {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new EquipementIntrouvableException(id));
    }

    public FicheEquipementDto consulterFiche(Integer id)
    {
        Equipement equipement = trouverEquipement(id);
        List<IncidentResumeDto> incidents = incidentRepository
                .findByEquipementIdOrderByOuvertLeDesc(id)
                .stream()
                .map(incident -> new IncidentResumeDto(
                        incident.getId(),
                        incident.getDescription(),
                        incident.getStatut(),
                        incident.getOuvertLe(),
                        incident.getResoluLe()
                )).toList();

        return new FicheEquipementDto(
                equipement.getId(),
                equipement.getReference(),
                equipement.getLibelle(),
                equipement.getType(),
                equipement.getEtat(),
                equipement.getStation().getId(),
                equipement.getStation().getNom(),
                incidents
        );
    }
}
