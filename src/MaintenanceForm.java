import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MaintenanceForm {
    private Stage dialog;
    private MaintenanceRecord result;
    private boolean isEditMode = false;
    private ObservableList<InventoryItem> inventoryData;
    private MaintenanceRecord originalRecord;
    
    public MaintenanceForm(Stage parent, ObservableList<InventoryItem> inventoryData, MaintenanceRecord record) {
        this.inventoryData = inventoryData;
        this.isEditMode = (record != null);
        this.originalRecord = record;
        createDialog(parent, record);
    }
    
    private void createDialog(Stage parent, MaintenanceRecord record) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(isEditMode ? "Update Maintenance" : "Lapor Kerusakan/Maintenance");
        dialog.setResizable(true); // PERBAIKAN: Buat resizable
        
        // PERBAIKAN: Gunakan ScrollPane untuk mengatasi masalah panjang form
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #ECF0F1; -fx-background: #ECF0F1;");
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label(isEditMode ? "Update Status Maintenance" : "Lapor Kerusakan/Maintenance");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        // ID Field - Auto-generated, shown but disabled
        TextField idField = createTextField("ID Maintenance (Auto-generated)", 
            record != null ? record.getMaintenanceId() : "");
        idField.setDisable(true);
        
        // Inventory selection
        ComboBox<InventoryItem> inventoryCombo = new ComboBox<>();
        inventoryCombo.getItems().addAll(inventoryData);
        inventoryCombo.setPromptText("Pilih Alat");
        inventoryCombo.setPrefHeight(35);
        inventoryCombo.setMaxWidth(Double.MAX_VALUE); // PERBAIKAN: Gunakan lebar penuh
        inventoryCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Custom cell factory to display item name and ID
        inventoryCombo.setCellFactory(param -> new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getNama());
                }
            }
        });
        
        inventoryCombo.setButtonCell(new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getId() + " - " + item.getNama());
                }
            }
        });
        
        // Set selected item if editing
        if (record != null) {
            InventoryItem selectedItem = inventoryData.stream()
                .filter(item -> item.getId().equals(record.getInventoryId()))
                .findFirst()
                .orElse(null);
            inventoryCombo.setValue(selectedItem);
            inventoryCombo.setDisable(true); // Don't allow changing item when editing
        }
        
        ComboBox<String> issueTypeCombo = createComboBox("Jenis Masalah", 
            new String[]{"Kerusakan", "Maintenance Rutin", "Upgrade", "Pembersihan", "Kalibrasi"});
        if (record != null) issueTypeCombo.setValue(record.getIssueType());
        
        ComboBox<String> priorityCombo = createComboBox("Prioritas", 
            new String[]{"Rendah", "Sedang", "Tinggi", "Kritis"});
        if (record != null) priorityCombo.setValue(record.getPriority());
        
        TextField reporterField = createTextField("Pelapor", 
            record != null ? record.getReportedBy() : System.getProperty("user.name"));
        
        // Description field
        Label descLabel = new Label("Deskripsi Masalah:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        descLabel.setTextFill(Color.web("#2C3E50"));
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Jelaskan masalah atau kebutuhan maintenance secara detail...");
        descriptionArea.setPrefRowCount(3); // PERBAIKAN: Kurangi tinggi default
        descriptionArea.setMaxWidth(Double.MAX_VALUE);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        if (record != null) descriptionArea.setText(record.getDescription());
        
        DatePicker reportedDatePicker = new DatePicker();
        reportedDatePicker.setPromptText("Tanggal Lapor");
        reportedDatePicker.setMaxWidth(Double.MAX_VALUE);
        reportedDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        if (record != null) {
            try {
                reportedDatePicker.setValue(LocalDate.parse(record.getReportedDate()));
            } catch (Exception e) {
                reportedDatePicker.setValue(LocalDate.now());
            }
        } else {
            reportedDatePicker.setValue(LocalDate.now());
        }
        reportedDatePicker.setDisable(isEditMode); // Don't allow changing date when editing
        
        // Status field (only for editing)
        ComboBox<String> statusCombo = null;
        DatePicker completedDatePicker = null;
        TextField technicianField = null;
        TextField costField = null;
        TextArea solutionArea = null;
        
        if (isEditMode) {
            statusCombo = createComboBox("Status", 
                new String[]{"Menunggu Penanganan", "Dalam Proses", "Selesai"});
            statusCombo.setValue(record.getStatus());
            
            completedDatePicker = new DatePicker();
            completedDatePicker.setPromptText("Tanggal Selesai (opsional)");
            completedDatePicker.setMaxWidth(Double.MAX_VALUE);
            completedDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
            if (record.getCompletedDate() != null && !record.getCompletedDate().isEmpty()) {
                try {
                    completedDatePicker.setValue(LocalDate.parse(record.getCompletedDate()));
                } catch (Exception e) {
                    // Ignore parsing error
                }
            }
            
            // PERBAIKAN: Tambahan field untuk update maintenance
            technicianField = createTextField("Teknisi", "");
            costField = createTextField("Biaya (Rp)", "");
            
            solutionArea = new TextArea();
            solutionArea.setPromptText("Solusi atau tindakan yang dilakukan...");
            solutionArea.setPrefRowCount(3); // PERBAIKAN: Kurangi tinggi
            solutionArea.setMaxWidth(Double.MAX_VALUE);
            solutionArea.setWrapText(true);
            solutionArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
            
            // PERBAIKAN: Auto-fill tanggal selesai ketika status diubah ke "Selesai"
            ComboBox<String> finalStatusCombo = statusCombo;
            DatePicker finalCompletedDatePicker = completedDatePicker;
            
            statusCombo.setOnAction(e -> {
                if ("Selesai".equals(finalStatusCombo.getValue())) {
                    if (finalCompletedDatePicker.getValue() == null) {
                        finalCompletedDatePicker.setValue(LocalDate.now());
                    }
                }
            });
        }
        
        // Buttons - PERBAIKAN: Posisi tombol di bagian bawah yang selalu terlihat
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveButton = new Button(isEditMode ? "Update" : "Simpan");
        saveButton.setPrefWidth(100);
        saveButton.setPrefHeight(40);
        saveButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        ComboBox<String> finalStatusCombo = statusCombo;
        DatePicker finalCompletedDatePicker = completedDatePicker;
        TextField finalTechnicianField = technicianField;
        TextField finalCostField = costField;
        TextArea finalSolutionArea = solutionArea;
        
        saveButton.setOnAction(e -> {
            if (validateForm(inventoryCombo, issueTypeCombo, priorityCombo, reporterField, 
                           descriptionArea, reportedDatePicker)) {
                
                InventoryItem selectedItem = inventoryCombo.getValue();
                String completedDateStr = "";
                String statusStr = "Menunggu Penanganan"; 
                String technicianStr = "";
                String costStr = "";
                String solutionStr = "";
                
                if (isEditMode && finalStatusCombo != null) {
                    statusStr = finalStatusCombo.getValue();
                    if (finalCompletedDatePicker.getValue() != null) {
                        completedDateStr = finalCompletedDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    if (finalTechnicianField != null) {
                        technicianStr = finalTechnicianField.getText().trim();
                    }
                    if (finalCostField != null) {
                        costStr = finalCostField.getText().trim();
                    }
                    if (finalSolutionArea != null) {
                        solutionStr = finalSolutionArea.getText().trim();
                    }
                }
                
                // PERBAIKAN: Buat MaintenanceRecord dengan semua field yang diperlukan
                result = new MaintenanceRecord(
                    isEditMode ? record.getMaintenanceId() : "", // Will be set by Main.java
                    selectedItem.getId(),
                    selectedItem.getNama(),
                    issueTypeCombo.getValue(),
                    priorityCombo.getValue(),
                    reporterField.getText().trim(),
                    descriptionArea.getText().trim(),
                    reportedDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    completedDateStr,
                    statusStr
                );
                
                // PERBAIKAN: Set additional fields jika dalam mode edit
                if (isEditMode) {
                    result.setTechnician(technicianStr);
                    result.setCost(costStr);
                    result.setSolution(solutionStr);
                }
                
                dialog.close();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(saveButton, cancelButton);
        
        // Add all components to form - PERBAIKAN: Lebih terorganisir
        formContainer.getChildren().addAll(
            createFieldContainer("ID:", idField),
            createFieldContainer("Alat:", inventoryCombo),
            createFieldContainer("Jenis Masalah:", issueTypeCombo),
            createFieldContainer("Prioritas:", priorityCombo),
            createFieldContainer("Pelapor:", reporterField),
            createFieldContainer("Deskripsi:", descriptionArea),
            createFieldContainer("Tanggal Lapor:", reportedDatePicker)
        );
        
        if (isEditMode && statusCombo != null) {
            // PERBAIKAN: Tambahkan separator untuk membedakan bagian edit
            Separator editSeparator = new Separator();
            editSeparator.setStyle("-fx-background-color: #BDC3C7;");
            
            Label editLabel = new Label("Update Status Maintenance");
            editLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            editLabel.setTextFill(Color.web("#E74C3C"));
            
            formContainer.getChildren().addAll(
                editSeparator,
                editLabel,
                createFieldContainer("Status:", statusCombo),
                createFieldContainer("Tanggal Selesai:", completedDatePicker),
                createFieldContainer("Teknisi:", technicianField),
                createFieldContainer("Biaya:", costField),
                createFieldContainer("Solusi:", solutionArea)
            );
        }
        
        formContainer.getChildren().add(buttonContainer);
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        // PERBAIKAN: Set konten ke ScrollPane
        scrollPane.setContent(mainContainer);
        
        // PERBAIKAN: Ukuran dialog yang lebih fleksibel
        Scene scene = new Scene(scrollPane, 520, Math.min(700, isEditMode ? 800 : 550));
        dialog.setScene(scene);
        
        // PERBAIKAN: Set minimum size
        dialog.setMinWidth(520);
        dialog.setMinHeight(400);
    }
    
    private TextField createTextField(String prompt, String value) {
        TextField field = new TextField(value);
        field.setPromptText(prompt);
        field.setPrefHeight(35);
        field.setMaxWidth(Double.MAX_VALUE); // PERBAIKAN: Gunakan lebar penuh
        field.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        return field;
    }
    
    private ComboBox<String> createComboBox(String prompt, String[] items) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setPromptText(prompt);
        combo.setPrefHeight(35);
        combo.setMaxWidth(Double.MAX_VALUE); // PERBAIKAN: Gunakan lebar penuh
        combo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        return combo;
    }
    
    private VBox createFieldContainer(String label, javafx.scene.Node field) {
        VBox container = new VBox(5);
        Label fieldLabel = new Label(label);
        fieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        fieldLabel.setTextFill(Color.web("#2C3E50"));
        container.getChildren().addAll(fieldLabel, field);
        return container;
    }
    
    private boolean validateForm(ComboBox<InventoryItem> inventoryCombo, ComboBox<String> issueTypeCombo,
                                ComboBox<String> priorityCombo, TextField reporterField, 
                                TextArea descriptionArea, DatePicker reportedDatePicker) {
        
        if (inventoryCombo.getValue() == null) {
            showAlert("Error", "Alat harus dipilih!", Alert.AlertType.ERROR);
            inventoryCombo.requestFocus();
            return false;
        }
        
        if (issueTypeCombo.getValue() == null) {
            showAlert("Error", "Jenis masalah harus dipilih!", Alert.AlertType.ERROR);
            issueTypeCombo.requestFocus();
            return false;
        }
        
        if (priorityCombo.getValue() == null) {
            showAlert("Error", "Prioritas harus dipilih!", Alert.AlertType.ERROR);
            priorityCombo.requestFocus();
            return false;
        }
        
        if (reporterField.getText().trim().isEmpty()) {
            showAlert("Error", "Pelapor tidak boleh kosong!", Alert.AlertType.ERROR);
            reporterField.requestFocus();
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Error", "Deskripsi masalah tidak boleh kosong!", Alert.AlertType.ERROR);
            descriptionArea.requestFocus();
            return false;
        }
        
        if (reportedDatePicker.getValue() == null) {
            showAlert("Error", "Tanggal lapor harus dipilih!", Alert.AlertType.ERROR);
            reportedDatePicker.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public MaintenanceRecord showAndWait() {
        dialog.showAndWait();
        return result;
    }
}