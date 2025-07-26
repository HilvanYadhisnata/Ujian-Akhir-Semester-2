import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BorrowManager {
    private static final String CSV_FILE = "borrowing.csv";
    private static final String CSV_HEADER = "BorrowID,InventoryID,InventoryName,BorrowerName,BorrowerType,BorrowerContact,BorrowDate,ReturnDate,ActualReturnDate,Status,Notes";
    
    public static void saveBorrowData(ObservableList<BorrowRecord> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println(CSV_HEADER);
            for (BorrowRecord record : data) {
                writer.println(record.toCSV());
            }
            System.out.println("Data peminjaman berhasil disimpan ke " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("Error saving borrow data: " + e.getMessage());
        }
    }
    
    public static ObservableList<BorrowRecord> loadBorrowData() {
        ObservableList<BorrowRecord> data = FXCollections.observableArrayList();
        File file = new File(CSV_FILE);
        
        if (!file.exists()) {
            // Create sample data if file doesn't exist
            createSampleBorrowData(data);
            saveBorrowData(data);
            return data;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
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
            System.out.println("Data peminjaman berhasil dimuat dari " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("Error loading borrow data: " + e.getMessage());
            createSampleBorrowData(data);
        }
        
        return data;
    }
    
    private static void createSampleBorrowData(ObservableList<BorrowRecord> data) {
        data.addAll(Arrays.asList(
            new BorrowRecord("BRW0001", "HW0003", "Laptop", "John Doe", "Mahasiswa", "john@email.com", 
                "2024-02-20", "2024-02-27", "", "Dipinjam", "Untuk tugas akhir"),
            new BorrowRecord("BRW0002", "ACC0001", "Keyboard", "Jane Smith", "Dosen", "jane@email.com", 
                "2024-02-15", "2024-02-20", "2024-02-19", "Dikembalikan", "Dikembalikan tepat waktu"),
            new BorrowRecord("BRW0003", "HW0005", "Proyektor", "Mike Johnson", "Staff", "mike@email.com", 
                "2024-02-10", "2024-02-15", "", "Terlambat", "Belum dikembalikan, sudah lewat batas waktu")
        ));
    }
    
    public static String generateNewBorrowId(ObservableList<BorrowRecord> data) {
        int maxId = 0;
        
        for (BorrowRecord record : data) {
            String id = record.getBorrowId();
            if (id.startsWith("BRW")) {
                try {
                    int numId = Integer.parseInt(id.substring(3));
                    maxId = Math.max(maxId, numId);
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
        }
        
        return String.format("BRW%04d", maxId + 1);
    }
    
    public static boolean isItemBorrowed(ObservableList<BorrowRecord> data, String inventoryId) {
        return data.stream().anyMatch(record -> 
            record.getInventoryId().equals(inventoryId) && 
            ("Dipinjam".equals(record.getStatus()) || "Terlambat".equals(record.getStatus()))
        );
    }
    
    public static boolean returnItem(ObservableList<BorrowRecord> data, String borrowId, String returnNotes) {
        for (BorrowRecord record : data) {
            if (record.getBorrowId().equals(borrowId)) {
                record.setActualReturnDate(LocalDate.now().toString());
                record.setStatus("Dikembalikan");
                record.setNotes(returnNotes);
                return true;
            }
        }
        return false;
    }
    
    public static List<BorrowRecord> getOverdueItems(ObservableList<BorrowRecord> data) {
        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdueItems = new ArrayList<>();
        
        for (BorrowRecord record : data) {
            if ("Dipinjam".equals(record.getStatus())) {
                try {
                    LocalDate returnDate = LocalDate.parse(record.getReturnDate());
                    if (today.isAfter(returnDate)) {
                        record.setStatus("Terlambat");
                        overdueItems.add(record);
                    }
                } catch (Exception e) {
                    // Skip records with invalid dates
                }
            }
        }
        
        return overdueItems;
    }
    
    public static List<BorrowRecord> getActiveBorrows(ObservableList<BorrowRecord> data) {
        return data.stream()
            .filter(record -> "Dipinjam".equals(record.getStatus()) || "Terlambat".equals(record.getStatus()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public static long getDaysOverdue(BorrowRecord record) {
        if (!"Terlambat".equals(record.getStatus())) {
            return 0;
        }
        
        try {
            LocalDate returnDate = LocalDate.parse(record.getReturnDate());
            LocalDate today = LocalDate.now();
            return ChronoUnit.DAYS.between(returnDate, today);
        } catch (Exception e) {
            return 0;
        }
    }
}