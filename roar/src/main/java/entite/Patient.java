package entite;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;
@Entity
@DiscriminatorValue("PATIENT")

public class Patient extends Users {
    private String adresse;
    private String telephone;
    private LocalDate dateNaissance;


    public Patient() {
        super();
    }


    public Patient(int id, String email, String password, List<String> roles,String type,
                   String nom, String prenom, String adresse, String telephone, LocalDate dateNaissance) {
        super(id, email, password, roles, nom, prenom, type);
        this.adresse = adresse;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
    }


    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }


    @Override
    public String toString() {
        return "Patient{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", roles=" + getRoles() +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", dateNaissance=" + dateNaissance +
                '}';
    }
}
