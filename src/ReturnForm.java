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

public class ReturnForm {
    private Stage dialog;
    private String result;
    private BorrowRecord selectedRecord;
    private ObservableList<BorrowRecord> borrowData;
    
    public ReturnForm(Stage parent, ObservableList<BorrowRecord> borrowData) {
        this.borrowData = borrowData;
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Pengembalian Alat");
        dialog.setResizable(false);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label("Form Pengembalian Alat");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        // Active borrows only
        FilteredList<BorrowRecord> activeBorrows = new FilteredList<>(borrowData, 
            record -> "Dipinjam".equals(record.getStatus()) || "Terlambat".equals(record.getStatus()));
        
        ComboBox<BorrowRecord> borrowCombo = new ComboBox<>();
        borrowCombo.getItems().addAll(activeBorrows);
        borrowCombo.setPromptText("Pilih Peminjaman yang Akan Dikembalikan");
        borrowCombo.setPrefHeight(35);
        borrowCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Custom cell factory to display borrow info
        borrowCombo.setCellFactory(param -> new ListCell<BorrowRecord>() {
            @Override
            protected void updateItem(BorrowRecord record, boolean empty) {
                super.updateItem(record, empty);
                if (empty || record == null) {
                    setText(null);
                } else {
                    String status = "Terlambat".equals(record.getStatus()) ? " [TERLAMBAT]" : "";
                    setText(record.getBorrowId() + " - " + record.getInventoryName() + 
                           " (oleh: " + record.getBorrowerName() + ")" + status);
                }
            }
        });
        
        borrowCombo.setButtonCell(new ListCell<BorrowRecord>() {
            @Override
            protected void updateItem(BorrowRecord record, boolean empty) {
                super.updateItem(record, empty);
                if (empty || record == null) {
                    setText(null);
                } else {
                    setText(record.getBorrowId() + " - " + record.getInventoryName());
                }
            }
        });
        
        // Borrow details display
        VBox detailsContainer = new VBox(10);
        detailsContainer.setPadding(new Insets(15));
        detailsContainer.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 5; -fx-border-color: #DEE2E6; -fx-border-radius: 5;");
        
        Label detailsTitle = new Label("Detail Peminjaman:");
        detailsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        detailsTitle.setTextFill(Color.web("#2C3E50"));
        
        Label borrowerLabel = new Label("Peminjam: -");
        Label borrowDateLabel = new Label("Tanggal Pinjam: -");
        Label returnDateLabel = new Label("Tanggal Kembali: -");
        Label statusLabel = new Label("Status: -");
        
        detailsContainer.getChildren().addAll(detailsTitle, borrowerLabel, borrowDateLabel, returnDateLabel, statusLabel);
        
        // Update details when selection changes
        borrowCombo.setOnAction(e -> {
            BorrowRecord selected = borrowCombo.getValue();
            if (selected != null) {
                borrowerLabel.setText("Peminjam: " + selected.getBorrowerName() + " (" + selected.getBorrowerType() + ")");
                borrowDateLabel.setText("Tanggal Pinjam: " + selected.getBorrowDate());
                returnDateLabel.setText("Tanggal Kembali: " + selected.getReturnDate());
                statusLabel.setText("Status: " + selected.getStatus());
                
                // Color code status
                if ("Terlambat".equals(selected.getStatus())) {
                    statusLabel.setTextFill(Color.web("#E74C3C"));
                    statusLabel.setStyle("-fx-font-weight: bold;");
                } else {
                    statusLabel.setTextFill(Color.web("#27AE60"));
                    statusLabel.setStyle("-fx-font-weight: bold;");
                }
            }
        });
        
        // Return condition
        ComboBox<String> conditionCombo = createComboBox("Kondisi Alat saat Dikembalikan", 
            new String[]{"Baik", "Rusak Ringan", "Rusak Berat", "Hilang"});
        
        // Return notes
        Label notesLabel = new Label("Catatan Pengembalian:");
        notesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        notesLabel.setTextFill(Color.web("#2C3E50"));
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Catatan kondisi alat, masalah yang ditemukan, dll...");
        notesArea.setPrefRowCount(4);
        notesArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        
        Button returnButton = new Button("Kembalikan");
        returnButton.setPrefWidth(100);
        returnButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        returnButton.setOnAction(e -> {
            if (validateForm(borrowCombo, conditionCombo, notesArea)) {
                selectedRecord = borrowCombo.getValue();
                
                String notes = "Kondisi: " + conditionCombo.getValue();
                if (!notesArea.getText().trim().isEmpty()) {
                    notes += "\nCatatan: " + notesArea.getText().trim();
                }
                
                result = notes;
                dialog.close();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(returnButton, cancelButton);
        
        // Add all components to form
        formContainer.getChildren().addAll(
            createFieldContainer("Pilih Peminjaman:", borrowCombo),
            detailsContainer,
            createFieldContainer("Kondisi Alat:", conditionCombo),
            createFieldContainer("Catatan:", notesArea),
            buttonContainer
        );
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        Scene scene = new Scene(mainContainer, 550, 700);
        dialog.setScene(scene);
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
    
    private boolean validateForm(ComboBox<BorrowRecord> borrowCombo, ComboBox<String> conditionCombo, TextArea notesArea) {
        if (borrowCombo.getValue() == null) {
            showAlert("Error", "Peminjaman harus dipilih!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (conditionCombo.getValue() == null) {
            showAlert("Error", "Kondisi alat harus dipilih!", Alert.AlertType.ERROR);
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
    
    public String showAndWait() {
        dialog.showAndWait();
        return result;
    }
    
    public BorrowRecord getSelectedRecord() {
        return selectedRecord;
    }
}