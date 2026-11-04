package altn72.tpfilrouge.api;

import altn72.tpfilrouge.dto.ErreurApiDto;
import altn72.tpfilrouge.dto.IncidentDetailDto;
import altn72.tpfilrouge.dto.IncidentSupervisionDto;
import altn72.tpfilrouge.service.IncidentService;
import altn72.tpfilrouge.service.SupervisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentApiControleur {
    private final IncidentService incidentService;
    private final SupervisionService supervisionService;

    public IncidentApiControleur(
            IncidentService incidentService,
            SupervisionService supervisionService)
    {
        this.incidentService = incidentService;
        this.supervisionService = supervisionService;
    }

    // F12 : on reutilise le meme calcul que le tableau de bord, donc la meme urgence (RG3, RG4)
    @Operation(
            summary = "Lister les incidents actifs",
            description = """
                    Accessible aux deux profils. Incidents non résolus avec leur
                    ancienneté en heures et leur niveau d’urgence calculé
                    à partir de l’heure courante.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incidents actifs",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = IncidentSupervisionDto.class)))),
            @ApiResponse(
                    responseCode = "302",
                    description = "Utilisateur non connecté : redirection vers /login",
                    content = @Content)
    })
    @GetMapping
    public List<IncidentSupervisionDto> listerIncidentsActifs()
    {
        return supervisionService.listerIncidentsActifs(null, null);
    }

    // F12 : un id qui existe pas donne un 404 en JSON (voir GestionErreurApi)
    @Operation(
            summary = "Consulter le détail d’un incident",
            description = "Accessible aux deux profils. Fonctionne aussi pour un incident résolu.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Détail de l’incident",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentDetailDto.class))),
            @ApiResponse(
                    responseCode = "302",
                    description = "Utilisateur non connecté : redirection vers /login",
                    content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Incident inexistant",
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
    @GetMapping("/{id}")
    public IncidentDetailDto consulterIncident(@PathVariable("id") Integer incidentId)
    {
        return incidentService.consulterDetail(incidentId);
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