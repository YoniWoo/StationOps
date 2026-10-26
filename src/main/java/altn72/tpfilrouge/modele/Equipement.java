package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

import java.lang.reflect.Type;

@Entity
@Table(name = "equipements")
public class Equipement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(nullable = false, length = 255)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TypeEquipement type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EtatEquipement etat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    protected Equipement()
    {}

    public Equipement(
            String reference,
            String libelle,
            TypeEquipement type,
            Station station
    )
    {
        this.reference = reference;
        this.libelle = libelle;
        this.type = type;
        this.station = station;
        this.etat = EtatEquipement.EN_SERVICE;
    }

    public Integer getId()
    {
        return id;
    }
    public String getReference()
    {
        return reference;
    }
    public String getLibelle()
    {
        return libelle;
    }
    public TypeEquipement getType()
    {
        return type;
    }
    public EtatEquipement getEtat()
    {
        return etat;
    }
    public Station getStation()
    {
        return station;
    }
    public void mettreHorsService()
    {
        this.etat = EtatEquipement.HORS_SERVICE;
    }
    public void remettreEnService()
    {
        this.etat = EtatEquipement.EN_SERVICE;
    }
}
