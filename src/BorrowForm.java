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
import javafx.collections.transformation.FilteredList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowForm {
    private Stage dialog;
    private BorrowRecord result;
    private ObservableList<InventoryItem> inventoryData;
    
    public BorrowForm(Stage parent, ObservableList<InventoryItem> inventoryData) {
        this.inventoryData = inventoryData;
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Peminjaman Baru");
        dialog.setResizable(false);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label("Form Peminjaman Alat");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        // ID Field - Auto-generated, shown but disabled
        TextField idField = createTextField("ID Peminjaman (Auto-generated)", "");
        idField.setDisable(true);
        
        // Available items only
        FilteredList<InventoryItem> availableItems = new FilteredList<>(inventoryData, 
            item -> "Tersedia".equals(item.getStatus()));
        
        ComboBox<InventoryItem> inventoryCombo = new ComboBox<>();
        inventoryCombo.getItems().addAll(availableItems);
        inventoryCombo.setPromptText("Pilih Alat yang Tersedia");
        inventoryCombo.setPrefHeight(35);
        inventoryCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Custom cell factory to display item name and ID
        inventoryCombo.setCellFactory(param -> new ListCell<InventoryItem>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getNama() + " (" + item.getLokasi() + ")");
                }
            }
        });
        
        inventoryCombo.setButtonCell(new ListCell<InventoryItem>() {
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
        
        TextField borrowerNameField = createTextField("Nama Peminjam", "");
        
        ComboBox<String> borrowerTypeCombo = createComboBox("Tipe Peminjam", 
            new String[]{"Mahasiswa", "Dosen", "Staff", "Tamu"});
        
        TextField contactField = createTextField("Kontak (Email/No. HP)", "");
        
        DatePicker borrowDatePicker = new DatePicker();
        borrowDatePicker.setPromptText("Tanggal Pinjam");
        borrowDatePicker.setValue(LocalDate.now());
        borrowDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // PERBAIKAN: Batasi pemilihan tanggal peminjaman tidak boleh kurang dari hari ini
        borrowDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
        
        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setPromptText("Tanggal Kembali");
        returnDatePicker.setValue(LocalDate.now().plusWeeks(1)); // Default 1 week
        returnDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // PERBAIKAN: Update tanggal kembali otomatis ketika tanggal pinjam berubah
        borrowDatePicker.setOnAction(e -> {
            LocalDate borrowDate = borrowDatePicker.getValue();
            if (borrowDate != null) {
                // Set tanggal kembali minimum 1 hari setelah tanggal pinjam
                LocalDate minReturnDate = borrowDate.plusDays(1);
                if (returnDatePicker.getValue() == null || returnDatePicker.getValue().isBefore(minReturnDate)) {
                    returnDatePicker.setValue(borrowDate.plusWeeks(1)); // Default 1 minggu
                }
                
                // Update day cell factory untuk return date picker
                returnDatePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (date.isBefore(minReturnDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                });
            }
        });
        
        // Notes field
        Label notesLabel = new Label("Catatan (Opsional):");
        notesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        notesLabel.setTextFill(Color.web("#2C3E50"));
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Tujuan peminjaman, catatan khusus, dll...");
        notesArea.setPrefRowCount(3);
        notesArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        
        Button saveButton = new Button("Simpan");
        saveButton.setPrefWidth(100);
        saveButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        saveButton.setOnAction(e -> {
            if (validateForm(inventoryCombo, borrowerNameField, borrowerTypeCombo, 
                           contactField, borrowDatePicker, returnDatePicker)) {
                
                InventoryItem selectedItem = inventoryCombo.getValue();
                
                result = new BorrowRecord(
                    idField.getText().trim(), // Will be overridden in main app
                    selectedItem.getId(),
                    selectedItem.getNama(),
                    borrowerNameField.getText().trim(),
                    borrowerTypeCombo.getValue(),
                    contactField.getText().trim(),
                    borrowDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    returnDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    "", // No actual return date yet
                    "Dipinjam",
                    notesArea.getText().trim()
                );
                dialog.close();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(saveButton, cancelButton);
        
        // Add all components to form
        formContainer.getChildren().addAll(
            createFieldContainer("ID:", idField),
            createFieldContainer("Alat:", inventoryCombo),
            createFieldContainer("Nama Peminjam:", borrowerNameField),
            createFieldContainer("Tipe Peminjam:", borrowerTypeCombo),
            createFieldContainer("Kontak:", contactField),
            createFieldContainer("Tanggal Pinjam:", borrowDatePicker),
            createFieldContainer("Tanggal Kembali:", returnDatePicker),
            createFieldContainer("Catatan:", notesArea),
            buttonContainer
        );
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        Scene scene = new Scene(mainContainer, 500, 750);
        dialog.setScene(scene);
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
    
    private boolean validateForm(ComboBox<InventoryItem> inventoryCombo, TextField borrowerNameField,
                                ComboBox<String> borrowerTypeCombo, TextField contactField,
                                DatePicker borrowDatePicker, DatePicker returnDatePicker) {
        
        if (inventoryCombo.getValue() == null) {
            showAlert("Error", "Alat harus dipilih!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (borrowerNameField.getText().trim().isEmpty()) {
            showAlert("Error", "Nama peminjam tidak boleh kosong!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (borrowerTypeCombo.getValue() == null) {
            showAlert("Error", "Tipe peminjam harus dipilih!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (contactField.getText().trim().isEmpty()) {
            showAlert("Error", "Kontak tidak boleh kosong!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (borrowDatePicker.getValue() == null) {
            showAlert("Error", "Tanggal pinjam harus dipilih!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (returnDatePicker.getValue() == null) {
            showAlert("Error", "Tanggal kembali harus dipilih!", Alert.AlertType.ERROR);
            return false;
        }
        
        // PERBAIKAN: Validasi tanggal peminjaman tidak boleh kurang dari hari ini
        if (borrowDatePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Error", "Tanggal peminjaman tidak boleh kurang dari hari ini!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (!returnDatePicker.getValue().isAfter(borrowDatePicker.getValue())) {
            showAlert("Error", "Tanggal kembali harus setelah tanggal pinjam!", Alert.AlertType.ERROR);
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
    
    public BorrowRecord showAndWait() {
        dialog.showAndWait();
        return result;
    }
}