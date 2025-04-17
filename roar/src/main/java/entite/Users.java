package entite;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
@Entity
@Table(name = "user") // le nom de ta table en base
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role_type", discriminatorType = DiscriminatorType.STRING)


public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();
    private String nom;
    private String prenom;
    private String type;



    public Users() {
        this.roles = new ArrayList<>();
    }


    public Users(int id, String email, String password, List<String> roles, String nom, String prenom, String type) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.nom = nom;
        this.prenom = prenom;
        this.type = type;
    }

    public Users(int i, String dupont, String jean, String mail, String roleMedecin, String médecin) {

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", roles=" + roles +
                '}';
    }
}
