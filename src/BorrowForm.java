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
        dialog.setTitle("Form Peminjaman Alat");
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
        
        // Available items selection
        ComboBox<String> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Pilih Alat yang Akan Dipinjam");
        itemCombo.setPrefHeight(35);
        itemCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Populate with available items
        for (InventoryItem item : inventoryData) {
            if ("Tersedia".equals(item.getStatus()) && "Baik".equals(item.getKondisi())) {
                itemCombo.getItems().add(item.getId() + " - " + item.getNama() + " (" + item.getMerk() + ")");
            }
        }
        
        // Item details display
        TextArea itemDetailsArea = new TextArea();
        itemDetailsArea.setPromptText("Detail alat akan ditampilkan di sini...");
        itemDetailsArea.setPrefHeight(80);
        itemDetailsArea.setEditable(false);
        itemDetailsArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Update item details when selection changes
        itemCombo.setOnAction(e -> {
            String selectedItem = itemCombo.getValue();
            if (selectedItem != null) {
                String itemId = selectedItem.split(" - ")[0];
                InventoryItem item = inventoryData.stream()
                    .filter(inv -> inv.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
                
                if (item != null) {
                    itemDetailsArea.setText(String.format(
                        "ID: %s\nNama: %s\nKategori: %s\nMerk: %s\nKondisi: %s\nLokasi: %s",
                        item.getId(), item.getNama(), item.getKategori(),
                        item.getMerk(), item.getKondisi(), item.getLokasi()
                    ));
                }
            }
        });
        
        // Borrower information
        TextField borrowerNameField = createTextField("Nama Peminjam", "");
        
        ComboBox<String> borrowerTypeCombo = createComboBox("Tipe Peminjam", 
            new String[]{"Mahasiswa", "Dosen", "Staff", "Peneliti"});
        
        // Dates
        DatePicker borrowDatePicker = new DatePicker(LocalDate.now());
        borrowDatePicker.setPromptText("Tanggal Peminjaman");
        borrowDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        borrowDatePicker.setDisable(true); // Today's date, not editable
        
        DatePicker returnDatePicker = new DatePicker(LocalDate.now().plusDays(7)); // Default 7 days
        returnDatePicker.setPromptText("Tanggal Pengembalian (Rencana)");
        returnDatePicker.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Notes
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Catatan (opsional)");
        notesArea.setPrefHeight(60);
        notesArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        
        Button borrowButton = new Button("Pinjam");
        borrowButton.setPrefWidth(100);
        borrowButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        borrowButton.setOnAction(e -> {
            if (validateForm(itemCombo, borrowerNameField, borrowerTypeCombo, returnDatePicker)) {
                String selectedItem = itemCombo.getValue();
                String itemId = selectedItem.split(" - ")[0];
                InventoryItem item = inventoryData.stream()
                    .filter(inv -> inv.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
                
                if (item != null) {
                    result = new BorrowRecord(
                        "", // Will be generated in main app
                        item.getId(),
                        item.getNama(),
                        borrowerNameField.getText().trim(),
                        borrowerTypeCombo.getValue(),
                        borrowDatePicker.getValue().toString(),
                        returnDatePicker.getValue().toString(),
                        "", // No actual return date yet
                        "Dipinjam",
                        notesArea.getText().trim()
                    );
                    dialog.close();
                }
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(borrowButton, cancelButton);
        
        // Add all components to form
        formContainer.getChildren().addAll(
            createFieldContainer("Pilih Alat:", itemCombo),
            createFieldContainer("Detail Alat:", itemDetailsArea),
            createFieldContainer("Nama Peminjam:", borrowerNameField),
            createFieldContainer("Tipe Peminjam:", borrowerTypeCombo),
            createFieldContainer("Tanggal Peminjaman:", borrowDatePicker),
            createFieldContainer("Tanggal Pengembalian:", returnDatePicker),
            createFieldContainer("Catatan:", notesArea),
            buttonContainer
        );
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        Scene scene = new Scene(mainContainer, 500, 700);
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
    
    private boolean validateForm(ComboBox<String> itemCombo, TextField borrowerNameField,
                                ComboBox<String> borrowerTypeCombo, DatePicker returnDatePicker) {
        
        if (itemCombo.getValue() == null) {
            showAlert("Error", "Pilih alat yang akan dipinjam!", Alert.AlertType.ERROR);
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
        
        if (returnDatePicker.getValue() == null) {
            showAlert("Error", "Tanggal pengembalian harus dipilih!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (returnDatePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Error", "Tanggal pengembalian tidak boleh sebelum hari ini!", Alert.AlertType.ERROR);
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