package altn72.tpfilrouge.api;

import altn72.tpfilrouge.dto.EquipementResumeDto;
import altn72.tpfilrouge.dto.FicheEquipementDto;
import altn72.tpfilrouge.dto.StationDto;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.StationService;
import altn72.tpfilrouge.dto.ErreurApiDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConsultationApiControleur {

    private final StationService stationService;
    private final EquipementService equipementService;

    public ConsultationApiControleur(
            StationService stationService,
            EquipementService equipementService)
    {
        this.stationService = stationService;
        this.equipementService = equipementService;
    }
    @Operation(
            summary = "Lister les stations",
            description = "Accessible aux deux profils. Tri par nom croissant.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stations triées par nom croissant",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = StationDto.class)))),
            @ApiResponse(
                    responseCode = "302",
                    description = "Utilisateur non connecté : redirection vers /login",
                    content = @Content)
    })
    @GetMapping("/stations")
    public List<StationDto> listerStations()
    {
        return stationService.listerStations()
                .stream()
                .map(station -> new StationDto(
                        station.getId(),
                        station.getNom()))
                .toList();
    }

    @Operation(
            summary = "Lister les équipements d’une station",
            description = """
                Accessible aux deux profils. Tri par référence croissante.
                Une station sans équipement retourne une liste vide.
                Une station inexistante retourne 404.
                """)
    @GetMapping("/stations/{id}/equipements")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                        Équipements triés par référence croissante.
                        Tableau vide si la station ne possède aucun équipement.
                        """,
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = EquipementResumeDto.class)))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identifiant invalide",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErreurApiDto.class))),
            @ApiResponse(
                    responseCode = "302",
                    description = "Utilisateur non connecté : redirection vers /login",
                    content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Station inexistante",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErreurApiDto.class)))
    })
    public List<EquipementResumeDto> listerEquipements(
            @PathVariable("id") Integer stationId)
    {
        return equipementService.listerParStation(stationId)
                .stream()
                .map(equipement -> new EquipementResumeDto(
                        equipement.getId(),
                        equipement.getReference(),
                        equipement.getLibelle(),
                        equipement.getType(),
                        equipement.getEtat()))
                .toList();
    }

    @Operation(
            summary = "Consulter une fiche équipement",
            description = """
                Accessible aux deux profils.
                Inclut les incidents triés par ouverture décroissante.
                Un équipement inexistant retourne 404.
                """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                        Fiche équipement avec ses incidents
                        triés par date d’ouverture décroissante.
                        """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = FicheEquipementDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identifiant invalide",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErreurApiDto.class))),
            @ApiResponse(
                    responseCode = "302",
                    description = "Utilisateur non connecté : redirection vers /login",
                    content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Équipement inexistant",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErreurApiDto.class)))
    })
    @GetMapping("/equipements/{id}")
    public FicheEquipementDto consulterEquipement(
            @PathVariable("id") Integer equipementId)
    {
        return equipementService.consulterFiche(equipementId);
    }
}