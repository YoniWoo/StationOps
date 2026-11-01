package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.exception.EquipementIntrouvableException;
import altn72.tpfilrouge.exception.StationIntrouvableException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "altn72.tpfilrouge.controleur")
public class GestionErreurWeb {
    @ExceptionHandler({
            StationIntrouvableException.class,
            EquipementIntrouvableException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String afficherRessourceIntrouvable(
            RuntimeException exception,
            Model model)
    {
        model.addAttribute("message", exception.getMessage());

        return "erreur/404";
    }
}
