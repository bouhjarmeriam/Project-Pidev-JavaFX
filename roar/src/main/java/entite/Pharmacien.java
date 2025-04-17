package entite;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.List;
@Entity
@DiscriminatorValue("PARMACIEN")
public class Pharmacien extends Users {
    private String telephone;


    public Pharmacien() {
        super();
    }


    public Pharmacien(int id, String email, String password, List<String> roles,String type,
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
        return "Pharmacien{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", roles=" + getRoles() +
                ", telephone='" + telephone + '\'' +
                '}';
    }

}
