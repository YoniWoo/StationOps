package altn72.tpfilrouge.controleur;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConnexionControleur {
    @GetMapping("/login")
    public String afficherConnexion()
    {
        return "connexion";
    }
}
