package stationops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// champs du formulaire F4, les messages s'affichent sous les champs si c'est pas valide
public class DeclarationIncidentForm
{
    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipementId;

    @NotBlank(message = "Décrivez la panne en quelques mots")
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    public Integer getEquipementId()
    {
        return equipementId;
    }

    public void setEquipementId(Integer equipementId)
    {
        this.equipementId = equipementId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
