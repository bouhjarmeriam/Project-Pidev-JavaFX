package entite;

public class departement {
    private int id;
    private String nom;
    private String adresse;
    private String image;

    // Constructeurs
    public departement() {}

    public departement(String nom, String adresse, String image) {
        this.nom = nom;
        this.adresse = adresse;
        this.image = image;
    }

    public departement(int id, String nom, String adresse, String image) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.image = image;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}