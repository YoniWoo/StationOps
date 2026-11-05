package stationops.service;

import stationops.dto.IncidentHistoriqueDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExportCsvServiceTest {
    private static final LocalDateTime OUVERTURE =
            LocalDateTime.of(2026, 10, 20, 8, 0);
    private static final LocalDateTime RESOLUTION =
            LocalDateTime.of(2026, 10, 22, 17, 30);

    private final ExportCsvService exportCsvService = new ExportCsvService();

    @Test
    void premiereLigneEstLEntete()
    {
        String csv = exportCsvService.exporterHistorique(List.of());

        assertEquals("Incident;Station;Equipement;Description;Ouverture;Resolution;Prestataire;Note\n", csv);
    }

    @Test
    void uneLigneParIncidentAvecLesDatesEnFrancais()
    {
        IncidentHistoriqueDto incident = new IncidentHistoriqueDto(
                6, "Porte bloquée", "Bastille", "BAS-ASC-001",
                "Mobilité Maintenance", OUVERTURE, RESOLUTION, 4);

        String[] lignes = exportCsvService.exporterHistorique(List.of(incident)).split("\n");

        assertEquals("6;Bastille;BAS-ASC-001;Porte bloquée;20/10/2026 08:00;22/10/2026 17:30;Mobilité Maintenance;4",
                lignes[1]);
    }

    // sans ca un ; dans la description decale toutes les colonnes dans Excel
    @Test
    void uneDescriptionAvecPointVirguleEstEntreGuillemets()
    {
        IncidentHistoriqueDto incident = new IncidentHistoriqueDto(
                7, "Ecran noir; reboot \"impossible\"", "Nation", "NAT-ECR-001",
                null, OUVERTURE, RESOLUTION, null);

        String[] lignes = exportCsvService.exporterHistorique(List.of(incident)).split("\n");

        assertEquals("7;Nation;NAT-ECR-001;\"Ecran noir; reboot \"\"impossible\"\"\";20/10/2026 08:00;22/10/2026 17:30;;",
                lignes[1]);
    }
}
