package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.StationIntrouvableException;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StationService {
    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository)
    {
        this.stationRepository = stationRepository;
    }

    public List<Station> listerStations()
    {
        return stationRepository.findAllByOrderByNomAsc();
    }

    public Station trouverStation(Integer id)
    {
        return stationRepository.findById(id)
                .orElseThrow(() -> new StationIntrouvableException(id));
    }
}
