package entite;

public class etage {
    private int id;
    private int numero;
    private entite.departement departement;
    public etage() {}
    public etage(int id, int numero, entite.departement departement) {
        this.id = id;
        this.numero = numero;
        this.departement = departement;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getNumero() {
        return numero;
    }
    public void setNumero(int numero) {
        this.numero = numero;
    }
    public entite.departement getDepartement() {
        return departement;
    }
    public void setDepartement(entite.departement departement) {
        this.departement = departement;
    }

}
