package stationops.service;

import stationops.dto.DelaiResolutionDto;
import stationops.dto.IncidentsParStationDto;
import stationops.modele.StatutIncident;
import stationops.modele.TypeEquipement;
import stationops.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatistiquesService {

    private static final double SECONDES_PAR_HEURE = 3600.0;

    private final IncidentRepository incidentRepository;

    public StatistiquesService(IncidentRepository incidentRepository)
    {
        this.incidentRepository = incidentRepository;
    }

    public List<IncidentsParStationDto> compterActifsParStation()
    {
        return incidentRepository.compterActifsParStation(
                StatutIncident.RESOLU);
    }

    public List<DelaiResolutionDto> calculerDelaisMoyensParType()
    {
        Map<TypeEquipement, DoubleSummaryStatistics> statistiques =
                incidentRepository
                        .trouverHistorique(StatutIncident.RESOLU)
                        .stream()
                        .collect(Collectors.groupingBy(incident -> incident.getEquipement().getType(),
                                () -> new EnumMap<>(TypeEquipement.class),
                                Collectors.summarizingDouble(incident ->
                                        Duration.between(incident.getOuvertLe(), incident.getResoluLe())
                                                .toSeconds() / SECONDES_PAR_HEURE)));

        return statistiques.entrySet()
                .stream()
                .map(entree -> new DelaiResolutionDto(
                        entree.getKey(),
                        entree.getValue().getAverage(),
                        entree.getValue().getCount()))
                .toList();
    }
}