package stationops.service;

import stationops.exception.StationIntrouvableException;
import stationops.modele.Station;
import stationops.repository.StationRepository;
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
