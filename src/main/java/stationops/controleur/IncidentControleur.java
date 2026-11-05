package stationops.controleur;

import stationops.dto.DeclarationIncidentForm;
import stationops.dto.EvaluationForm;
import stationops.dto.IncidentDetailDto;
import stationops.dto.ModificationIncidentForm;
import stationops.exception.AffectationAConfirmerException;
import stationops.modele.Incident;
import stationops.securite.UtilisateurConnecte;
import stationops.service.EquipementService;
import stationops.service.EvaluationService;
import stationops.service.ExportCsvService;
import stationops.service.IncidentService;
import stationops.service.PrestataireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.nio.charset.StandardCharsets;

// pages web des incidents, les regles restent dans les services
@Controller
@RequestMapping("/incidents")
public class IncidentControleur {
    private final IncidentService incidentService;
    private final EvaluationService evaluationService;
    private final PrestataireService prestataireService;
    private final EquipementService equipementService;
    private final ExportCsvService exportCsvService;

    public IncidentControleur(
            IncidentService incidentService,
            EvaluationService evaluationService,
            PrestataireService prestataireService,
            EquipementService equipementService,
            ExportCsvService exportCsvService)
    {
        this.incidentService = incidentService;
        this.evaluationService = evaluationService;
        this.prestataireService = prestataireService;
        this.equipementService = equipementService;
        this.exportCsvService = exportCsvService;
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

    // lot alpha : telechargement du CSV
    // ResponseEntity ici car il faut regler les en-tetes (type du fichier et nom de telechargement)
    @GetMapping("/historique/export")
    public ResponseEntity<byte[]> exporterHistorique()
    {
        String csv = exportCsvService.exporterHistorique(
                evaluationService.listerHistoriqueAvecNotes());

        // le caractere BOM au debut dit a Excel que le fichier est en UTF-8 (sinon les accents sont cassés)
        byte[] contenu = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"historique-incidents.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenu);
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
        remplirDetail(id, utilisateur, model);

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
    // PS5 : si le service demande une confirmation on reaffiche le detail avec le message et
    // un bouton "Confirmer" qui renvoie le meme formulaire avec confirmation=true
    // le controleur decide rien, il affiche juste ce que le service a refusé
    public String affecterPrestataire(
            @PathVariable("id") Integer id,
            @RequestParam("prestataireId") Integer prestataireId,
            @RequestParam(name = "confirmation", defaultValue = "false") boolean confirmation,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        try
        {
            incidentService.affecterPrestataire(id, prestataireId, confirmation);
        }
        catch (AffectationAConfirmerException demande)
        {
            remplirDetail(id, utilisateur, model);
            model.addAttribute("confirmationMessage", demande.getMessage());
            model.addAttribute("confirmationPrestataireId", demande.getPrestataireId());
            return "incident";
        }

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

    // tout ce que la page detail affiche (F5), utilisé aussi quand on redemande une confirmation
    private void remplirDetail(Integer id, UtilisateurConnecte utilisateur, Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("incident", incidentService.consulterDetail(id));
        model.addAttribute("evaluation", evaluationService.trouverEvaluation(id).orElse(null));
        model.addAttribute("prestataires", prestataireService.listerPrestatairesPourAffectation());
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
