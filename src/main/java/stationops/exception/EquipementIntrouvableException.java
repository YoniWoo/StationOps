package stationops.exception;

public class EquipementIntrouvableException extends RuntimeException {
    public EquipementIntrouvableException(Integer id) {
        super("l'équipement " + id + " n'existe pas");
    }
}
