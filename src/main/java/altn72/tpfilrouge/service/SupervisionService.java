package altn72.tpfilrouge.service;

import altn72.tpfilrouge.dto.IncidentSupervisionDto;
import altn72.tpfilrouge.modele.StatutIncident;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.dto.IndicateursSupervisionDto;
import altn72.tpfilrouge.modele.NiveauUrgence;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupervisionService {

    private final IncidentRepository incidentRepository;
    private final CalculUrgenceService calculUrgenceService;
    private final Clock horloge;

    public SupervisionService(
            IncidentRepository incidentRepository,
            CalculUrgenceService calculUrgenceService,
            Clock horloge)
    {
        this.incidentRepository = incidentRepository;
        this.calculUrgenceService = calculUrgenceService;
        this.horloge = horloge;
    }

    public List<IncidentSupervisionDto> listerIncidentsActifs(
            Integer stationId,
            StatutIncident statut)
    {
        LocalDateTime maintenant = LocalDateTime.now(horloge);

        return incidentRepository
                .trouverIncidentsActifs(StatutIncident.RESOLU)
                .stream()
                .filter(incident -> stationId == null
                        || stationId.equals(
                        incident.getEquipement().getStation().getId()))
                .filter(incident -> statut == null
                        || incident.getStatut() == statut)
                .map(incident -> new IncidentSupervisionDto(
                        incident.getId(),
                        incident.getEquipement().getId(),
                        incident.getEquipement().getReference(),
                        incident.getEquipement().getStation().getNom(),
                        incident.getDescription(),
                        incident.getStatut(),
                        incident.getOuvertLe(),
                        Duration.between(
                                incident.getOuvertLe(),
                                maintenant).toHours(),
                        incident.getPrestataire() == null
                                ? null
                                : incident.getPrestataire().getNom(),
                        calculUrgenceService.calculer(incident)))
                .toList();
    }
    public IndicateursSupervisionDto calculerIndicateurs(
            List<IncidentSupervisionDto> incidents)
    {
        long affectes = incidents.stream()
                .filter(incident ->
                        incident.statut() == StatutIncident.AFFECTE)
                .count();

        long nonAffectes = incidents.stream()
                .filter(incident ->
                        incident.statut() == StatutIncident.NON_AFFECTE)
                .count();

        long urgents = incidents.stream()
                .filter(incident ->
                        incident.urgence() == NiveauUrgence.ROUGE)
                .count();

        return new IndicateursSupervisionDto(
                incidents.size(),
                affectes,
                nonAffectes,
                urgents);
    }
}