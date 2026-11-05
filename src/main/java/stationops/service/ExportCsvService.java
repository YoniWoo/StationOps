package stationops.service;

import stationops.dto.IncidentHistoriqueDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// lot alpha (piste 9) : export de l'historique en CSV pour l'ouvrir dans Excel ou LibreOffice
@Service
public class ExportCsvService
{
    // point-virgule car Excel en francais utilise la virgule pour les decimales
    private static final String SEPARATEUR = ";";
    private static final String ENTETE =
            "Incident;Station;Equipement;Description;Ouverture;Resolution;Prestataire;Note";
    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String exporterHistorique(List<IncidentHistoriqueDto> incidents)
    {
        StringBuilder csv = new StringBuilder();
        csv.append(ENTETE).append("\n");

        for (IncidentHistoriqueDto incident : incidents)
        {
            csv.append(incident.id()).append(SEPARATEUR);
            csv.append(champ(incident.stationNom())).append(SEPARATEUR);
            csv.append(champ(incident.equipementReference())).append(SEPARATEUR);
            csv.append(champ(incident.description())).append(SEPARATEUR);
            csv.append(date(incident.ouvertLe())).append(SEPARATEUR);
            csv.append(date(incident.resoluLe())).append(SEPARATEUR);
            csv.append(champ(incident.prestataireNom())).append(SEPARATEUR);
            if (incident.note() != null)
            {
                csv.append(incident.note());
            }
            csv.append("\n");
        }
        return csv.toString();
    }

    // une description peut contenir un ; ou des guillemets, dans ce cas on l'entoure de guillemets
    // et on double ceux qui sont dedans (regle du format CSV)
    private String champ(String valeur)
    {
        if (valeur == null)
        {
            return "";
        }
        if (valeur.contains(SEPARATEUR) || valeur.contains("\"") || valeur.contains("\n"))
        {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
    }

    private String date(LocalDateTime date)
    {
        if (date == null)
        {
            return "";
        }
        return date.format(FORMAT_DATE);
    }
}
