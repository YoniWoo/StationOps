package altn72.tpfilrouge.api;

import altn72.tpfilrouge.dto.ErreurApiDto;
import altn72.tpfilrouge.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentApiControleur {

    private final IncidentService incidentService;

    public IncidentApiControleur(IncidentService incidentService)
    {
        this.incidentService = incidentService;
    }

    @Operation(
            summary = "Résoudre un incident",
            description = """
                    Réservé au responsable connecté.
                    Récupérer un jeton via GET /api/csrf dans la même session,
                    puis le transmettre dans l’en-tête X-CSRF-TOKEN.
                    L’équipement est remis en service uniquement
                    s’il ne reste aucun incident actif.
                    """,
            parameters = @Parameter(
                    name = "X-CSRF-TOKEN",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Jeton CSRF de la session courante",
                    schema = @Schema(type = "string"))
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Incident résolu",
                    content = @Content),
            @ApiResponse(
                    responseCode = "302",
                    description = "Utilisateur non connecté : redirection vers /login",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Droits insuffisants ou jeton CSRF absent ou invalide",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErreurApiDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Incident inexistant",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErreurApiDto.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Incident déjà résolu",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErreurApiDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identifiant invalide",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErreurApiDto.class)))
    })
    @PostMapping("/{id}/resolution")
    public ResponseEntity<Void> resoudreIncident(
            @PathVariable("id") Integer incidentId)
    {
        incidentService.resoudreIncident(incidentId);

        return ResponseEntity.noContent().build();
    }
}