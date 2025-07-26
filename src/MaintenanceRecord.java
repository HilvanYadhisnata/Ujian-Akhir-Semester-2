import javafx.beans.property.SimpleStringProperty;

public class MaintenanceRecord {
    private SimpleStringProperty maintenanceId;
    private SimpleStringProperty inventoryId;
    private SimpleStringProperty inventoryName;
    private SimpleStringProperty issueType;
    private SimpleStringProperty priority;
    private SimpleStringProperty reportedBy;
    private SimpleStringProperty description;
    private SimpleStringProperty reportedDate;
    private SimpleStringProperty completedDate;
    private SimpleStringProperty status;
    // PERBAIKAN: Tambahan field untuk maintenance yang lebih lengkap
    private SimpleStringProperty technician;
    private SimpleStringProperty cost;
    private SimpleStringProperty solution;
    
    public MaintenanceRecord(String maintenanceId, String inventoryId, String inventoryName,
                           String issueType, String priority, String reportedBy, String description,
                           String reportedDate, String completedDate, String status) {
        this.maintenanceId = new SimpleStringProperty(maintenanceId);
        this.inventoryId = new SimpleStringProperty(inventoryId);
        this.inventoryName = new SimpleStringProperty(inventoryName);
        this.issueType = new SimpleStringProperty(issueType);
        this.priority = new SimpleStringProperty(priority);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.description = new SimpleStringProperty(description);
        this.reportedDate = new SimpleStringProperty(reportedDate);
        this.completedDate = new SimpleStringProperty(completedDate != null ? completedDate : "");
        this.status = new SimpleStringProperty(status);
        // PERBAIKAN: Inisialisasi field tambahan
        this.technician = new SimpleStringProperty("");
        this.cost = new SimpleStringProperty("");
        this.solution = new SimpleStringProperty("");
    }
    
    // Constructor dengan field tambahan
    public MaintenanceRecord(String maintenanceId, String inventoryId, String inventoryName,
                           String issueType, String priority, String reportedBy, String description,
                           String reportedDate, String completedDate, String status,
                           String technician, String cost, String solution) {
        this(maintenanceId, inventoryId, inventoryName, issueType, priority, reportedBy, 
             description, reportedDate, completedDate, status);
        this.technician = new SimpleStringProperty(technician != null ? technician : "");
        this.cost = new SimpleStringProperty(cost != null ? cost : "");
        this.solution = new SimpleStringProperty(solution != null ? solution : "");
    }
    
    // Getters
    public String getMaintenanceId() { return maintenanceId.get(); }
    public String getInventoryId() { return inventoryId.get(); }
    public String getInventoryName() { return inventoryName.get(); }
    public String getIssueType() { return issueType.get(); }
    public String getPriority() { return priority.get(); }
    public String getReportedBy() { return reportedBy.get(); }
    public String getDescription() { return description.get(); }
    public String getReportedDate() { return reportedDate.get(); }
    public String getCompletedDate() { return completedDate.get(); }
    public String getStatus() { return status.get(); }
    public String getTechnician() { return technician.get(); }
    public String getCost() { return cost.get(); }
    public String getSolution() { return solution.get(); }
    
    // Property getters for TableView
    public SimpleStringProperty maintenanceIdProperty() { return maintenanceId; }
    public SimpleStringProperty inventoryIdProperty() { return inventoryId; }
    public SimpleStringProperty inventoryNameProperty() { return inventoryName; }
    public SimpleStringProperty issueTypeProperty() { return issueType; }
    public SimpleStringProperty priorityProperty() { return priority; }
    public SimpleStringProperty reportedByProperty() { return reportedBy; }
    public SimpleStringProperty descriptionProperty() { return description; }
    public SimpleStringProperty reportedDateProperty() { return reportedDate; }
    public SimpleStringProperty completedDateProperty() { return completedDate; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty technicianProperty() { return technician; }
    public SimpleStringProperty costProperty() { return cost; }
    public SimpleStringProperty solutionProperty() { return solution; }
    
    // Setters
    public void setMaintenanceId(String maintenanceId) { this.maintenanceId.set(maintenanceId); }
    public void setInventoryId(String inventoryId) { this.inventoryId.set(inventoryId); }
    public void setInventoryName(String inventoryName) { this.inventoryName.set(inventoryName); }
    public void setIssueType(String issueType) { this.issueType.set(issueType); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setReportedBy(String reportedBy) { this.reportedBy.set(reportedBy); }
    public void setDescription(String description) { this.description.set(description); }
    public void setReportedDate(String reportedDate) { this.reportedDate.set(reportedDate); }
    public void setCompletedDate(String completedDate) { this.completedDate.set(completedDate != null ? completedDate : ""); }
    public void setStatus(String status) { this.status.set(status); }
    public void setTechnician(String technician) { this.technician.set(technician != null ? technician : ""); }
    public void setCost(String cost) { this.cost.set(cost != null ? cost : ""); }
    public void setSolution(String solution) { this.solution.set(solution != null ? solution : ""); }
    
    // Convert to CSV format - PERBAIKAN: Tambahkan field baru
    public String toCSV() {
        return String.join(",", 
            getMaintenanceId(), 
            getInventoryId(), 
            getInventoryName(), 
            getIssueType(), 
            getPriority(), 
            getReportedBy(), 
            "\"" + getDescription().replace("\"", "\"\"") + "\"",  // Escape quotes in description
            getReportedDate(), 
            getCompletedDate(), 
            getStatus(),
            getTechnician(),
            getCost(),
            "\"" + getSolution().replace("\"", "\"\"") + "\""  // Escape quotes in solution
        );
    }
    
    // Create from CSV line - PERBAIKAN: Handle field tambahan
    public static MaintenanceRecord fromCSV(String csvLine) {
        try {
            // Handle quoted descriptions and solutions
            String[] parts = parseCSVLine(csvLine);
            if (parts.length >= 10) {
                // Backward compatibility - jika hanya ada 10 field (format lama)
                if (parts.length == 10) {
                    return new MaintenanceRecord(
                        parts[0], parts[1], parts[2], parts[3], parts[4], 
                        parts[5], parts[6], parts[7], parts[8], parts[9]
                    );
                }
                // Format baru dengan 13 field
                else if (parts.length >= 13) {
                    return new MaintenanceRecord(
                        parts[0], parts[1], parts[2], parts[3], parts[4], 
                        parts[5], parts[6], parts[7], parts[8], parts[9],
                        parts[10], parts[11], parts[12]
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing maintenance CSV line: " + csvLine);
        }
        return null;
    }
    
    // Helper method to parse CSV line with quoted fields
    private static String[] parseCSVLine(String csvLine) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < csvLine.length(); i++) {
            char c = csvLine.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < csvLine.length() && csvLine.charAt(i + 1) == '"') {
                    field.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        
        return result.toArray(new String[0]);
    }
}