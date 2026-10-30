package altn72.tpfilrouge.exception;

// levee quand on cherche un incident, equipement... avec un id qui existe pas en base
public class RessourceIntrouvableException extends RuntimeException
{
    public RessourceIntrouvableException(String message)
    {
        super(message);
    }
}
