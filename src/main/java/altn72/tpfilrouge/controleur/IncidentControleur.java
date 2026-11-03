package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.dto.DeclarationIncidentForm;
import altn72.tpfilrouge.dto.EvaluationForm;
import altn72.tpfilrouge.dto.IncidentDetailDto;
import altn72.tpfilrouge.dto.ModificationIncidentForm;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.securite.UtilisateurConnecte;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.EvaluationService;
import altn72.tpfilrouge.service.IncidentService;
import altn72.tpfilrouge.service.PrestataireService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final EquipementService equipementService;

    public IncidentControleur(
            IncidentService incidentService,
            EvaluationService evaluationService,
            PrestataireService prestataireService,
            EquipementService equipementService)
    {
        this.incidentService = incidentService;
        this.evaluationService = evaluationService;
        this.prestataireService = prestataireService;
        this.equipementService = equipementService;
    }

    // F9 : accessible aux 2 profils en lecture
    @GetMapping("/historique")
    public String afficherHistorique(
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("incidents", evaluationService.listerHistoriqueAvecNotes());

        return "historique";
    }

    // F4 : formulaire vide pour un equipement donné (on arrive depuis la fiche equipement)
    @GetMapping("/nouveau")
    public String afficherDeclaration(
            @RequestParam("equipementId") Integer equipementId,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        DeclarationIncidentForm formulaire = new DeclarationIncidentForm();
        formulaire.setEquipementId(equipementId);

        model.addAttribute("formulaire", formulaire);
        return afficherFormulaireDeclaration(equipementId, utilisateur, model);
    }

    // F4 : si un champ est pas valide on reaffiche la meme page avec les messages
    @PostMapping("/nouveau")
    public String declarerIncident(
            @Valid @ModelAttribute("formulaire") DeclarationIncidentForm formulaire,
            BindingResult erreurs,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        if (erreurs.hasErrors())
        {
            return afficherFormulaireDeclaration(formulaire.getEquipementId(), utilisateur, model);
        }

        Incident incident = incidentService.declarerIncident(
                formulaire.getEquipementId(),
                formulaire.getDescription(),
                utilisateur.getId());

        return "redirect:/incidents/" + incident.getId();
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

    // F6 : formulaire pré-rempli avec la description actuelle
    @GetMapping("/{id}/modifier")
    public String afficherModification(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        IncidentDetailDto incident = incidentService.consulterDetail(id);

        ModificationIncidentForm formulaire = new ModificationIncidentForm();
        formulaire.setDescription(incident.description());

        model.addAttribute("formulaire", formulaire);
        model.addAttribute("incident", incident);
        model.addAttribute("utilisateurConnecte", utilisateur);
        return "incidentModifier";
    }

    // F6 : si l'incident est résolu entre temps, le modele refuse et on tombe sur la page 409
    @PostMapping("/{id}/modifier")
    public String modifierDescription(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("formulaire") ModificationIncidentForm formulaire,
            BindingResult erreurs,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        if (erreurs.hasErrors())
        {
            model.addAttribute("incident", incidentService.consulterDetail(id));
            model.addAttribute("utilisateurConnecte", utilisateur);
            return "incidentModifier";
        }

        incidentService.modifierDescription(id, formulaire.getDescription());

        return "redirect:/incidents/" + id;
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

    // F10 : formulaire d'evaluation, reservé au responsable
    @GetMapping("/{id}/evaluation")
    public String afficherEvaluation(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("formulaire", new EvaluationForm());
        return afficherFormulaireEvaluation(id, utilisateur, model);
    }

    // F10 : les regles (résolu, prestataire, une seule evaluation) sont verifiées dans EvaluationService
    @PostMapping("/{id}/evaluation")
    public String evaluerPrestataire(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("formulaire") EvaluationForm formulaire,
            BindingResult erreurs,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        if (erreurs.hasErrors())
        {
            return afficherFormulaireEvaluation(id, utilisateur, model);
        }

        evaluationService.evaluerPrestataire(
                id,
                formulaire.getNote(),
                formulaire.getCommentaire());

        return "redirect:/incidents/" + id;
    }

    private String afficherFormulaireEvaluation(
            Integer incidentId,
            UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("incident", incidentService.consulterDetail(incidentId));

        return "incidentEvaluation";
    }

    private String afficherFormulaireDeclaration(
            Integer equipementId,
            UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("equipement", equipementService.consulterFiche(equipementId));

        return "incidentNouveau";
    }
}
