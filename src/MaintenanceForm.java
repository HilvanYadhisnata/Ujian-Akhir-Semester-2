import javafx.collections.ObservableList;
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

import java.time.LocalDate;

public class MaintenanceForm {
    private Stage dialog;
    private MaintenanceRecord result;
    private ObservableList<InventoryItem> inventoryData;
    private MaintenanceRecord existingRecord;
    private boolean isEditMode = false;
    
    public MaintenanceForm(Stage parent, ObservableList<InventoryItem> inventoryData, MaintenanceRecord record) {
        this.inventoryData = inventoryData;
        this.existingRecord = record;
        this.isEditMode = (record != null);
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle(isEditMode ? "Update Status Maintenance" : "Laporan Kerusakan/Maintenance");
        dialog.setResizable(false);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label(isEditMode ? "Update Status Maintenance" : "Laporan Kerusakan/Maintenance");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        if (isEditMode) {
            createUpdateForm(formContainer);
        } else {
            createReportForm(formContainer);
        }
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        Scene scene = new Scene(mainContainer, isEditMode ? 500 : 550, isEditMode ? 400 : 650);
        dialog.setScene(scene);
    }
    
    private void createReportForm(VBox formContainer) {
        // Item selection
        ComboBox<String> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Pilih Alat yang Bermasalah");
        itemCombo.setPrefHeight(35);
        itemCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Populate with inventory items
        for (InventoryItem item : inventoryData) {
            itemCombo.getItems().add(item.getId() + " - " + item.getNama() + " (" + item.getMerk() + ")");
        }
        
        // Issue type
        ComboBox<String> issueTypeCombo = createComboBox("Jenis Masalah", 
            new String[]{"Kerusakan", "Maintenance Rutin", "Upgrade", "Kalibrasi"});
        
        // Priority
        ComboBox<String> priorityCombo = createComboBox("Prioritas", 
            new String[]{"Rendah", "Sedang", "Tinggi", "Urgent"});
        priorityCombo.setValue("Sedang");
        
        // Description
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Deskrisi detail masalah atau pekerjaan yang diperlukan...");
        descriptionArea.setPrefHeight(100);
        descriptionArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Reporter info
        TextField reporterField = createTextField("Nama Pelapor", "");
        
        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        
        Button reportButton = new Button("Laporkan");
        reportButton.setPrefWidth(100);
        reportButton.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        reportButton.setOnAction(e -> {
            if (validateReportForm(itemCombo, issueTypeCombo, priorityCombo, descriptionArea, reporterField)) {
                String selectedItem = itemCombo.getValue();
                String itemId = selectedItem.split(" - ")[0];
                InventoryItem item = inventoryData.stream()
                    .filter(inv -> inv.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
                
                if (item != null) {
                    result = new MaintenanceRecord(
                        "", // Will be generated in main app
                        item.getId(),
                        item.getNama(),
                        issueTypeCombo.getValue(),
                        descriptionArea.getText().trim(),
                        reporterField.getText().trim(),
                        LocalDate.now().toString(),
                        priorityCombo.getValue(),
                        "Dilaporkan",
                        "",
                        "",
                        "",
                        ""
                    );
                    dialog.close();
                }
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(reportButton, cancelButton);
        
        formContainer.getChildren().addAll(
            createFieldContainer("Pilih Alat:", itemCombo),
            createFieldContainer("Jenis Masalah:", issueTypeCombo),
            createFieldContainer("Prioritas:", priorityCombo),
            createFieldContainer("Deskripsi:", descriptionArea),
            createFieldContainer("Nama Pelapor:", reporterField),
            buttonContainer
        );
    }
    
    private void createUpdateForm(VBox formContainer) {
        // Show existing info
        TextArea infoArea = new TextArea();
        infoArea.setText(String.format(
            "ID: %s\nAlat: %s\nJenis: %s\nDeskripsi: %s\nPelapor: %s\nTanggal: %s\nPrioritas: %s",
            existingRecord.getMaintenanceId(),
            existingRecord.getInventoryName(),
            existingRecord.getIssueType(),
            existingRecord.getDescription(),
            existingRecord.getReportedBy(),
            existingRecord.getReportedDate(),
            existingRecord.getPriority()
        ));
        infoArea.setEditable(false);
        infoArea.setPrefHeight(120);
        infoArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Status update
        ComboBox<String> statusCombo = createComboBox("Status Baru", 
            new String[]{"Dilaporkan", "Dalam Perbaikan", "Selesai", "Ditunda"});
        statusCombo.setValue(existingRecord.getStatus());
        
        // Technician
        TextField technicianField = createTextField("Teknisi", existingRecord.getTechnician());
        
        // Cost
        TextField costField = createTextField("Biaya (Rp)", existingRecord.getCost());
        
        // Notes
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Catatan tambahan...");
        notesArea.setPrefHeight(60);
        notesArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        
        Button updateButton = new Button("Update");
        updateButton.setPrefWidth(100);
        updateButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        updateButton.setOnAction(e -> {
            if (validateUpdateForm(statusCombo)) {
                // Update existing record
                existingRecord.setStatus(statusCombo.getValue());
                existingRecord.setTechnician(technicianField.getText().trim());
                existingRecord.setCost(costField.getText().trim());
                
                if ("Selesai".equals(statusCombo.getValue())) {
                    existingRecord.setCompletedDate(LocalDate.now().toString());
                }
                
                String newNotes = notesArea.getText().trim();
                if (!newNotes.isEmpty()) {
                    String existingNotes = existingRecord.getNotes();
                    if (existingNotes != null && !existingNotes.isEmpty()) {
                        existingRecord.setNotes(existingNotes + " | " + newNotes);
                    } else {
                        existingRecord.setNotes(newNotes);
                    }
                }
                
                result = existingRecord;
                dialog.close();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(updateButton, cancelButton);
        
        formContainer.getChildren().addAll(
            createFieldContainer("Info Maintenance:", infoArea),
            createFieldContainer("Status:", statusCombo),
            createFieldContainer("Teknisi:", technicianField),
            createFieldContainer("Biaya:", costField),
            createFieldContainer("Catatan:", notesArea),
            buttonContainer
        );
    }
    
    private TextField createTextField(String prompt, String value) {
        TextField field = new TextField(value);
        field.setPromptText(prompt);
        field.setPrefHeight(35);
        field.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        return field;
    }
    
    private ComboBox<String> createComboBox(String prompt, String[] items) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setPromptText(prompt);
        combo.setPrefHeight(35);
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
    
    private boolean validateReportForm(ComboBox<String> itemCombo, ComboBox<String> issueTypeCombo,
                                     ComboBox<String> priorityCombo, TextArea descriptionArea, TextField reporterField) {
        
        if (itemCombo.getValue() == null) {
            showAlert("Error", "Pilih alat yang bermasalah!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (issueTypeCombo.getValue() == null) {
            showAlert("Error", "Pilih jenis masalah!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (priorityCombo.getValue() == null) {
            showAlert("Error", "Pilih prioritas!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Error", "Deskripsi tidak boleh kosong!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (reporterField.getText().trim().isEmpty()) {
            showAlert("Error", "Nama pelapor tidak boleh kosong!", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }
    
    private boolean validateUpdateForm(ComboBox<String> statusCombo) {
        if (statusCombo.getValue() == null) {
            showAlert("Error", "Status harus dipilih!", Alert.AlertType.ERROR);
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