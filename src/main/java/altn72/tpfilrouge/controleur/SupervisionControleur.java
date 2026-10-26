package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.securite.UtilisateurConnecte;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupervisionControleur {
    @GetMapping("/supervision")
    public String afficherSupervision(
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        model.addAttribute("utilisateurConnecte", utilisateur);
        return "supervision";
    }
}
