package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "prestataires")
public class Prestataire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 30)
    private String telephone;

    protected  Prestataire()
    {}

    public Prestataire(String nom, String email, String telephone)
    {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
    }

    public Integer getId()
    {
        return id;
    }
    public  String getNom()
    {
        return nom;
    }
    public String getEmail()
    {
        return email;
    }
    public String getTelephone()
    {
        return telephone;
    }
}
