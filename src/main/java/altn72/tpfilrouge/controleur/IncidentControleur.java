package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.securite.UtilisateurConnecte;
import altn72.tpfilrouge.service.EvaluationService;
import altn72.tpfilrouge.service.IncidentService;
import altn72.tpfilrouge.service.PrestataireService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// pages web des incidents, les regles restent dans les services
@Controller
@RequestMapping("/incidents")
public class IncidentControleur {
    private final IncidentService incidentService;
    private final EvaluationService evaluationService;
    private final PrestataireService prestataireService;

    public IncidentControleur(
            IncidentService incidentService,
            EvaluationService evaluationService,
            PrestataireService prestataireService)
    {
        this.incidentService = incidentService;
        this.evaluationService = evaluationService;
        this.prestataireService = prestataireService;
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
        model.addAttribute("prestataires", prestataireService.listerPrestataires());

        return "incident";
    }

    // F7 : affecter ou reaffecter, reservé au responsable (voir ConfigurationSecurite)
    // redirect apres le POST pour que F5 rejoue pas l'action
    @PostMapping("/{id}/affectation")
    public String affecterPrestataire(
            @PathVariable("id") Integer id,
            @RequestParam("prestataireId") Integer prestataireId)
    {
        incidentService.affecterPrestataire(id, prestataireId);

        return "redirect:/incidents/" + id;
    }

    // F8 : l'heure de resolution est prise par le service avec l'horloge
    @PostMapping("/{id}/resolution")
    public String resoudreIncident(@PathVariable("id") Integer id)
    {
        incidentService.resoudreIncident(id);

        return "redirect:/incidents/" + id;
    }
}
