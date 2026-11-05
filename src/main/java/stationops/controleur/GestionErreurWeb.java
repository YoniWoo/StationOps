package stationops.controleur;

import stationops.exception.EquipementIntrouvableException;
import stationops.exception.EvaluationInterditeException;
import stationops.exception.RessourceIntrouvableException;
import stationops.exception.StationIntrouvableException;
import stationops.exception.TransitionIncidentInterditeException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "stationops.controleur")
public class GestionErreurWeb {
    @ExceptionHandler({
            StationIntrouvableException.class,
            EquipementIntrouvableException.class,
            RessourceIntrouvableException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String afficherRessourceIntrouvable(
            RuntimeException exception,
            Model model)
    {
        model.addAttribute("message", exception.getMessage());

        return "erreur/404";
    }

    // 409 : la demande est correcte mais l'etat de l'incident l'interdit (deja résolu, deja evalué...)
    // on affiche le message en francais de l'exception, jamais la trace technique
    @ExceptionHandler({
            TransitionIncidentInterditeException.class,
            EvaluationInterditeException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String afficherActionImpossible(
            RuntimeException exception,
            Model model)
    {
        model.addAttribute("message", exception.getMessage());

        return "erreur/action-impossible";
    }
}
