package entite;

import java.sql.Date;

public class Consultation {
    private int id;
    private int serviceId;       // For database relationship
    private String serviceName;  // For display purposes
    private Date date;
    private String patientIdentifier;
    private String status;
    private String phoneNumber;

    // Constructors
    public Consultation() {}

    public Consultation(int serviceId, Date date, String patientIdentifier,
                        String status, String phoneNumber) {
        this.serviceId = serviceId;
        this.date = date;
        this.patientIdentifier = patientIdentifier;
        this.status = status;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getPatientIdentifier() { return patientIdentifier; }
    public void setPatientIdentifier(String patientIdentifier) { this.patientIdentifier = patientIdentifier; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}