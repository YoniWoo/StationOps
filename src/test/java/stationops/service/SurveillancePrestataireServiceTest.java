package stationops.service;

import stationops.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// les 7 exemples de reference de l'enoncé, ecrits de la plus ancienne a la plus récente comme dans le sujet
public class SurveillancePrestataireServiceTest {
    private EvaluationRepository evaluationRepository;
    private SurveillancePrestataireService surveillanceService;

    @BeforeEach
    void preparer()
    {
        evaluationRepository = mock(EvaluationRepository.class);
        surveillanceService = new SurveillancePrestataireService(evaluationRepository);
    }

    // le sujet donne les notes de la plus ancienne a la plus récente, le service les attend dans l'autre sens
    private Double moyenne(Integer... notesDeLaPlusAncienne)
    {
        List<Integer> notes = new ArrayList<>(List.of(notesDeLaPlusAncienne));
        Collections.reverse(notes);
        return surveillanceService.calculerMoyenneDesNotes(notes);
    }

    @Test
    void exemple1MoinsDe5EvaluationsPasDeMoyenne()
    {
        Double moyenne = moyenne(4, 1, 1, 1);

        assertNull(moyenne);
        assertFalse(surveillanceService.estSousSurveillance(moyenne));
    }

    @Test
    void exemple2MoyenneDe3PileNestPasSurveille()
    {
        Double moyenne = moyenne(3, 3, 3, 3, 3);

        assertEquals(3.0, moyenne);
        assertFalse(surveillanceService.estSousSurveillance(moyenne));
    }

    @Test
    void exemple3LaDerniereNoteFaitPasserSous3()
    {
        Double moyenne = moyenne(3, 3, 3, 3, 3, 2);

        assertEquals(2.8, moyenne, 0.0001);
        assertTrue(surveillanceService.estSousSurveillance(moyenne));
    }

    @Test
    void exemple4LaFenetreGlisseEtLePrestataireSort()
    {
        Double moyenne = moyenne(3, 3, 3, 3, 3, 2, 5);

        assertEquals(3.2, moyenne, 0.0001);
        assertFalse(surveillanceService.estSousSurveillance(moyenne));
    }

    @Test
    void exemple5UnVieux1NeComptePlus()
    {
        Double moyenne = moyenne(1, 5, 5, 5, 5, 5);

        assertEquals(5.0, moyenne);
        assertFalse(surveillanceService.estSousSurveillance(moyenne));
    }

    @Test
    void exemple6Moyenne2Virgule8EstSurveille()
    {
        Double moyenne = moyenne(2, 3, 3, 3, 3);

        assertEquals(2.8, moyenne, 0.0001);
        assertTrue(surveillanceService.estSousSurveillance(moyenne));
    }

    @Test
    void exemple7Moyenne3Virgule4NestPasSurveille()
    {
        Double moyenne = moyenne(1, 1, 5, 5, 5);

        assertEquals(3.4, moyenne, 0.0001);
        assertFalse(surveillanceService.estSousSurveillance(moyenne));
    }

    // verifie que le service demande bien au plus 5 notes au repository
    @Test
    void leServiceDemandeLaFenetreAuRepository()
    {
        when(evaluationRepository.trouverDernieresNotes(eq(7), any()))
                .thenReturn(List.of(2, 3, 3, 3, 3));

        assertTrue(surveillanceService.estSousSurveillance(7));
        verify(evaluationRepository).trouverDernieresNotes(
                eq(7),
                eq(PageRequest.of(0, SurveillancePrestataireService.TAILLE_FENETRE)));
    }
}
