package stationops.controleur;

import stationops.securite.UtilisateurConnecte;
import stationops.service.EquipementService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/equipements")
public class EquipementControleur {
    private final EquipementService equipementService;

    public EquipementControleur(EquipementService equipementService)
    {
        this.equipementService = equipementService;
    }
    @GetMapping("/{id}")
    public String afficherFiche(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("fiche", equipementService.consulterFiche(id));

        return "ficheEquipement";
    }
}
