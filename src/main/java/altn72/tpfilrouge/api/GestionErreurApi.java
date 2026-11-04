package altn72.tpfilrouge.api;

import altn72.tpfilrouge.dto.ErreurApiDto;
import altn72.tpfilrouge.exception.EquipementIntrouvableException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.StationIntrouvableException;
import altn72.tpfilrouge.exception.TransitionIncidentInterditeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "altn72.tpfilrouge.api")
public class GestionErreurApi {

    @ExceptionHandler({
            StationIntrouvableException.class,
            EquipementIntrouvableException.class,
            RessourceIntrouvableException.class
    })
    public ResponseEntity<ErreurApiDto> gererRessourceIntrouvable(
            RuntimeException exception)
    {
        return construireErreur(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    @ExceptionHandler(TransitionIncidentInterditeException.class)
    public ResponseEntity<ErreurApiDto> gererTransitionInterdite(
            TransitionIncidentInterditeException exception)
    {
        return construireErreur(
                HttpStatus.CONFLICT,
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurApiDto> gererParametreInvalide(
            MethodArgumentTypeMismatchException exception)
    {
        return construireErreur(
                HttpStatus.BAD_REQUEST,
                "Le paramètre '" + exception.getName()
                        + "' possède une valeur invalide.");
    }

    private ResponseEntity<ErreurApiDto> construireErreur(
            HttpStatus statut,
            String message)
    {
        ErreurApiDto erreur = new ErreurApiDto(
                statut.value(),
                message);

        return ResponseEntity.status(statut).body(erreur);
    }
}