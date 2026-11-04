package altn72.tpfilrouge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// F6 : seule la description est modifiable donc le formulaire a un seul champ
public class ModificationIncidentForm
{
    @NotBlank(message = "La description ne peut pas être vide")
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
