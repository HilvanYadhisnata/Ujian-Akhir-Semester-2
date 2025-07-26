import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.print.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Import untuk PDF generation
import java.io.FileOutputStream;
import java.awt.Desktop;

public class ReportGenerator {
    private Stage dialog;
    private ObservableList<InventoryItem> inventoryData;
    private ObservableList<BorrowRecord> borrowData;
    private ObservableList<MaintenanceRecord> maintenanceData;
    private String currentReportContent = "";
    
    public ReportGenerator(Stage parent, ObservableList<InventoryItem> inventoryData,
                          ObservableList<BorrowRecord> borrowData, ObservableList<MaintenanceRecord> maintenanceData) {
        this.inventoryData = inventoryData;
        this.borrowData = borrowData;
        this.maintenanceData = maintenanceData;
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Generator Laporan");
        dialog.setResizable(false);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label("Generator Laporan");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        // Report type selection
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll(
            "Laporan Inventaris Lengkap",
            "Laporan Peminjaman",
            "Laporan Maintenance",
            "Laporan Status Alat",
            "Laporan Ringkasan"
        );
        reportTypeCombo.setPromptText("Pilih Jenis Laporan");
        reportTypeCombo.setPrefHeight(35);
        reportTypeCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Date range
        DatePicker startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        startDatePicker.setPromptText("Tanggal Mulai");
        startDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPromptText("Tanggal Akhir");
        endDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Preview area
        TextArea previewArea = new TextArea();
        previewArea.setPromptText("Preview laporan akan ditampilkan di sini...");
        previewArea.setPrefHeight(200);
        previewArea.setEditable(false);
        previewArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7; -fx-font-family: 'Courier New';");
        
        // Update preview when report type changes
        reportTypeCombo.setOnAction(e -> updatePreview(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue(), previewArea));
        
        startDatePicker.setOnAction(e -> updatePreview(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue(), previewArea));
            
        endDatePicker.setOnAction(e -> updatePreview(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue(), previewArea));
        
        // Main Buttons
        HBox mainButtonContainer = new HBox(15);
        mainButtonContainer.setStyle("-fx-alignment: center;");
        
        Button previewButton = new Button("🔍 Preview");
        previewButton.setPrefWidth(100);
        previewButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        previewButton.setOnAction(e -> updatePreview(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue(), previewArea));
        
        // Print & Export buttons section
        VBox actionContainer = new VBox(15);
        actionContainer.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label actionLabel = new Label("Aksi Laporan");
        actionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        actionLabel.setTextFill(Color.web("#2C3E50"));
        
        // Print and Export buttons
        HBox printExportContainer = new HBox(10);
        printExportContainer.setStyle("-fx-alignment: center;");
        
        Button printButton = new Button("🖨️ Cetak");
        printButton.setPrefWidth(120);
        printButton.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        printButton.setOnAction(e -> printReport(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue()));
        
        Button exportTxtButton = new Button("📄 Export TXT");
        exportTxtButton.setPrefWidth(120);
        exportTxtButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        exportTxtButton.setOnAction(e -> exportToTXT(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue()));
        
        Button exportPdfButton = new Button("📄 Export PDF");
        exportPdfButton.setPrefWidth(120);
        exportPdfButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        exportPdfButton.setOnAction(e -> exportToPDF(reportTypeCombo.getValue(), 
            startDatePicker.getValue(), endDatePicker.getValue()));
        
        printExportContainer.getChildren().addAll(printButton, exportTxtButton, exportPdfButton);
        
        actionContainer.getChildren().addAll(actionLabel, printExportContainer);
        
        // Close button
        HBox closeContainer = new HBox();
        closeContainer.setStyle("-fx-alignment: center;");
        
        Button closeButton = new Button("Tutup");
        closeButton.setPrefWidth(100);
        closeButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> dialog.close());
        
        closeContainer.getChildren().add(closeButton);
        
        mainButtonContainer.getChildren().add(previewButton);
        
