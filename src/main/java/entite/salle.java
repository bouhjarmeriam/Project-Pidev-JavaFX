package entite;

public class salle {
    private int id;
    private String nom;
    private int capacite;
    private String type;
    private String status;
    private int priorite;
    private etage etage;
public salle() {}
    public salle( String nom, int capacite, String type, String status, int priorite){
        this.nom = nom;
        this.capacite = capacite;
        this.type = type;
        this.status = status;
        this.priorite = priorite;
    }
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
    public int getCapacite() {
        return capacite;
    }
    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getPriorite() {
        return priorite;
    }
    public void setPriorite(int priorite) {
        this.priorite = priorite;
    }
    public etage getEtage() {
        return etage;
    }
    public void setEtage(etage etage) {
        this.etage = etage;
    }
}
