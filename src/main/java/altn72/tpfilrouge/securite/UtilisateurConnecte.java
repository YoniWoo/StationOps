package altn72.tpfilrouge.securite;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class UtilisateurConnecte extends User {
    private final Integer id;
    private final String prenom;
    private final Profil profil;

    public UtilisateurConnecte(Utilisateur utilisateur)
    {
        super(
                utilisateur.getEmail(),
                utilisateur.getMotDePasseHash(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + utilisateur.getProfil().name()
                ))
        );
        this.id = utilisateur.getId();
        this.prenom = utilisateur.getPrenom();
        this.profil = utilisateur.getProfil();
    }

    public Integer getId()
    {
        return id;
    }
    public String getPrenom()
    {
        return prenom;
    }
    public Profil getProfil()
    {
        return profil;
    }

    public String getLibelleProfil()
    {
        return switch (profil)
        {
            case RESPONSABLE_EXPLOITATION -> "Responsable d'exploitation";
            case AGENT_TERRAIN -> "Agent de terrain";
        };
    }
}
