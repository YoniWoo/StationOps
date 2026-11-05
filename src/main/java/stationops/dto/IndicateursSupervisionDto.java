package stationops.dto;

public record IndicateursSupervisionDto(
        long actifs,
        long affectes,
        long nonAffectes,
        long urgents
) {
}
