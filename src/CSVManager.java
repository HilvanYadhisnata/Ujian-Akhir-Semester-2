import java.io.*;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CSVManager {
    private static final String CSV_FILE = "inventory.csv";
    private static final String CSV_HEADER = "ID,Nama,Kategori,Merk,Kondisi,Lokasi,Status,Tanggal Masuk";
    
    public static void saveInventoryData(ObservableList<InventoryItem> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println(CSV_HEADER);
            for (InventoryItem item : data) {
                writer.println(item.toCSV());
            }
            System.out.println("Data berhasil disimpan ke " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    public static ObservableList<InventoryItem> loadInventoryData() {
        ObservableList<InventoryItem> data = FXCollections.observableArrayList();
        File file = new File(CSV_FILE);
        
        if (!file.exists()) {
            // Create sample data if file doesn't exist
            createSampleData(data);
            saveInventoryData(data);
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
                
                InventoryItem item = InventoryItem.fromCSV(line);
                if (item != null) {
                    data.add(item);
                }
            }
            System.out.println("Data berhasil dimuat dari " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            createSampleData(data);
        }
        
        return data;
    }
    
    private static void createSampleData(ObservableList<InventoryItem> data) {
        data.addAll(Arrays.asList(
            new InventoryItem("HW0001", "Komputer Desktop", "Hardware", "ASUS", "Baik", "Lab 1", "Tersedia", "2024-01-15"),
            new InventoryItem("HW0002", "Monitor LCD 24\"", "Hardware", "LG", "Baik", "Lab 1", "Tersedia", "2024-01-15"),
            new InventoryItem("HW0003", "Laptop", "Hardware", "Dell", "Baik", "Lab 2", "Dipinjam", "2024-02-01"),
            new InventoryItem("HW0004", "Printer", "Hardware", "Canon", "Rusak", "Lab 2", "Maintenance", "2024-02-10"),
            new InventoryItem("HW0005", "Proyektor", "Hardware", "Epson", "Baik", "Lab 3", "Tersedia", "2024-02-15"),
            new InventoryItem("ACC0001", "Keyboard", "Aksesoris", "Logitech", "Baik", "Lab 1", "Tersedia", "2024-01-20"),
            new InventoryItem("ACC0002", "Mouse", "Aksesoris", "Logitech", "Baik", "Lab 1", "Tersedia", "2024-01-20"),
            new InventoryItem("NET0001", "Switch 24 Port", "Networking", "Cisco", "Baik", "Server Room", "Tersedia", "2024-03-01"),
            new InventoryItem("SW0001", "Microsoft Office", "Software", "Microsoft", "Baik", "Lab 1", "Tersedia", "2024-01-10"),
            new InventoryItem("FUR0001", "Meja Komputer", "Furniture", "IKEA", "Baik", "Lab 1", "Tersedia", "2024-01-05")
        ));
    }
    
    // Category prefixes mapping
    private static final Map<String, String> CATEGORY_PREFIXES = Map.of(
        "Hardware", "HW",
        "Software", "SW", 
        "Aksesoris", "ACC",
        "Networking", "NET",
        "Furniture", "FUR"
    );
    
    public static String generateNewId(ObservableList<InventoryItem> data, String kategori) {
        String prefix = CATEGORY_PREFIXES.getOrDefault(kategori, "GEN");
        int maxId = 0;
        
        // Find the highest ID number for this category
        for (InventoryItem item : data) {
            String id = item.getId();
            if (id.startsWith(prefix)) {
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
    
    // Overloaded method for backward compatibility
    public static String generateNewId(ObservableList<InventoryItem> data) {
        return generateNewId(data, "Hardware"); // Default to Hardware
    }
    
    public static String getCategoryPrefix(String kategori) {
        return CATEGORY_PREFIXES.getOrDefault(kategori, "GEN");
    }
}