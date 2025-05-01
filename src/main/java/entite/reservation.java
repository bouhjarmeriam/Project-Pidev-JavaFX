package entite;

public class reservation {
    private int id;
    private int date_debut;
    private int date_fin;
    private salle salle;

    public reservation() {}
    public reservation(int id, int date_debut, int date_fin, salle salle) {
        this.id = id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.salle = salle;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getDate_debut() {
        return date_debut;
    }
    public void setDate_debut(int date_debut) {
        this.date_debut = date_debut;
    }
    public int getDate_fin() {
        return date_fin;
    }
    public void setDate_fin(int date_fin) {
        this.date_fin = date_fin;
    }
    public salle getSalle() {
        return salle;
    }
    public void setSalle(salle salle) {
        this.salle = salle;
    }
}
