package altn72.tpfilrouge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// F10 : note de 1 a 5 obligatoire, commentaire facultatif
public class EvaluationForm
{
    @NotNull(message = "Choisissez une note")
    @Min(value = 1, message = "La note doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5")
    private Integer note;

    @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
    private String commentaire;

    public Integer getNote()
    {
        return note;
    }

    public void setNote(Integer note)
    {
        this.note = note;
    }

    public String getCommentaire()
    {
        return commentaire;
    }

    public void setCommentaire(String commentaire)
    {
        this.commentaire = commentaire;
    }
}
