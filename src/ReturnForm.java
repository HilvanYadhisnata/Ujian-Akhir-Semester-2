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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ReturnForm {
    private Stage dialog;
    private String result; // Return notes
    private ObservableList<BorrowRecord> borrowData;
    private BorrowRecord selectedRecord;
    
    public ReturnForm(Stage parent, ObservableList<BorrowRecord> borrowData) {
        this.borrowData = borrowData;
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Form Pengembalian Alat");
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
        
        // Borrowed items selection
        ComboBox<String> borrowCombo = new ComboBox<>();
        borrowCombo.setPromptText("Pilih Item yang Akan Dikembalikan");
        borrowCombo.setPrefHeight(35);
        borrowCombo.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Populate with borrowed items
        for (BorrowRecord record : borrowData) {
            if ("Dipinjam".equals(record.getStatus()) || "Terlambat".equals(record.getStatus())) {
                String displayText = String.format("%s - %s (Peminjam: %s)", 
                    record.getInventoryId(), record.getInventoryName(), record.getBorrowerName());
                borrowCombo.getItems().add(displayText);
            }
        }
        
        // Borrow details display
        TextArea borrowDetailsArea = new TextArea();
        borrowDetailsArea.setPromptText("Detail peminjaman akan ditampilkan di sini...");
        borrowDetailsArea.setPrefHeight(120);
        borrowDetailsArea.setEditable(false);
        borrowDetailsArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Status indicator
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setWrapText(true);
        
        // Update details when selection changes
        borrowCombo.setOnAction(e -> {
            String selectedItem = borrowCombo.getValue();
            if (selectedItem != null) {
                String borrowId = selectedItem.split(" - ")[0];
                selectedRecord = borrowData.stream()
                    .filter(record -> record.getInventoryId().equals(borrowId) && 
                            ("Dipinjam".equals(record.getStatus()) || "Terlambat".equals(record.getStatus())))
                    .findFirst()
                    .orElse(null);
                
                if (selectedRecord != null) {
                    updateBorrowDetails(borrowDetailsArea, statusLabel, selectedRecord);
                }
            }
        });
        
        // Return condition
        ComboBox<String> conditionCombo = createComboBox("Kondisi Alat Saat Dikembalikan", 
            new String[]{"Baik", "Rusak", "Perlu Perbaikan"});
        conditionCombo.setValue("Baik"); // Default
        
        // Return notes
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Catatan pengembalian (kondisi alat, masalah yang ditemukan, dll.)");
        notesArea.setPrefHeight(80);
        notesArea.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        // Buttons
        HBox buttonContainer = new HBox(15);
        buttonContainer.setStyle("-fx-alignment: center;");
        
        Button returnButton = new Button("Kembalikan");
        returnButton.setPrefWidth(120);
        returnButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button cancelButton = new Button("Batal");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Button actions
        returnButton.setOnAction(e -> {
            if (validateForm(borrowCombo, conditionCombo)) {
                // Create return notes
                StringBuilder notes = new StringBuilder();
                if (!notesArea.getText().trim().isEmpty()) {
                    notes.append(notesArea.getText().trim()).append(" | ");
                }
                notes.append("Kondisi: ").append(conditionCombo.getValue());
                
                result = notes.toString();
                dialog.close();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonContainer.getChildren().addAll(returnButton, cancelButton);
        
        // Add all components to form
        formContainer.getChildren().addAll(
            createFieldContainer("Pilih Item:", borrowCombo),
            createFieldContainer("Detail Peminjaman:", borrowDetailsArea),
            statusLabel,
            new Separator(),
            createFieldContainer("Kondisi Alat:", conditionCombo),
            createFieldContainer("Catatan Pengembalian:", notesArea),
            buttonContainer
        );
        
        mainContainer.getChildren().addAll(headerLabel, formContainer);
        
        Scene scene = new Scene(mainContainer, 550, 700);
        dialog.setScene(scene);
    }
    
    private void updateBorrowDetails(TextArea detailsArea, Label statusLabel, BorrowRecord record) {
        // Calculate days
        LocalDate borrowDate = LocalDate.parse(record.getBorrowDate());
        LocalDate returnDate = LocalDate.parse(record.getReturnDate());
        LocalDate today = LocalDate.now();
        
        long daysBorrowed = ChronoUnit.DAYS.between(borrowDate, today);
        long daysOverdue = today.isAfter(returnDate) ? ChronoUnit.DAYS.between(returnDate, today) : 0;
        
        String details = String.format(
            "ID Peminjaman: %s\n" +
            "Alat: %s\n" +
            "Peminjam: %s (%s)\n" +
            "Tanggal Pinjam: %s\n" +
            "Tanggal Kembali (Rencana): %s\n" +
            "Lama Dipinjam: %d hari\n" +
            "Status: %s",
            record.getBorrowId(),
            record.getInventoryName(),
            record.getBorrowerName(),
            record.getBorrowerType(),
            record.getBorrowDate(),
            record.getReturnDate(),
            daysBorrowed,
            record.getStatus()
        );
        
        if (!record.getNotes().isEmpty()) {
            details += "\nCatatan Peminjaman: " + record.getNotes();
        }
        
        detailsArea.setText(details);
        
        // Update status label
        if (daysOverdue > 0) {
            statusLabel.setText("⚠️ TERLAMBAT " + daysOverdue + " HARI! Kemungkinan ada denda.");
            statusLabel.setTextFill(Color.web("#E74C3C"));
        } else {
            long daysLeft = ChronoUnit.DAYS.between(today, returnDate);
            if (daysLeft == 0) {
                statusLabel.setText("📅 Hari terakhir pengembalian adalah hari ini.");
                statusLabel.setTextFill(Color.web("#E67E22"));
            } else if (daysLeft > 0) {
                statusLabel.setText("✅ Pengembalian tepat waktu. Sisa waktu: " + daysLeft + " hari.");
                statusLabel.setTextFill(Color.web("#27AE60"));
            }
        }
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
    
    private boolean validateForm(ComboBox<String> borrowCombo, ComboBox<String> conditionCombo) {
        if (borrowCombo.getValue() == null) {
            showAlert("Error", "Pilih item yang akan dikembalikan!", Alert.AlertType.ERROR);
            return false;
        }
        
        if (conditionCombo.getValue() == null) {
            showAlert("Error", "Pilih kondisi alat saat dikembalikan!", Alert.AlertType.ERROR);
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
    
    public BorrowRecord getSelectedRecord() {
        return selectedRecord;
    }
    
    public String showAndWait() {
        dialog.showAndWait();
        return result;
    }
}