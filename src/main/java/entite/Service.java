package entite;

/**
 * Represents a basic medical service entity
 */
public class Service {
    private int id;
    private String name;
    private String description;
    private int duration; // Using int to store duration (e.g., 90 for 90 minutes)

    // Default constructor
    public Service() {
    }

    // Parameterized constructor
    public Service(String name, String description, int duration) {
        this.name = name;
        this.description = description;
        this.duration = duration; // Duration is now an int
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration; // Set duration as an int
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", duration=" + duration + " minutes" + // Display duration in minutes
                '}';
    }
}
