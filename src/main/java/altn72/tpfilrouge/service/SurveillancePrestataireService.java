package altn72.tpfilrouge.service;

import altn72.tpfilrouge.repository.EvaluationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

// Service qui dit si un prestataire est "sous surveillance" (evolution seance finale)
// rien est stocké en base, on recalcule a chaque fois a partir des evaluations (PS4)
@Service
public class SurveillancePrestataireService
{
    // PS6 : le seuil et la taille de la fenetre sont definis ici et nulle part ailleurs
    public static final int TAILLE_FENETRE = 5;
    public static final double SEUIL_SURVEILLANCE = 3.0;

    private final EvaluationRepository evaluationRepository;

    public SurveillancePrestataireService(EvaluationRepository evaluationRepository)
    {
        this.evaluationRepository = evaluationRepository;
    }

    // moyenne des 5 dernieres notes, ou null si il en a moins de 5 (PS2)
    public Double calculerMoyenne(Integer prestataireId)
    {
        List<Integer> notes = evaluationRepository.trouverDernieresNotes(
                prestataireId,
                PageRequest.of(0, TAILLE_FENETRE));
        return calculerMoyenneDesNotes(notes);
    }

    public boolean estSousSurveillance(Integer prestataireId)
    {
        return estSousSurveillance(calculerMoyenne(prestataireId));
    }

    // PS3 : strictement inferieur, une moyenne de 3,0 pile ne met pas sous surveillance
    public boolean estSousSurveillance(Double moyenne)
    {
        return moyenne != null && moyenne < SEUIL_SURVEILLANCE;
    }

    // les notes arrivent de la plus récente a la plus ancienne (PS1)
    // on garde seulement les 5 premieres : la fenetre glisse (PS4)
    // séparé du repository pour pouvoir tester les exemples de l'enoncé sans base
    public Double calculerMoyenneDesNotes(List<Integer> notesDeLaPlusRecente)
    {
        if (notesDeLaPlusRecente.size() < TAILLE_FENETRE)
        {
            return null;
        }

        int somme = 0;
        for (int i = 0; i < TAILLE_FENETRE; i++)
        {
            somme = somme + notesDeLaPlusRecente.get(i);
        }
        return (double) somme / TAILLE_FENETRE;
    }
}
