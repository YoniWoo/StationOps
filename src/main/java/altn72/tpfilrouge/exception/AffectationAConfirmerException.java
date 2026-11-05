package altn72.tpfilrouge.exception;

// levee quand on affecte un prestataire sous surveillance sans avoir confirmé (PS5)
// on garde l'id du prestataire pour pouvoir reproposer la meme affectation a confirmer
public class AffectationAConfirmerException extends RuntimeException
{
    private final Integer prestataireId;

    public AffectationAConfirmerException(Integer prestataireId, String message)
    {
        super(message);
        this.prestataireId = prestataireId;
    }

    public Integer getPrestataireId()
    {
        return prestataireId;
    }
}
