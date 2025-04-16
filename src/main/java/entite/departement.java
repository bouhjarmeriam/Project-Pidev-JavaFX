package entite;

public class departement {
    private int id;
    private String nom;
    private String adresse;
    private String image;
    private int nbretage;  // Attribut pour l'affichage seulement, non lié à la base de données

    // Constructeurs
    public departement() {
        this.nbretage = 0;  // Initialisé à 0 par défaut
    }

    public departement(String nom, String adresse, String image) {
        this.nom = nom;
        this.adresse = adresse;
        this.image = image;
        this.nbretage = 0;  // Initialisé à 0 par défaut
    }

    public departement(int id, String nom, String adresse, String image) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.image = image;
        this.nbretage = 0;  // Initialisé à 0 par défaut
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

    // Getter et setter pour nbretage
    public int getNbretage() { return nbretage; }
    public void setNbretage(int nbretage) { this.nbretage = nbretage; }

    // Méthode pour augmenter le nombre d'étages
    public void ajouterEtage() {
        this.nbretage++;
    }

    // Méthode optionnelle pour diminuer le nombre d'étages
    public void supprimerEtage() {
        if (this.nbretage > 0) {
            this.nbretage--;
        }
    }
}