import java.io.*;
import java.util.*;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MaintenanceManager {
    private static final String CSV_FILE = "maintenance.csv";
    // PERBAIKAN: Update header CSV dengan field tambahan
    private static final String CSV_HEADER = "MaintenanceID,InventoryID,InventoryName,IssueType,Priority,ReportedBy,Description,ReportedDate,CompletedDate,Status,Technician,Cost,Solution";
    
    public static void saveMaintenanceData(ObservableList<MaintenanceRecord> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println(CSV_HEADER);
            for (MaintenanceRecord record : data) {
                writer.println(record.toCSV());
            }
            System.out.println("Data maintenance berhasil disimpan ke " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("Error saving maintenance data: " + e.getMessage());
        }
    }
    
    public static ObservableList<MaintenanceRecord> loadMaintenanceData() {
        ObservableList<MaintenanceRecord> data = FXCollections.observableArrayList();
        File file = new File(CSV_FILE);
        
        if (!file.exists()) {
            // Create sample data if file doesn't exist
            createSampleMaintenanceData(data);
            saveMaintenanceData(data);
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
                
                MaintenanceRecord record = MaintenanceRecord.fromCSV(line);
                if (record != null) {
                    data.add(record);
                }
            }
            System.out.println("Data maintenance berhasil dimuat dari " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("Error loading maintenance data: " + e.getMessage());
            createSampleMaintenanceData(data);
        }
        
        return data;
    }
    
    private static void createSampleMaintenanceData(ObservableList<MaintenanceRecord> data) {
        // PERBAIKAN: Sample data dengan field tambahan
        data.addAll(Arrays.asList(
            new MaintenanceRecord("MNT0001", "HW0004", "Printer", "Kerusakan", "Tinggi", "admin", 
                "Printer tidak bisa mencetak, kertas sering macet", "2024-02-15", "2024-02-20", "Selesai", 
                "John Doe", "150000", "Membersihkan jalur kertas dan mengganti roller"),
            new MaintenanceRecord("MNT0002", "HW0001", "Komputer Desktop", "Maintenance Rutin", "Sedang", "Sistem Otomatis", 
                "Maintenance rutin terjadwal (setiap 6 bulan)", "2024-01-15", "2024-01-20", "Selesai",
                "Jane Smith", "75000", "Pembersihan hardware dan update software"),
            new MaintenanceRecord("MNT0003", "NET0001", "Switch 24 Port", "Maintenance Rutin", "Sedang", "Sistem Otomatis", 
                "Maintenance rutin terjadwal (setiap 6 bulan)", "2024-03-01", null, "Menunggu Penanganan",
                "", "", "")
        ));
    }
    
    public static String generateNewMaintenanceId(ObservableList<MaintenanceRecord> data) {
        int maxId = 0;
        
        for (MaintenanceRecord record : data) {
            String id = record.getMaintenanceId();
            if (id.startsWith("MNT")) {
                try {
                    int numId = Integer.parseInt(id.substring(3));
                    maxId = Math.max(maxId, numId);
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
        }
        
        return String.format("MNT%04d", maxId + 1);
    }
    
    public static boolean isItemUnderMaintenance(ObservableList<MaintenanceRecord> data, String inventoryId) {
        return data.stream().anyMatch(record -> 
            record.getInventoryId().equals(inventoryId) && 
            ("Menunggu Penanganan".equals(record.getStatus()) || "Dalam Proses".equals(record.getStatus()))
        );
    }
    
    // PERBAIKAN: Method untuk update maintenance record secara lengkap
    public static boolean updateMaintenanceRecord(ObservableList<MaintenanceRecord> data, MaintenanceRecord updatedRecord) {
        for (int i = 0; i < data.size(); i++) {
            MaintenanceRecord record = data.get(i);
            if (record.getMaintenanceId().equals(updatedRecord.getMaintenanceId())) {
                // Update semua field
                record.setIssueType(updatedRecord.getIssueType());
                record.setPriority(updatedRecord.getPriority());
                record.setReportedBy(updatedRecord.getReportedBy());
                record.setDescription(updatedRecord.getDescription());
                record.setReportedDate(updatedRecord.getReportedDate());
                record.setCompletedDate(updatedRecord.getCompletedDate());
                record.setStatus(updatedRecord.getStatus());
                record.setTechnician(updatedRecord.getTechnician());
                record.setCost(updatedRecord.getCost());
                record.setSolution(updatedRecord.getSolution());
                return true;
            }
        }
        return false;
    }
    
    public static boolean completeMaintenanceRecord(ObservableList<MaintenanceRecord> data, String maintenanceId, String completedDate) {
        for (MaintenanceRecord record : data) {
            if (record.getMaintenanceId().equals(maintenanceId)) {
                record.setStatus("Selesai");
                record.setCompletedDate(completedDate);
                return true;
            }
        }
        return false;
    }
    
    public static List<MaintenanceRecord> getPendingMaintenance(ObservableList<MaintenanceRecord> data) {
        return data.stream()
            .filter(record -> "Menunggu Penanganan".equals(record.getStatus()) || "Dalam Proses".equals(record.getStatus()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public static List<MaintenanceRecord> getOverdueMaintenance(ObservableList<MaintenanceRecord> data) {
        java.time.LocalDate today = java.time.LocalDate.now();
        return data.stream()
            .filter(record -> {
                try {
                    java.time.LocalDate reportedDate = java.time.LocalDate.parse(record.getReportedDate());
                    long daysSinceReported = java.time.temporal.ChronoUnit.DAYS.between(reportedDate, today);
                    return daysSinceReported > 7 && 
                           ("Menunggu Penanganan".equals(record.getStatus()) || "Dalam Proses".equals(record.getStatus()));
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    // PERBAIKAN: Method tambahan untuk reporting
    public static List<MaintenanceRecord> getCompletedMaintenanceInRange(ObservableList<MaintenanceRecord> data, 
                                                                         LocalDate startDate, LocalDate endDate) {
        return data.stream()
            .filter(record -> {
                if (!"Selesai".equals(record.getStatus()) || record.getCompletedDate().isEmpty()) {
                    return false;
                }
                try {
                    LocalDate completedDate = LocalDate.parse(record.getCompletedDate());
                    return !completedDate.isBefore(startDate) && !completedDate.isAfter(endDate);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    // PERBAIKAN: Method untuk menghitung total biaya maintenance
    public static double getTotalMaintenanceCost(List<MaintenanceRecord> records) {
        return records.stream()
            .mapToDouble(record -> {
                try {
                    String costStr = record.getCost().replaceAll("[^\\d.]", ""); // Remove non-numeric characters
                    return costStr.isEmpty() ? 0.0 : Double.parseDouble(costStr);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            })
            .sum();
    }
    
    // PERBAIKAN: Method untuk mendapatkan maintenance berdasarkan prioritas
    public static List<MaintenanceRecord> getMaintenanceByPriority(ObservableList<MaintenanceRecord> data, String priority) {
        return data.stream()
            .filter(record -> priority.equals(record.getPriority()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    // PERBAIKAN: Method untuk mendapatkan statistik maintenance
    public static Map<String, Long> getMaintenanceStatistics(ObservableList<MaintenanceRecord> data) {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("total", (long) data.size());
        stats.put("pending", data.stream().filter(r -> "Menunggu Penanganan".equals(r.getStatus())).count());
        stats.put("inProgress", data.stream().filter(r -> "Dalam Proses".equals(r.getStatus())).count());
        stats.put("completed", data.stream().filter(r -> "Selesai".equals(r.getStatus())).count());
        stats.put("highPriority", data.stream().filter(r -> "Tinggi".equals(r.getPriority()) || "Kritis".equals(r.getPriority())).count());
        
        return stats;
    }
}