package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.securite.UtilisateurConnecte;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.StationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stations")
public class StationControleur {
    private final StationService stationService;
    private final EquipementService equipementService;

    public StationControleur(
            StationService stationService,
            EquipementService equipementService)
    {
        this.equipementService = equipementService;
        this.stationService = stationService;
    }

    @GetMapping
    public String afficherStation(
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("stations", stationService.listerStations());

        return "stations";
    }

    @GetMapping("/{id}/equipements")
    public String afficherEquipement(
            @PathVariable("id") Integer stationId,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("station", stationService.trouverStation(stationId));
        model.addAttribute("equipements", equipementService.listerParStation(stationId));

        return "equipements";
    }
}
