package altn72.tpfilrouge.service;

import altn72.tpfilrouge.dto.PrestataireChoixDto;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.repository.PrestataireRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// sert a remplir la liste deroulante pour affecter un incident (F7)
@Service
public class PrestataireService
{
    private final PrestataireRepository prestataireRepository;
    private final SurveillancePrestataireService surveillanceService;

    public PrestataireService(
            PrestataireRepository prestataireRepository,
            SurveillancePrestataireService surveillanceService)
    {
        this.prestataireRepository = prestataireRepository;
        this.surveillanceService = surveillanceService;
    }

    // chaque prestataire avec son statut de surveillance recalculé (PS4), la vue ajoute juste la mention
    public List<PrestataireChoixDto> listerPrestatairesPourAffectation()
    {
        List<PrestataireChoixDto> choix = new ArrayList<>();
        for (Prestataire prestataire : prestataireRepository.findAllByOrderByNomAsc())
        {
            choix.add(new PrestataireChoixDto(
                    prestataire.getId(),
                    prestataire.getNom(),
                    surveillanceService.estSousSurveillance(prestataire.getId())));
        }
        return choix;
    }
}
