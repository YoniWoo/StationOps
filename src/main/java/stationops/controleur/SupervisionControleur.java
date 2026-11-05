package stationops.controleur;

import stationops.modele.StatutIncident;
import stationops.securite.UtilisateurConnecte;
import stationops.service.StationService;
import stationops.service.SupervisionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SupervisionControleur {

    private final SupervisionService supervisionService;
    private final StationService stationService;

    public SupervisionControleur(
            SupervisionService supervisionService,
            StationService stationService)
    {
        this.supervisionService = supervisionService;
        this.stationService = stationService;
    }

    @GetMapping("/supervision")
    public String afficherSupervision(
            @RequestParam(name = "stationId", required = false)
            Integer stationId,
            @RequestParam(name = "statut", required = false)
            StatutIncident statut,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        var incidents = supervisionService.listerIncidentsActifs(stationId, statut);
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("stations", stationService.listerStations());
        model.addAttribute("stationSelectionnee", stationId);
        model.addAttribute("statutSelectionne", statut == null ? "" : statut.name());
        model.addAttribute("incidents", incidents);
        model.addAttribute("indicateurs", supervisionService.calculerIndicateurs(incidents));
        return "supervision";
    }
}