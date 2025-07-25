import java.io.*;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

public class MaintenanceManager {
    private static final String MAINTENANCE_FILE = "maintenance.csv";
    private static final String MAINTENANCE_HEADER = "Maintenance ID,Inventory ID,Inventory Name,Issue Type,Description,Reported By,Reported Date,Priority,Status,Technician,Completed Date,Cost,Notes";
    
    // Save maintenance data
    public static void saveMaintenanceData(ObservableList<MaintenanceRecord> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MAINTENANCE_FILE))) {
            writer.println(MAINTENANCE_HEADER);
            for (MaintenanceRecord record : data) {
                writer.println(record.toCSV());
            }
            System.out.println("Data maintenance berhasil disimpan ke " + MAINTENANCE_FILE);
        } catch (IOException e) {
            System.err.println("Error saving maintenance data: " + e.getMessage());
        }
    }
    
    // Load maintenance data
    public static ObservableList<MaintenanceRecord> loadMaintenanceData() {
        ObservableList<MaintenanceRecord> data = FXCollections.observableArrayList();
        File file = new File(MAINTENANCE_FILE);
        
        if (!file.exists()) {
            return data; // Return empty list if file doesn't exist
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(MAINTENANCE_FILE))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                MaintenanceRecord record = MaintenanceRecord.fromCSV(line);
                if (record != null) {
                    data.add(record);
                }
            }
            System.out.println("Data maintenance berhasil dimuat dari " + MAINTENANCE_FILE);
        } catch (IOException e) {
            System.err.println("Error loading maintenance data: " + e.getMessage());
        }
        
        return data;
    }
    
    // Generate new maintenance ID
    public static String generateNewMaintenanceId(ObservableList<MaintenanceRecord> data) {
        int maxId = 0;
        String prefix = "MNT";
        
        for (MaintenanceRecord record : data) {
            String id = record.getMaintenanceId();
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
    
    // Get active maintenance for an inventory item
    public static MaintenanceRecord getActiveMaintenanceForItem(ObservableList<MaintenanceRecord> data, String inventoryId) {
        return data.stream()
            .filter(record -> inventoryId.equals(record.getInventoryId()) && 
                    ("Dilaporkan".equals(record.getStatus()) || "Dalam Perbaikan".equals(record.getStatus())))
            .findFirst()
            .orElse(null);
    }
    
    // Check if inventory item is under maintenance
    public static boolean isItemUnderMaintenance(ObservableList<MaintenanceRecord> data, String inventoryId) {
        return getActiveMaintenanceForItem(data, inventoryId) != null;
    }
    
    // Get pending maintenance items
    public static List<MaintenanceRecord> getPendingMaintenance(ObservableList<MaintenanceRecord> data) {
        return data.stream()
            .filter(record -> "Dilaporkan".equals(record.getStatus()) || "Dalam Perbaikan".equals(record.getStatus()))
            .sorted((r1, r2) -> {
                // Sort by priority: Urgent, Tinggi, Sedang, Rendah
                Map<String, Integer> priorityOrder = Map.of(
                    "Urgent", 4, "Tinggi", 3, "Sedang", 2, "Rendah", 1
                );
                return priorityOrder.getOrDefault(r2.getPriority(), 0) - 
                       priorityOrder.getOrDefault(r1.getPriority(), 0);
            })
            .toList();
    }
    
    // Get completed maintenance in date range
    public static List<MaintenanceRecord> getCompletedMaintenanceInRange(ObservableList<MaintenanceRecord> data, 
                                                                         LocalDate startDate, LocalDate endDate) {
        return data.stream()
            .filter(record -> "Selesai".equals(record.getStatus()))
            .filter(record -> {
                try {
                    if (record.getCompletedDate() != null && !record.getCompletedDate().isEmpty()) {
                        LocalDate completedDate = LocalDate.parse(record.getCompletedDate());
                        return !completedDate.isBefore(startDate) && !completedDate.isAfter(endDate);
                    }
                } catch (Exception e) {
                    // Ignore invalid dates
                }
                return false;
            })
            .toList();
    }
    
    // Calculate total maintenance cost in date range
    public static double getTotalMaintenanceCost(List<MaintenanceRecord> records) {
        return records.stream()
            .mapToDouble(record -> {
                try {
                    String cost = record.getCost();
                    if (cost != null && !cost.isEmpty()) {
                        return Double.parseDouble(cost.replace(",", ""));
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid costs
                }
                return 0.0;
            })
            .sum();
    }
    
    // Update maintenance status
    public static boolean updateMaintenanceStatus(ObservableList<MaintenanceRecord> data, String maintenanceId, 
                                                 String newStatus, String technician, String notes, String cost) {
        MaintenanceRecord record = data.stream()
            .filter(r -> maintenanceId.equals(r.getMaintenanceId()))
            .findFirst()
            .orElse(null);
            
        if (record != null) {
            record.setStatus(newStatus);
            if (technician != null && !technician.trim().isEmpty()) {
                record.setTechnician(technician);
            }
            if ("Selesai".equals(newStatus)) {
                record.setCompletedDate(LocalDate.now().toString());
            }
            if (cost != null && !cost.trim().isEmpty()) {
                record.setCost(cost);
            }
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = record.getNotes();
                if (existingNotes != null && !existingNotes.isEmpty()) {
                    record.setNotes(existingNotes + " | " + notes);
                } else {
                    record.setNotes(notes);
                }
            }
            return true;
        }
        
        return false;
    }
}