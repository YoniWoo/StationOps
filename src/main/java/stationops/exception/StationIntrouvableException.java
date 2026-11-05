package stationops.exception;

public class StationIntrouvableException extends RuntimeException {
    public StationIntrouvableException(Integer id)
    {
        super("La station " + id + " n'existe pas.");
    }
}