        formContainer.getChildren().addAll(
            createFieldContainer("Jenis Laporan:", reportTypeCombo),
            createFieldContainer("Tanggal Mulai:", startDatePicker),
            createFieldContainer("Tanggal Akhir:", endDatePicker),
            createFieldContainer("Preview:", previewArea),
            mainButtonContainer,
            new Separator(),
            actionContainer,
            closeContainer
        );
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        Scene scene = new Scene(mainContainer, 650, 750);
        dialog.setScene(scene);
    }
    
    private void updatePreview(String reportType, LocalDate startDate, LocalDate endDate, TextArea previewArea) {
        if (reportType == null) return;
        
        currentReportContent = generateReportContent(reportType, startDate, endDate);
        String preview = currentReportContent.length() > 1000 ? 
            currentReportContent.substring(0, 1000) + "\n\n... (Preview terbatas)" : currentReportContent;
        previewArea.setText(preview);
    }
    
    // Print Report Method
    private void printReport(String reportType, LocalDate startDate, LocalDate endDate) {
        if (reportType == null) {
            showAlert("Error", "Pilih jenis laporan terlebih dahulu!", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            // Generate report content
            String content = generateReportContent(reportType, startDate, endDate);
            
            // Create printer job
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob == null) {
                showAlert("Error", "Tidak dapat mengakses printer!", Alert.AlertType.ERROR);
                return;
            }
            
            // Show print dialog
            if (printerJob.showPrintDialog(dialog)) {
                // Create TextFlow for printing
                TextFlow textFlow = new TextFlow();
                
                // Split content into lines and create Text nodes
                String[] lines = content.split("\n");
                for (String line : lines) {
                    Text text = new Text(line + "\n");
                    text.setFont(Font.font("Courier New", 10));
                    textFlow.getChildren().add(text);
                }
                
                // Set page layout
                Printer printer = printerJob.getPrinter();
                PageLayout pageLayout = printer.createPageLayout(Paper.A4, 
                    PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
                
                textFlow.setPrefWidth(pageLayout.getPrintableWidth());
                textFlow.autosize();
                
                // Print the content
                boolean success = printerJob.printPage(pageLayout, textFlow);
                
                if (success) {
                    printerJob.endJob();
                    showAlert("Sukses", "Laporan berhasil dicetak!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Gagal mencetak laporan!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error saat mencetak: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    // Export to TXT Method
    private void exportToTXT(String reportType, LocalDate startDate, LocalDate endDate) {
        if (reportType == null) {
            showAlert("Error", "Pilih jenis laporan terlebih dahulu!", Alert.AlertType.ERROR);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan sebagai TXT");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        String filename = reportType.replace(" ", "_") + "_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
        fileChooser.setInitialFileName(filename);
        
        File file = fileChooser.showSaveDialog(dialog);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(generateReportContent(reportType, startDate, endDate));
                
                // Ask if user wants to open the file
                Alert openAlert = new Alert(Alert.AlertType.CONFIRMATION);
                openAlert.setTitle("Export Berhasil");
                openAlert.setHeaderText("Laporan berhasil disimpan!");
                openAlert.setContentText("File disimpan di: " + file.getAbsolutePath() + 
                    "\n\nApakah Anda ingin membuka file sekarang?");
                
                ButtonType openButton = new ButtonType("Buka File");
                ButtonType closeButton = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
                openAlert.getButtonTypes().setAll(openButton, closeButton);
                
                openAlert.showAndWait().ifPresent(response -> {
                    if (response == openButton) {
                        openFile(file);
                    }
                });
                
            } catch (IOException e) {
                showAlert("Error", "Gagal menyimpan laporan: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    // Export to PDF Method (Simple HTML to PDF approach)
    private void exportToPDF(String reportType, LocalDate startDate, LocalDate endDate) {
        if (reportType == null) {
            showAlert("Error", "Pilih jenis laporan terlebih dahulu!", Alert.AlertType.ERROR);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan sebagai HTML (untuk PDF)");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("HTML Files", "*.html")
        );
        
        String filename = reportType.replace(" ", "_") + "_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".html";
        fileChooser.setInitialFileName(filename);
        
        File file = fileChooser.showSaveDialog(dialog);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Generate HTML content
                String htmlContent = generateHTMLReport(reportType, startDate, endDate);
                writer.write(htmlContent);
                
                // Show success message with instructions
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Export Berhasil");
                successAlert.setHeaderText("Laporan HTML berhasil dibuat!");
                successAlert.setContentText("File disimpan di: " + file.getAbsolutePath() + 
                    "\n\nUntuk mengkonversi ke PDF:\n" +
                    "1. Buka file HTML di browser\n" +
                    "2. Tekan Ctrl+P (Print)\n" +
                    "3. Pilih 'Save as PDF' sebagai destination\n" +
                    "4. Klik Save\n\n" +
                    "Apakah Anda ingin membuka file HTML sekarang?");
                
                ButtonType openButton = new ButtonType("Buka HTML");
                ButtonType closeButton = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
                successAlert.getButtonTypes().setAll(openButton, closeButton);
                
                successAlert.showAndWait().ifPresent(response -> {
                    if (response == openButton) {
                        openFile(file);
                    }
                });
                
            } catch (IOException e) {
                showAlert("Error", "Gagal menyimpan laporan: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    // Generate HTML Report for PDF export
    private String generateHTMLReport(String reportType, LocalDate startDate, LocalDate endDate) {
        String textContent = generateReportContent(reportType, startDate, endDate);
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>").append(reportType).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Courier New', monospace; margin: 2cm; line-height: 1.4; }\n");
        html.append("h1 { color: #2C3E50; text-align: center; border-bottom: 2px solid #3498DB; padding-bottom: 10px; }\n");
        html.append("h2 { color: #34495E; margin-top: 25px; }\n");
        html.append(".header { text-align: center; margin-bottom: 30px; }\n");
        html.append(".content { white-space: pre-line; }\n");
        html.append(".footer { margin-top: 50px; text-align: center; color: #7F8C8D; font-size: 12px; }\n");
        html.append("@media print { body { margin: 1cm; } }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<div class=\"header\">\n");
        html.append("<h1>LAB INVENTORY SYSTEM</h1>\n");
        html.append("<h2>").append(reportType).append("</h2>\n");
        html.append("</div>\n");
        
        html.append("<div class=\"content\">\n");
        html.append(textContent.replace("\n", "<br>\n"));
        html.append("</div>\n");
        
        html.append("<div class=\"footer\">\n");
        html.append("<p>Generated by Lab Inventory System - ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))).append("</p>\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    // Helper method to open file
    private void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Info", "File berhasil disimpan di: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Info", "File berhasil disimpan di: " + file.getAbsolutePath() + 
                "\nTidak dapat membuka file secara otomatis.", Alert.AlertType.INFORMATION);
        }
    }
    
    private String generateReportContent(String reportType, LocalDate startDate, LocalDate endDate) {
        StringBuilder content = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        
        // Header
        content.append("==========================================\n");
        content.append("         LAB INVENTORY SYSTEM\n");
        content.append("==========================================\n\n");
        content.append(reportType).append("\n");
        content.append("Tanggal Cetak: ").append(LocalDate.now().format(formatter)).append("\n");
        
        if (startDate != null && endDate != null) {
            content.append("Periode: ").append(startDate.format(formatter))
                   .append(" - ").append(endDate.format(formatter)).append("\n");
        }
        content.append("\n");
        
        switch (reportType) {
            case "Laporan Inventaris Lengkap":
                generateInventoryReport(content);
                break;
            case "Laporan Peminjaman":
                generateBorrowingReport(content, startDate, endDate);
                break;
            case "Laporan Maintenance":
                generateMaintenanceReport(content, startDate, endDate);
                break;
            case "Laporan Status Alat":
                generateStatusReport(content);
                break;
            case "Laporan Ringkasan":
                generateSummaryReport(content, startDate, endDate);
                break;
        }
        
        content.append("\n==========================================\n");
        content.append("Generated by Lab Inventory System\n");
        content.append("==========================================");
        
        return content.toString();
    }
    
    private void generateInventoryReport(StringBuilder content) {
        content.append("DAFTAR INVENTARIS\n");
        content.append("------------------------------------------\n");
        
        Map<String, List<InventoryItem>> byCategory = inventoryData.stream()
            .collect(Collectors.groupingBy(InventoryItem::getKategori));
        
        for (Map.Entry<String, List<InventoryItem>> entry : byCategory.entrySet()) {
            content.append("\n").append(entry.getKey()).append(":\n");
            for (InventoryItem item : entry.getValue()) {
                content.append(String.format("  %s - %s (%s) - %s - %s\n",
                    item.getId(), item.getNama(), item.getMerk(), item.getKondisi(), item.getStatus()));
            }
        }
        
        content.append("\nRINGKASAN:\n");
        content.append("Total Item: ").append(inventoryData.size()).append("\n");
        content.append("Tersedia: ").append(inventoryData.stream().filter(i -> "Tersedia".equals(i.getStatus())).count()).append("\n");
        content.append("Dipinjam: ").append(inventoryData.stream().filter(i -> "Dipinjam".equals(i.getStatus())).count()).append("\n");
        content.append("Maintenance: ").append(inventoryData.stream().filter(i -> "Maintenance".equals(i.getStatus())).count()).append("\n");
    }
    
    private void generateBorrowingReport(StringBuilder content, LocalDate startDate, LocalDate endDate) {
        content.append("LAPORAN PEMINJAMAN\n");
        content.append("------------------------------------------\n");
        
        List<BorrowRecord> filteredRecords = borrowData.stream()
            .filter(record -> {
                if (startDate == null || endDate == null) return true;
                try {
                    LocalDate borrowDate = LocalDate.parse(record.getBorrowDate());
                    return !borrowDate.isBefore(startDate) && !borrowDate.isAfter(endDate);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        for (BorrowRecord record : filteredRecords) {
            content.append(String.format("%s - %s\n  Peminjam: %s (%s)\n  Tanggal: %s - %s\n  Status: %s\n\n",
                record.getBorrowId(), record.getInventoryName(),
                record.getBorrowerName(), record.getBorrowerType(),
                record.getBorrowDate(), record.getReturnDate(),
                record.getStatus()));
        }
        
        content.append("RINGKASAN:\n");
        content.append("Total Peminjaman: ").append(filteredRecords.size()).append("\n");
        content.append("Aktif: ").append(filteredRecords.stream().filter(r -> "Dipinjam".equals(r.getStatus())).count()).append("\n");
        content.append("Selesai: ").append(filteredRecords.stream().filter(r -> "Dikembalikan".equals(r.getStatus())).count()).append("\n");
        content.append("Terlambat: ").append(filteredRecords.stream().filter(r -> "Terlambat".equals(r.getStatus())).count()).append("\n");
    }
    
    private void generateMaintenanceReport(StringBuilder content, LocalDate startDate, LocalDate endDate) {
        content.append("LAPORAN MAINTENANCE\n");
        content.append("------------------------------------------\n");
        
        List<MaintenanceRecord> filteredRecords = maintenanceData.stream()
            .filter(record -> {
                if (startDate == null || endDate == null) return true;
                try {
                    LocalDate reportDate = LocalDate.parse(record.getReportedDate());
                    return !reportDate.isBefore(startDate) && !reportDate.isAfter(endDate);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        for (MaintenanceRecord record : filteredRecords) {
            content.append(String.format("%s - %s\n  Alat: %s\n  Jenis: %s\n  Prioritas: %s\n  Status: %s\n  Teknisi: %s\n  Biaya: %s\n\n",
                record.getMaintenanceId(), record.getDescription(),
                record.getInventoryName(), record.getIssueType(),
                record.getPriority(), record.getStatus(),
                record.getTechnician().isEmpty() ? "-" : record.getTechnician(),
                record.getCost().isEmpty() ? "-" : "Rp " + record.getCost()));
        }
        
        double totalCost = MaintenanceManager.getTotalMaintenanceCost(filteredRecords);
        
        content.append("RINGKASAN:\n");
        content.append("Total Laporan: ").append(filteredRecords.size()).append("\n");
        content.append("Pending: ").append(filteredRecords.stream().filter(r -> "Dilaporkan".equals(r.getStatus())).count()).append("\n");
        content.append("Dalam Proses: ").append(filteredRecords.stream().filter(r -> "Dalam Perbaikan".equals(r.getStatus())).count()).append("\n");
        content.append("Selesai: ").append(filteredRecords.stream().filter(r -> "Selesai".equals(r.getStatus())).count()).append("\n");
        content.append("Total Biaya: Rp ").append(String.format("%,.0f", totalCost)).append("\n");
    }
    
    private void generateStatusReport(StringBuilder content) {
        content.append("LAPORAN STATUS ALAT\n");
        content.append("------------------------------------------\n");
        
        Map<String, Long> statusCount = inventoryData.stream()
            .collect(Collectors.groupingBy(InventoryItem::getStatus, Collectors.counting()));
        
        Map<String, Long> conditionCount = inventoryData.stream()
            .collect(Collectors.groupingBy(InventoryItem::getKondisi, Collectors.counting()));
        
        content.append("STATUS KETERSEDIAAN:\n");
        statusCount.forEach((status, count) -> 
            content.append("  ").append(status).append(": ").append(count).append("\n"));
        
        content.append("\nKONDISI ALAT:\n");
        conditionCount.forEach((condition, count) -> 
            content.append("  ").append(condition).append(": ").append(count).append("\n"));
        
        content.append("\nALAT BERMASALAH:\n");
        inventoryData.stream()
            .filter(item -> "Rusak".equals(item.getKondisi()) || "Maintenance".equals(item.getStatus()))
            .forEach(item -> content.append("  ").append(item.getId()).append(" - ").append(item.getNama()).append("\n"));
    }
    
    private void generateSummaryReport(StringBuilder content, LocalDate startDate, LocalDate endDate) {
        content.append("LAPORAN RINGKASAN\n");
        content.append("------------------------------------------\n");
        
        // Inventory summary
        content.append("INVENTARIS:\n");
        content.append("  Total Item: ").append(inventoryData.size()).append("\n");
        content.append("  Tersedia: ").append(inventoryData.stream().filter(i -> "Tersedia".equals(i.getStatus())).count()).append("\n");
        content.append("  Dipinjam: ").append(inventoryData.stream().filter(i -> "Dipinjam".equals(i.getStatus())).count()).append("\n");
        content.append("  Maintenance: ").append(inventoryData.stream().filter(i -> "Maintenance".equals(i.getStatus())).count()).append("\n\n");
        
        // Borrowing summary
        long activeBorrows = borrowData.stream().filter(r -> "Dipinjam".equals(r.getStatus())).count();
        long overdueBorrows = borrowData.stream().filter(r -> "Terlambat".equals(r.getStatus())).count();
        
        content.append("PEMINJAMAN:\n");
        content.append("  Aktif: ").append(activeBorrows).append("\n");
        content.append("  Terlambat: ").append(overdueBorrows).append("\n\n");
        
        // Maintenance summary
        long pendingMaintenance = maintenanceData.stream().filter(r -> "Dilaporkan".equals(r.getStatus())).count();
        long inProgressMaintenance = maintenanceData.stream().filter(r -> "Dalam Perbaikan".equals(r.getStatus())).count();
        
        content.append("MAINTENANCE:\n");
        content.append("  Pending: ").append(pendingMaintenance).append("\n");
        content.append("  Dalam Proses: ").append(inProgressMaintenance).append("\n");
        
        if (startDate != null && endDate != null) {
            List<MaintenanceRecord> periodRecords = MaintenanceManager.getCompletedMaintenanceInRange(maintenanceData, startDate, endDate);
            double totalCost = MaintenanceManager.getTotalMaintenanceCost(periodRecords);
            content.append("  Selesai (Periode): ").append(periodRecords.size()).append("\n");
            content.append("  Biaya (Periode): Rp ").append(String.format("%,.0f", totalCost)).append("\n");
        }
    }
    
    private VBox createFieldContainer(String label, javafx.scene.Node field) {
        VBox container = new VBox(5);
        Label fieldLabel = new Label(label);
        fieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        fieldLabel.setTextFill(Color.web("#2C3E50"));
        container.getChildren().addAll(fieldLabel, field);
        return container;
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void showAndWait() {
        dialog.showAndWait();
    }
}