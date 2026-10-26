package altn72.tpfilrouge.modele;

import altn72.tpfilrouge.exception.EvaluationInterditeException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "evaluations")
public class Evaluation {
    private static final int NOTE_MAXIMALE = 5;
    private static final int NOTE_MINIMALE = 1;
    private static final int COMMENTAIRE_MAXIMALE = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;

    @Column(nullable = false)
    private int note;

    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation;

    @Column(length = COMMENTAIRE_MAXIMALE)
    private String commentaire;

    protected Evaluation()
    {}

    public Evaluation(
            Incident incident,
            int note,
            String commentaire,
            LocalDateTime dateEvaluation)
    {
        Incident incidentVerifier = Objects.requireNonNull(
                incident, "Incident obligatoire");
        LocalDateTime dateVerifier = Objects.requireNonNull(
                dateEvaluation, "Date d'évaluation obligatoire.");

        verifierIncident(incidentVerifier);
        verifierNote(note);
        verifierDate(incidentVerifier, dateVerifier);

        this.incident = incidentVerifier;
        this.note = note;
        this.commentaire = preparerCommentaire(commentaire);
        this.dateEvaluation = dateVerifier;
    }

    private static void verifierIncident(Incident incident)
    {
        if (!incident.estResolu())
        {
            throw new EvaluationInterditeException(
                    "Un incident doit être résolu avant d'être évalué.");
        }

        if (incident.getPrestataire() == null)
        {
            throw new EvaluationInterditeException(
                    "Une évaluation nécessite un prestataire associé à l'incident.");
        }
    }

    private static void verifierNote(int note)
    {
        if (note < NOTE_MINIMALE || note > NOTE_MAXIMALE)
        {
            throw new IllegalArgumentException(
                    "La note doit être comprise entre 1 et 5");
        }
    }

    private static void verifierDate(
            Incident incident,
            LocalDateTime dateEvaluation)
    {
        if (dateEvaluation.isBefore(incident.getOuvertLe()))
        {
            throw new EvaluationInterditeException(
                    "L'évaluation ne peut pas précéder la résolution d'incident.");
        }
    }

    private static String preparerCommentaire(String commentaire)
    {
        if (commentaire == null || commentaire.isBlank())
        {
            return null;
        }

        String commentaireClean = commentaire.strip();

        if (commentaireClean.length() > COMMENTAIRE_MAXIMALE)
        {
            throw new IllegalArgumentException(
                    "Le commentaire ne doit pas dépasser 2000 caractères.");
        }
        return commentaireClean;
    }

    public Integer getId()
    {
        return id;
    }

    public Incident getIncident()
    {
        return incident;
    }

    public int getNote()
    {
        return note;
    }
    public String getCommentaire()
    {
        return commentaire;
    }
    public LocalDateTime getDateEvaluation()
    {
        return dateEvaluation;
    }
}
