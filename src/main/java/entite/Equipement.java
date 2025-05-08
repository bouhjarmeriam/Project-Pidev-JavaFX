package entite;

public class Equipement {
    private int id;
    private String nom;
    private String type;
    private String statut;
    private String category;


    // Constructeurs
    public Equipement() {}

    public Equipement(int id, String nom, String type, String statut, String category) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.statut = statut;
        this.category = category;
    }

    public Equipement(String nom, String type, String statut, String category) {
        this.nom = nom;
        this.type = type;
        this.statut = statut;
        this.category = category;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // toString pour affichage (facultatif)
    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", statut='" + statut + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
