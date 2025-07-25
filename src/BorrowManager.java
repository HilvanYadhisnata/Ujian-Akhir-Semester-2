import java.io.*;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowManager {
    private static final String BORROW_FILE = "borrowing.csv";
    private static final String BORROW_HEADER = "Borrow ID,Inventory ID,Inventory Name,Borrower Name,Borrower Type,Borrow Date,Return Date,Actual Return Date,Status,Notes";
    
    // Save borrowing data
    public static void saveBorrowData(ObservableList<BorrowRecord> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BORROW_FILE))) {
            writer.println(BORROW_HEADER);
            for (BorrowRecord record : data) {
                writer.println(record.toCSV());
            }
            System.out.println("Data peminjaman berhasil disimpan ke " + BORROW_FILE);
        } catch (IOException e) {
            System.err.println("Error saving borrow data: " + e.getMessage());
        }
    }
    
    // Load borrowing data
    public static ObservableList<BorrowRecord> loadBorrowData() {
        ObservableList<BorrowRecord> data = FXCollections.observableArrayList();
        File file = new File(BORROW_FILE);
        
        if (!file.exists()) {
            return data; // Return empty list if file doesn't exist
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(BORROW_FILE))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                BorrowRecord record = BorrowRecord.fromCSV(line);
                if (record != null) {
                    data.add(record);
                }
            }
            System.out.println("Data peminjaman berhasil dimuat dari " + BORROW_FILE);
        } catch (IOException e) {
            System.err.println("Error loading borrow data: " + e.getMessage());
        }
        
        return data;
    }
    
    // Generate new borrow ID
    public static String generateNewBorrowId(ObservableList<BorrowRecord> data) {
        int maxId = 0;
        String prefix = "BR";
        
        for (BorrowRecord record : data) {
            String id = record.getBorrowId();
            if (id != null && id.startsWith(prefix)) {
                try {
                    int numId = Integer.parseInt(id.substring(prefix.length()));
                    maxId = Math.max(maxId, numId);
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
        }
        
        return String.format("%s%04d", prefix, maxId + 1);
    }
    
    // Get active borrowing for an inventory item
    public static BorrowRecord getActiveBorrowingForItem(ObservableList<BorrowRecord> data, String inventoryId) {
        return data.stream()
            .filter(record -> inventoryId.equals(record.getInventoryId()) && "Dipinjam".equals(record.getStatus()))
            .findFirst()
            .orElse(null);
    }
    
    // Check if inventory item is currently borrowed
    public static boolean isItemBorrowed(ObservableList<BorrowRecord> data, String inventoryId) {
        return getActiveBorrowingForItem(data, inventoryId) != null;
    }
    
    // Get overdue items
    public static List<BorrowRecord> getOverdueItems(ObservableList<BorrowRecord> data) {
        List<BorrowRecord> overdueItems = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (BorrowRecord record : data) {
            if ("Dipinjam".equals(record.getStatus()) && record.getReturnDate() != null) {
                try {
                    LocalDate returnDate = LocalDate.parse(record.getReturnDate());
                    if (today.isAfter(returnDate)) {
                        // Update status to overdue
                        record.setStatus("Terlambat");
                        overdueItems.add(record);
                    }
                } catch (Exception e) {
                    // Ignore invalid dates
                }
            }
        }
        
        return overdueItems;
    }
    
    // Return an item
    public static boolean returnItem(ObservableList<BorrowRecord> data, String borrowId, String notes) {
        BorrowRecord record = data.stream()
            .filter(r -> borrowId.equals(r.getBorrowId()) && "Dipinjam".equals(r.getStatus()))
            .findFirst()
            .orElse(null);
            
        if (record != null) {
            record.setActualReturnDate(LocalDate.now().toString());
            record.setStatus("Dikembalikan");
            if (notes != null && !notes.trim().isEmpty()) {
                record.setNotes(notes);
            }
            return true;
        }
        
        return false;
    }
}