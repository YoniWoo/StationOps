package altn72.tpfilrouge.modele;

import altn72.tpfilrouge.exception.TransitionIncidentInterditeException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "incidents")
public class Incident
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "ouvert_le", nullable = false)
    private LocalDateTime ouvertLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutIncident statut;

    @Column(name = "resolu_le")
    private LocalDateTime resoluLe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipement_id", nullable = false)
    private Equipement equipement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "declarant_id", nullable = false)
    private Utilisateur declarant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id")
    private Prestataire prestataire;

    protected Incident()
    {}

    public Incident(
            String description,
            Equipement equipement,
            Utilisateur declarant,
            LocalDateTime ouvertLe
    )
    {
        this.description = verifierDescription(description);
        this.equipement = Objects.requireNonNull(
                equipement, "Equipement obligatoire");
        this.declarant = Objects.requireNonNull(
                declarant, "Declarant obligatoire");
        this.ouvertLe = Objects.requireNonNull(
                ouvertLe, "Date ouverture obligatoire.");
        this.statut = StatutIncident.NON_AFFECTE;
    }

    public void modifierDescription(String description)
    {
        verifierNonResolu();
        this.description = verifierDescription(description);
    }

    public void affecterPrestataire(Prestataire prestataire)
    {
        verifierNonResolu();
        this.prestataire = Objects.requireNonNull(
                prestataire, "Prestataire obligatoire");
        this.statut = StatutIncident.AFFECTE;
    }

    public void resoudre(LocalDateTime dateResolution)
    {
        verifierNonResolu();

        Objects.requireNonNull(
                dateResolution, "Date de résolution obligatoire");
        if (dateResolution.isBefore(ouvertLe))
        {
            throw new TransitionIncidentInterditeException(
                    "La résolution ne peut pas précéder l'ouverture de l'incident");
        }
        this.resoluLe = dateResolution;
        this.statut = StatutIncident.RESOLU;
    }

    public boolean estResolu()
    {
        return statut == StatutIncident.RESOLU;
    }

    private void verifierNonResolu()
    {
        if (estResolu())
        {
            throw new TransitionIncidentInterditeException(
                    "L'incident est déjà résolu: il ne peut plus être modifié");
        }
    }

    private static String verifierDescription(String description)
    {
        if (description == null || description.isBlank())
        {
            throw  new IllegalArgumentException(
                    "Description obligatoire");
        }
        String descriptionClean = description.strip();

        if (descriptionClean.length() > 2000)
        {
            throw new IllegalArgumentException(
                    "La description ne doit pas dépasser 200 caractères");
        }
        return descriptionClean;
    }

    public Integer getId()
    {
        return id;
    }
    public String getDescription()
    {
        return description;
    }
    public LocalDateTime getOuvertLe()
    {
        return ouvertLe;
    }
    public StatutIncident getStatut()
    {
        return statut;
    }
    public LocalDateTime getResoluLe()
    {
        return resoluLe;
    }
    public Equipement getEquipement()
    {
        return equipement;
    }
    public Utilisateur getDeclarant()
    {
        return declarant;
    }

    public Prestataire getPrestataire() {
        return prestataire;
    }
}
