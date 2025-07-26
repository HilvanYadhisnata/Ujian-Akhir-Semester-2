import javafx.beans.property.SimpleStringProperty;

public class BorrowRecord {
    private SimpleStringProperty borrowId;
    private SimpleStringProperty inventoryId;
    private SimpleStringProperty inventoryName;
    private SimpleStringProperty borrowerName;
    private SimpleStringProperty borrowerType;
    private SimpleStringProperty borrowerContact;
    private SimpleStringProperty borrowDate;
    private SimpleStringProperty returnDate;
    private SimpleStringProperty actualReturnDate;
    private SimpleStringProperty status;
    private SimpleStringProperty notes;
    
    public BorrowRecord(String borrowId, String inventoryId, String inventoryName,
                       String borrowerName, String borrowerType, String borrowerContact,
                       String borrowDate, String returnDate, String actualReturnDate,
                       String status, String notes) {
        this.borrowId = new SimpleStringProperty(borrowId);
        this.inventoryId = new SimpleStringProperty(inventoryId);
        this.inventoryName = new SimpleStringProperty(inventoryName);
        this.borrowerName = new SimpleStringProperty(borrowerName);
        this.borrowerType = new SimpleStringProperty(borrowerType);
        this.borrowerContact = new SimpleStringProperty(borrowerContact);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.returnDate = new SimpleStringProperty(returnDate);
        this.actualReturnDate = new SimpleStringProperty(actualReturnDate != null ? actualReturnDate : "");
        this.status = new SimpleStringProperty(status);
        this.notes = new SimpleStringProperty(notes != null ? notes : "");
    }
    
    // Getters
    public String getBorrowId() { return borrowId.get(); }
    public String getInventoryId() { return inventoryId.get(); }
    public String getInventoryName() { return inventoryName.get(); }
    public String getBorrowerName() { return borrowerName.get(); }
    public String getBorrowerType() { return borrowerType.get(); }
    public String getBorrowerContact() { return borrowerContact.get(); }
    public String getBorrowDate() { return borrowDate.get(); }
    public String getReturnDate() { return returnDate.get(); }
    public String getActualReturnDate() { return actualReturnDate.get(); }
    public String getStatus() { return status.get(); }
    public String getNotes() { return notes.get(); }
    
    // Property getters for TableView
    public SimpleStringProperty borrowIdProperty() { return borrowId; }
    public SimpleStringProperty inventoryIdProperty() { return inventoryId; }
    public SimpleStringProperty inventoryNameProperty() { return inventoryName; }
    public SimpleStringProperty borrowerNameProperty() { return borrowerName; }
    public SimpleStringProperty borrowerTypeProperty() { return borrowerType; }
    public SimpleStringProperty borrowerContactProperty() { return borrowerContact; }
    public SimpleStringProperty borrowDateProperty() { return borrowDate; }
    public SimpleStringProperty returnDateProperty() { return returnDate; }
    public SimpleStringProperty actualReturnDateProperty() { return actualReturnDate; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty notesProperty() { return notes; }
    
    // Setters
    public void setBorrowId(String borrowId) { this.borrowId.set(borrowId); }
    public void setInventoryId(String inventoryId) { this.inventoryId.set(inventoryId); }
    public void setInventoryName(String inventoryName) { this.inventoryName.set(inventoryName); }
    public void setBorrowerName(String borrowerName) { this.borrowerName.set(borrowerName); }
    public void setBorrowerType(String borrowerType) { this.borrowerType.set(borrowerType); }
    public void setBorrowerContact(String borrowerContact) { this.borrowerContact.set(borrowerContact); }
    public void setBorrowDate(String borrowDate) { this.borrowDate.set(borrowDate); }
    public void setReturnDate(String returnDate) { this.returnDate.set(returnDate); }
    public void setActualReturnDate(String actualReturnDate) { this.actualReturnDate.set(actualReturnDate != null ? actualReturnDate : ""); }
    public void setStatus(String status) { this.status.set(status); }
    public void setNotes(String notes) { this.notes.set(notes != null ? notes : ""); }
    
    // Convert to CSV format
    public String toCSV() {
        return String.join(",", 
            getBorrowId(), 
            getInventoryId(), 
            "\"" + getInventoryName().replace("\"", "\"\"") + "\"",
            "\"" + getBorrowerName().replace("\"", "\"\"") + "\"",
            getBorrowerType(), 
            "\"" + getBorrowerContact().replace("\"", "\"\"") + "\"",
            getBorrowDate(), 
            getReturnDate(), 
            getActualReturnDate(), 
            getStatus(),
            "\"" + getNotes().replace("\"", "\"\"") + "\""
        );
    }
    
    // Create from CSV line
    public static BorrowRecord fromCSV(String csvLine) {
        try {
            String[] parts = parseCSVLine(csvLine);
            if (parts.length >= 11) {
                return new BorrowRecord(
                    parts[0], parts[1], parts[2], parts[3], parts[4], 
                    parts[5], parts[6], parts[7], parts[8], parts[9], parts[10]
                );
            }
        } catch (Exception e) {
            System.err.println("Error parsing borrow CSV line: " + csvLine);
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