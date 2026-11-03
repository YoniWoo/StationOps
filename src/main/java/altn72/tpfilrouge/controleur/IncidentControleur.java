package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.securite.UtilisateurConnecte;
import altn72.tpfilrouge.service.EvaluationService;
import altn72.tpfilrouge.service.IncidentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// pages web des incidents, les regles restent dans les services
@Controller
@RequestMapping("/incidents")
public class IncidentControleur {
    private final IncidentService incidentService;
    private final EvaluationService evaluationService;

    public IncidentControleur(
            IncidentService incidentService,
            EvaluationService evaluationService)
    {
        this.incidentService = incidentService;
        this.evaluationService = evaluationService;
    }

    // F5 : detail d'un incident, 404 si l'id existe pas (voir GestionErreurWeb)
    @GetMapping("/{id}")
    public String afficherDetail(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("incident", incidentService.consulterDetail(id));
        model.addAttribute("evaluation", evaluationService.trouverEvaluation(id).orElse(null));

        return "incident";
    }
}
