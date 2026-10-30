package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.TransitionIncidentInterditeException;
import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
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
    private PrestataireRepository prestataireRepository;
    private IncidentService incidentService;

    private Equipement equipement;
    private Utilisateur agent;

    @BeforeEach
    void preparer()
    {
        incidentRepository = mock(IncidentRepository.class);
        equipementRepository = mock(EquipementRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);
        prestataireRepository = mock(PrestataireRepository.class);

        // horloge bloquée sur MAINTENANT pour savoir exactement quelle date attendre
        Clock horloge = Clock.fixed(MAINTENANT.atZone(ZONE).toInstant(), ZONE);

        incidentService = new IncidentService(
                incidentRepository,
                equipementRepository,
                utilisateurRepository,
                prestataireRepository,
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
    void modifierDescriptionRemplaceLaDescription()
    {
        Incident incident = new Incident(
                "Ascenseur bloque", equipement, agent, MAINTENANT);
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));

        incidentService.modifierDescription(1, "Ascenseur bloqué au 2e niveau");

        assertEquals("Ascenseur bloqué au 2e niveau", incident.getDescription());
    }

    @Test
    void modifierDescriptionDUnIncidentResoluEstRefuse()
    {
        Incident incident = new Incident(
                "Ascenseur immobilisé", equipement, agent, MAINTENANT);
        incident.resoudre(MAINTENANT);
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));

        assertThrows(
                TransitionIncidentInterditeException.class,
                () -> incidentService.modifierDescription(1, "Autre description")
        );
    }

    @Test
    void affecterPuisReaffecterChangeLePrestataire()
    {
        Incident incident = new Incident(
                "Portique bloqué", equipement, agent, MAINTENANT);
        Prestataire premier = new Prestataire("Mobilité Maintenance", "contact@mm.test", null);
        Prestataire second = new Prestataire("Rapid Depannage", "contact@rd.test", null);
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));
        when(prestataireRepository.findById(1)).thenReturn(Optional.of(premier));
        when(prestataireRepository.findById(2)).thenReturn(Optional.of(second));

        incidentService.affecterPrestataire(1, 1);
        incidentService.affecterPrestataire(1, 2);

        assertEquals(StatutIncident.AFFECTE, incident.getStatut());
        assertSame(second, incident.getPrestataire());
    }

    @Test
    void affecterUnIncidentResoluEstRefuse()
    {
        Incident incident = new Incident(
                "Portique bloqué", equipement, agent, MAINTENANT);
        incident.resoudre(MAINTENANT);
        Prestataire prestataire = new Prestataire("Mobilité Maintenance", "contact@mm.test", null);
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));
        when(prestataireRepository.findById(1)).thenReturn(Optional.of(prestataire));

        assertThrows(
                TransitionIncidentInterditeException.class,
                () -> incidentService.affecterPrestataire(1, 1)
        );
    }

    @Test
    void resoudreLeDernierIncidentRemetLEquipementEnService()
    {
        equipement.mettreHorsService();
        Incident incident = new Incident(
                "Ascenseur immobilisé", equipement, agent, MAINTENANT.minusHours(5));
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));
        when(incidentRepository.existsByEquipementIdAndStatutNot(any(), eq(StatutIncident.RESOLU)))
                .thenReturn(false);

        incidentService.resoudreIncident(1);

        assertEquals(StatutIncident.RESOLU, incident.getStatut());
        assertEquals(MAINTENANT, incident.getResoluLe());
        assertEquals(EtatEquipement.EN_SERVICE, equipement.getEtat());
    }

    // un autre incident est encore ouvert sur le meme equipement donc il doit rester hors service
    @Test
    void resoudreAvecUnAutreIncidentActifLaisseLEquipementHorsService()
    {
        equipement.mettreHorsService();
        Incident incident = new Incident(
                "Ascenseur immobilisé", equipement, agent, MAINTENANT.minusHours(5));
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));
        when(incidentRepository.existsByEquipementIdAndStatutNot(any(), eq(StatutIncident.RESOLU)))
                .thenReturn(true);

        incidentService.resoudreIncident(1);

        assertEquals(StatutIncident.RESOLU, incident.getStatut());
        assertEquals(EtatEquipement.HORS_SERVICE, equipement.getEtat());
    }

    @Test
    void resoudreUnIncidentDejaResoluEstRefuse()
    {
        Incident incident = new Incident(
                "Ascenseur immobilisé", equipement, agent, MAINTENANT.minusHours(5));
        incident.resoudre(MAINTENANT.minusHours(1));
        when(incidentRepository.findById(1)).thenReturn(Optional.of(incident));

        assertThrows(
                TransitionIncidentInterditeException.class,
                () -> incidentService.resoudreIncident(1)
        );
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
