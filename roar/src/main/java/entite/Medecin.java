package entite;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;
@Entity
@DiscriminatorValue("MEDECIN")

public class Medecin extends Users{
    private String specialite;
    private String telephone;

    public Medecin() {
        super();
    }

    public Medecin(int id, String email, String password, List<String> roles, String nom, String prenom,String type,
                   String specialite, String telephone) {
        super(id, email, password, roles, nom, prenom,type);
        this.specialite = specialite;
        this.telephone = telephone;
    }


    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }


    @Override
    public String toString() {
        return "Medecin{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", roles=" + getRoles() +
                ", specialite='" + specialite + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }

}
