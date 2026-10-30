package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IncidentServiceTest {
    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT =
            LocalDateTime.of(2026, 10, 30, 10, 0);

    private IncidentRepository incidentRepository;
    private EquipementRepository equipementRepository;
    private UtilisateurRepository utilisateurRepository;
    private IncidentService incidentService;

    private Equipement equipement;
    private Utilisateur agent;

    @BeforeEach
    void preparer()
    {
        incidentRepository = mock(IncidentRepository.class);
        equipementRepository = mock(EquipementRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);

        // horloge bloquée sur MAINTENANT pour savoir exactement quelle date attendre
        Clock horloge = Clock.fixed(MAINTENANT.atZone(ZONE).toInstant(), ZONE);

        incidentService = new IncidentService(
                incidentRepository,
                equipementRepository,
                utilisateurRepository,
                horloge);

        equipement = new Equipement(
                "REP-ASC-001", "Ascenseur accès principal",
                TypeEquipement.ASCENSEUR, new Station("République"));
        agent = new Utilisateur(
                "agent@stationops.test", "hash-de-test",
                "Agent", Profil.AGENT_TERRAIN);
    }

    @Test
    void declarerIncidentCreeUnIncidentNonAffecteEtMetLEquipementHorsService()
    {
        when(equipementRepository.findById(1)).thenReturn(Optional.of(equipement));
        when(utilisateurRepository.findById(2)).thenReturn(Optional.of(agent));

        Incident incident = incidentService.declarerIncident(
                1, "Ascenseur immobilisé", 2);

        assertEquals(StatutIncident.NON_AFFECTE, incident.getStatut());
        assertNull(incident.getPrestataire());
        assertEquals(MAINTENANT, incident.getOuvertLe());
        assertEquals(EtatEquipement.HORS_SERVICE, equipement.getEtat());
        verify(incidentRepository).save(incident);
    }

    @Test
    void declarerIncidentSurUnEquipementInexistantEstRefuse()
    {
        when(equipementRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> incidentService.declarerIncident(99, "Panne", 2)
        );
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void trouverUnIncidentInexistantLeveUneException()
    {
        when(incidentRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> incidentService.trouverIncident(99)
        );
    }
}
