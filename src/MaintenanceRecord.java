import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

public class MaintenanceRecord {
    private SimpleStringProperty maintenanceId;
    private SimpleStringProperty inventoryId;
    private SimpleStringProperty inventoryName;
    private SimpleStringProperty issueType; // Kerusakan/Maintenance Rutin
    private SimpleStringProperty description;
    private SimpleStringProperty reportedBy;
    private SimpleStringProperty reportedDate;
    private SimpleStringProperty priority; // Rendah/Sedang/Tinggi/Urgent
    private SimpleStringProperty status; // Dilaporkan/Dalam Perbaikan/Selesai/Ditunda
    private SimpleStringProperty technician;
    private SimpleStringProperty completedDate;
    private SimpleStringProperty cost;
    private SimpleStringProperty notes;
    
    public MaintenanceRecord(String maintenanceId, String inventoryId, String inventoryName,
                           String issueType, String description, String reportedBy, String reportedDate,
                           String priority, String status, String technician, String completedDate,
                           String cost, String notes) {
        this.maintenanceId = new SimpleStringProperty(maintenanceId);
        this.inventoryId = new SimpleStringProperty(inventoryId);
        this.inventoryName = new SimpleStringProperty(inventoryName);
        this.issueType = new SimpleStringProperty(issueType);
        this.description = new SimpleStringProperty(description);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.reportedDate = new SimpleStringProperty(reportedDate);
        this.priority = new SimpleStringProperty(priority);
        this.status = new SimpleStringProperty(status);
        this.technician = new SimpleStringProperty(technician);
        this.completedDate = new SimpleStringProperty(completedDate);
        this.cost = new SimpleStringProperty(cost);
        this.notes = new SimpleStringProperty(notes);
    }
    
    // Getters
    public String getMaintenanceId() { return maintenanceId.get(); }
    public String getInventoryId() { return inventoryId.get(); }
    public String getInventoryName() { return inventoryName.get(); }
    public String getIssueType() { return issueType.get(); }
    public String getDescription() { return description.get(); }
    public String getReportedBy() { return reportedBy.get(); }
    public String getReportedDate() { return reportedDate.get(); }
    public String getPriority() { return priority.get(); }
    public String getStatus() { return status.get(); }
    public String getTechnician() { return technician.get(); }
    public String getCompletedDate() { return completedDate.get(); }
    public String getCost() { return cost.get(); }
    public String getNotes() { return notes.get(); }
    
    // Property getters for TableView
    public SimpleStringProperty maintenanceIdProperty() { return maintenanceId; }
    public SimpleStringProperty inventoryIdProperty() { return inventoryId; }
    public SimpleStringProperty inventoryNameProperty() { return inventoryName; }
    public SimpleStringProperty issueTypeProperty() { return issueType; }
    public SimpleStringProperty descriptionProperty() { return description; }
    public SimpleStringProperty reportedByProperty() { return reportedBy; }
    public SimpleStringProperty reportedDateProperty() { return reportedDate; }
    public SimpleStringProperty priorityProperty() { return priority; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty technicianProperty() { return technician; }
    public SimpleStringProperty completedDateProperty() { return completedDate; }
    public SimpleStringProperty costProperty() { return cost; }
    public SimpleStringProperty notesProperty() { return notes; }
    
    // Setters
    public void setMaintenanceId(String maintenanceId) { this.maintenanceId.set(maintenanceId); }
    public void setInventoryId(String inventoryId) { this.inventoryId.set(inventoryId); }
    public void setInventoryName(String inventoryName) { this.inventoryName.set(inventoryName); }
    public void setIssueType(String issueType) { this.issueType.set(issueType); }
    public void setDescription(String description) { this.description.set(description); }
    public void setReportedBy(String reportedBy) { this.reportedBy.set(reportedBy); }
    public void setReportedDate(String reportedDate) { this.reportedDate.set(reportedDate); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setStatus(String status) { this.status.set(status); }
    public void setTechnician(String technician) { this.technician.set(technician); }
    public void setCompletedDate(String completedDate) { this.completedDate.set(completedDate); }
    public void setCost(String cost) { this.cost.set(cost); }
    public void setNotes(String notes) { this.notes.set(notes); }
    
    // Convert to CSV format
    public String toCSV() {
        return String.join(",",
            escapeCSV(getMaintenanceId()),
            escapeCSV(getInventoryId()),
            escapeCSV(getInventoryName()),
            escapeCSV(getIssueType()),
            escapeCSV(getDescription()),
            escapeCSV(getReportedBy()),
            escapeCSV(getReportedDate()),
            escapeCSV(getPriority()),
            escapeCSV(getStatus()),
            escapeCSV(getTechnician()),
            escapeCSV(getCompletedDate()),
            escapeCSV(getCost()),
            escapeCSV(getNotes())
        );
    }
    
    // Create from CSV line
    public static MaintenanceRecord fromCSV(String csvLine) {
        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length >= 13) {
            for (int i = 0; i < parts.length; i++) {
                parts[i] = unescapeCSV(parts[i]);
            }
            return new MaintenanceRecord(
                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                parts[6], parts[7], parts[8], parts[9], parts[10], parts[11], parts[12]
            );
        }
        return null;
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private static String unescapeCSV(String value) {
        if (value == null) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }
}