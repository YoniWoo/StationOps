package stationops.dto;

// une ligne de la liste deroulante d'affectation (F7 + evolution sous surveillance)
public record PrestataireChoixDto(
        Integer id,
        String nom,
        boolean sousSurveillance) {
}
