package entite;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.List;
@Entity
@DiscriminatorValue("STAFF")
public class Staff extends Users{
    private String telephone;
    public Staff() {
        super();
    }

    public Staff(int id, String email, String password, List<String> roles,String type,
                 String nom, String prenom, String telephone) {
        super(id, email, password, roles, nom, prenom, type);
        this.telephone = telephone;
    }


    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }


    @Override
    public String toString() {
        return "Staff{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", roles=" + getRoles() +
                ", telephone='" + telephone + '\'' +
                '}';
    }

}
