package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.EvaluationInterditeException;
import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.EvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

// Service pour evaluer le prestataire une fois l'incident résolu (F10)
@Service
public class EvaluationService
{
    private final EvaluationRepository evaluationRepository;

    // on reutilise IncidentService pour pas refaire la recherche + le 404
    private final IncidentService incidentService;
    private final Clock horloge;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            IncidentService incidentService,
            Clock horloge)
    {
        this.evaluationRepository = evaluationRepository;
        this.incidentService = incidentService;
        this.horloge = horloge;
    }

    // RG8 : une seule evaluation par incident, le reste (résolu, prestataire, note 1 a 5) est verifié dans Evaluation
    // on verifie avant d'enregistrer pour avoir un message clair au lieu de l'erreur UNIQUE de la base
    @Transactional
    public Evaluation evaluerPrestataire(Integer incidentId, int note, String commentaire)
    {
        Incident incident = incidentService.trouverIncident(incidentId);

        if (evaluationRepository.existsByIncidentId(incidentId))
        {
            throw new EvaluationInterditeException(
                    "Cet incident a déjà été évalué.");
        }

        Evaluation evaluation = new Evaluation(
                incident,
                note,
                commentaire,
                LocalDateTime.now(horloge));
        evaluationRepository.save(evaluation);
        return evaluation;
    }

    // vide si l'incident a pas encore été evalué, c'est pas une erreur
    public Optional<Evaluation> trouverEvaluation(Integer incidentId)
    {
        return evaluationRepository.findByIncidentId(incidentId);
    }
}
