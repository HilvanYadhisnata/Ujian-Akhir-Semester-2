import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowRecord {
    private SimpleStringProperty borrowId;
    private SimpleStringProperty inventoryId;
    private SimpleStringProperty inventoryName;
    private SimpleStringProperty borrowerName;
    private SimpleStringProperty borrowerType; // Mahasiswa/Dosen/Staff
    private SimpleStringProperty borrowDate;
    private SimpleStringProperty returnDate; // Expected return date
    private SimpleStringProperty actualReturnDate; // Actual return date (null if not returned)
    private SimpleStringProperty status; // Dipinjam/Dikembalikan/Terlambat
    private SimpleStringProperty notes;
    
    public BorrowRecord(String borrowId, String inventoryId, String inventoryName, 
                       String borrowerName, String borrowerType, String borrowDate, 
                       String returnDate, String actualReturnDate, String status, String notes) {
        this.borrowId = new SimpleStringProperty(borrowId);
        this.inventoryId = new SimpleStringProperty(inventoryId);
        this.inventoryName = new SimpleStringProperty(inventoryName);
        this.borrowerName = new SimpleStringProperty(borrowerName);
        this.borrowerType = new SimpleStringProperty(borrowerType);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.returnDate = new SimpleStringProperty(returnDate);
        this.actualReturnDate = new SimpleStringProperty(actualReturnDate);
        this.status = new SimpleStringProperty(status);
        this.notes = new SimpleStringProperty(notes);
    }
    
    // Getters
    public String getBorrowId() { return borrowId.get(); }
    public String getInventoryId() { return inventoryId.get(); }
    public String getInventoryName() { return inventoryName.get(); }
    public String getBorrowerName() { return borrowerName.get(); }
    public String getBorrowerType() { return borrowerType.get(); }
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
    public void setBorrowDate(String borrowDate) { this.borrowDate.set(borrowDate); }
    public void setReturnDate(String returnDate) { this.returnDate.set(returnDate); }
    public void setActualReturnDate(String actualReturnDate) { this.actualReturnDate.set(actualReturnDate); }
    public void setStatus(String status) { this.status.set(status); }
    public void setNotes(String notes) { this.notes.set(notes); }
    
    // Check if overdue
    public boolean isOverdue() {
        if ("Dipinjam".equals(getStatus()) && getReturnDate() != null) {
            try {
                LocalDate returnDate = LocalDate.parse(getReturnDate());
                return LocalDate.now().isAfter(returnDate);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    // Convert to CSV format
    public String toCSV() {
        return String.join(",", 
            getBorrowId() != null ? getBorrowId() : "",
            getInventoryId() != null ? getInventoryId() : "",
            getInventoryName() != null ? getInventoryName() : "",
            getBorrowerName() != null ? getBorrowerName() : "",
            getBorrowerType() != null ? getBorrowerType() : "",
            getBorrowDate() != null ? getBorrowDate() : "",
            getReturnDate() != null ? getReturnDate() : "",
            getActualReturnDate() != null ? getActualReturnDate() : "",
            getStatus() != null ? getStatus() : "",
            getNotes() != null ? getNotes() : ""
        );
    }
    
    // Create from CSV line
    public static BorrowRecord fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", -1); // -1 to keep empty strings
        if (parts.length >= 10) {
            return new BorrowRecord(
                parts[0], parts[1], parts[2], parts[3], parts[4],
                parts[5], parts[6], parts[7], parts[8], parts[9]
            );
        }
        return null;
    }
}