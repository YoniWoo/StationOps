package altn72.tpfilrouge.modele;


import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name= "mot_de_passe_hash", nullable = false, length = 255)
    private String motDePasseHash;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Profil profil;

    protected Utilisateur()
    {}

    public Utilisateur(
            String email,
            String motDePasseHash,
            String prenom,
            Profil profil)
    {
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.prenom = prenom;
        this.profil = profil;
    }

    public Integer getId()
    {
        return id;
    }
    public String getEmail()
    {
        return email;
    }
    public String getMotDePasseHash()
    {
        return motDePasseHash;
    }
    public String getPrenom()
    {
        return prenom;
    }
    public Profil getProfil()
    {
        return profil;
    }
}
