package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.EquipementIntrouvableException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.repository.EquipementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipementService {
    private final EquipementRepository equipementRepository;
    private final StationService stationService;

    public EquipementService(
            EquipementRepository equipementRepository,
            StationService stationService) {
        this.equipementRepository = equipementRepository;
        this.stationService = stationService;
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
}
