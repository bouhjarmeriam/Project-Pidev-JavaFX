package entite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Entretien {
    private int id;
    private int equipementId;
    private LocalDate date;
    private String description;
    private String nomEquipement;
    private LocalDateTime createdAt;

    public Entretien() {}

    public Entretien(int id, int equipementId, LocalDate date, String description, String nomEquipement, LocalDateTime createdAt) {
        this.id = id;
        this.equipementId = equipementId;
        this.date = date;
        this.description = description;
        this.nomEquipement = nomEquipement;
        this.createdAt = createdAt;
    }

    public Entretien(int equipementId, LocalDate date, String description, String nomEquipement, LocalDateTime createdAt) {
        this.equipementId = equipementId;
        this.date = date;
        this.description = description;
        this.nomEquipement = nomEquipement;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getEquipementId() {
        return equipementId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getNomEquipement() {
        return nomEquipement;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEquipementId(int equipementId) {
        this.equipementId = equipementId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNomEquipement(String nomEquipement) {
        this.nomEquipement = nomEquipement;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Affichage
    @Override
    public String toString() {
        return "Entretien{" +
                "id=" + id +
                ", equipementId=" + equipementId +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", nomEquipement='" + nomEquipement + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
