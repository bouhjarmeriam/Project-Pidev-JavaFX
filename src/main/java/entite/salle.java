package entite;

public class salle {
    private int id;
    private String nom;
    private int capacite;
    private String type_salle;
    private String status;
    private etage etage;
    private String image;
    private int priorite;
    public salle() {}
    public salle( String nom, int capacite, String type_salle, String image,String status  ,int priorite) {
      this.nom = nom;
      this.capacite = capacite;
      this.type_salle = type_salle;
      this.image = image;
        this.status = status;
        this.priorite = priorite;

    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getType_salle() { return type_salle; }
    public void setType_salle(String type_salle) { this.type_salle = type_salle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public etage getEtage() { return etage; }
    public void setEtage(etage etage) { this.etage = etage; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public int getPriorite() { return priorite; }
    public void setPriorite(int priorite) { this.priorite = priorite; }
}