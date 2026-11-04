package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.repository.PrestataireRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// sert a remplir la liste deroulante pour affecter un incident (F7)
@Service
public class PrestataireService
{
    private final PrestataireRepository prestataireRepository;

    public PrestataireService(PrestataireRepository prestataireRepository)
    {
        this.prestataireRepository = prestataireRepository;
    }

    public List<Prestataire> listerPrestataires()
    {
        return prestataireRepository.findAllByOrderByNomAsc();
    }
}
