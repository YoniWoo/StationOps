package stationops.dto;

public record ErreurApiDto(
        int statut,
        String message
) {
}
