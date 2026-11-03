package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.dto.DelaiResolutionDto;
import altn72.tpfilrouge.dto.IncidentsParStationDto;
import altn72.tpfilrouge.securite.UtilisateurConnecte;
import altn72.tpfilrouge.service.StatistiquesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatistiquesControleur {

    private final StatistiquesService statistiquesService;

    public StatistiquesControleur(
            StatistiquesService statistiquesService)
    {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping("/supervision/statistiques")
    public String afficherStatistiques(
            @AuthenticationPrincipal UtilisateurConnecte utilisateur,
            Model model)
    {
        var incidentsParStation =
                statistiquesService.compterActifsParStation();

        var delaisParType =
                statistiquesService.calculerDelaisMoyensParType();

        long maximumIncidents = incidentsParStation.stream()
                .mapToLong(IncidentsParStationDto::nombre)
                .max()
                .orElse(0L);

        double maximumDelai = delaisParType.stream()
                .mapToDouble(DelaiResolutionDto::moyenneHeures)
                .max()
                .orElse(0.0);

        model.addAttribute("utilisateurConnecte", utilisateur);
        model.addAttribute("incidentsParStation", incidentsParStation);
        model.addAttribute("delaisParType", delaisParType);
        model.addAttribute("maximumIncidents", maximumIncidents);
        model.addAttribute("maximumDelai", maximumDelai);

        return "statistiques";
    }
}