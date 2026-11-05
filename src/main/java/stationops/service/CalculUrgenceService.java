package stationops.service;

import stationops.modele.Incident;
import stationops.modele.NiveauUrgence;
import stationops.modele.StatutIncident;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CalculUrgenceService {
    private static final Duration SEUIL_ORANGE = Duration.ofHours(48);
    private static final Duration SEUIL_ROUGE = Duration.ofHours(72);

    private final Clock horloge;

    public CalculUrgenceService(Clock horloge)
    {
        this.horloge = horloge;
    }

    public NiveauUrgence calculer(Incident incident)
    {
        Objects.requireNonNull(incident, "Incident obligatoire");
        if (incident.getStatut() == StatutIncident.RESOLU)
        {
            return NiveauUrgence.RESOLU;
        }
        if (incident.getStatut() == StatutIncident.AFFECTE)
        {
            return NiveauUrgence.AFFECTE;
        }
        Duration anciennete = Duration.between(
                incident.getOuvertLe(),
                LocalDateTime.now(horloge));

        if (anciennete.compareTo(SEUIL_ROUGE) >= 0)
        {
            return NiveauUrgence.ROUGE;
        }
        if (anciennete.compareTo(SEUIL_ORANGE) >= 0)
        {
            return NiveauUrgence.ORANGE;
        }
        return NiveauUrgence.VERT;
    }
}
