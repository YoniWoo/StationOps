package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "stations")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private  String nom;

    protected Station()
    {}

    public Station(String nom)
    {
        this.nom = nom;
    }

    public Integer getId()
    {
        return id;
    }

    public String getNom()
    {
        return nom;
    }
}
