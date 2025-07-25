import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;

public class AvailabilityChecker {
    private Stage dialog;
    private ObservableList<InventoryItem> inventoryData;
    private FilteredList<InventoryItem> filteredData;
    
    public AvailabilityChecker(Stage parent, ObservableList<InventoryItem> inventoryData) {
        this.inventoryData = inventoryData;
        this.filteredData = new FilteredList<>(inventoryData, p -> true);
        createDialog(parent);
    }
    
    private void createDialog(Stage parent) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parent);
        dialog.setTitle("Cek Ketersediaan Alat");
        dialog.setResizable(true);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label("Cek Ketersediaan Alat");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Search and Filter Section
        VBox searchContainer = new VBox(15);
        searchContainer.setPadding(new Insets(20));
        searchContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        searchContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label searchLabel = new Label("Pencarian dan Filter");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        searchLabel.setTextFill(Color.web("#2C3E50"));
        
        // Search controls
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nama, kategori, atau merk...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Semua Status", "Tersedia", "Dipinjam", "Maintenance");
        statusFilter.setValue("Tersedia"); // Default to available items
        statusFilter.setStyle("-fx-background-radius: 5;");
        
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Semua Kategori", "Hardware", "Software", "Aksesoris", "Networking", "Furniture");
        categoryFilter.setValue("Semua Kategori");
        categoryFilter.setStyle("-fx-background-radius: 5;");
        
        Button resetButton = new Button("Reset Filter");
        resetButton.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-background-radius: 5;");
        resetButton.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("Tersedia");
            categoryFilter.setValue("Semua Kategori");
        });
        
        searchBox.getChildren().addAll(
            new Label("Cari:"), searchField,
            new Label("Status:"), statusFilter,
            new Label("Kategori:"), categoryFilter,
            resetButton
        );
        
        // Statistics
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label totalLabel = new Label("Total: 0");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        totalLabel.setTextFill(Color.web("#2C3E50"));
        
        Label availableLabel = new Label("Tersedia: 0");
        availableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        availableLabel.setTextFill(Color.web("#27AE60"));
        
        Label borrowedLabel = new Label("Dipinjam: 0");
        borrowedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        borrowedLabel.setTextFill(Color.web("#E67E22"));
        
        Label maintenanceLabel = new Label("Maintenance: 0");
        maintenanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        maintenanceLabel.setTextFill(Color.web("#E74C3C"));
        
        statsBox.getChildren().addAll(totalLabel, availableLabel, borrowedLabel, maintenanceLabel);
        
        searchContainer.getChildren().addAll(searchLabel, searchBox, new Separator(), statsBox);
        
        // Table
        TableView<InventoryItem> table = createAvailabilityTable();
        
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tableContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label tableLabel = new Label("Daftar Inventaris");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tableLabel.setTextFill(Color.web("#2C3E50"));
        
        tableContainer.getChildren().addAll(tableLabel, table);
        
        // Filter functionality
        Runnable updateFilter = () -> {
            filteredData.setPredicate(item -> {
                // Search text filter
                String searchText = searchField.getText();
                if (searchText != null && !searchText.isEmpty()) {
                    String lowerCaseFilter = searchText.toLowerCase();
                    if (!item.getNama().toLowerCase().contains(lowerCaseFilter) &&
                        !item.getKategori().toLowerCase().contains(lowerCaseFilter) &&
                        !item.getMerk().toLowerCase().contains(lowerCaseFilter) &&
                        !item.getId().toLowerCase().contains(lowerCaseFilter)) {
                        return false;
                    }
                }
                
                // Status filter
                String statusValue = statusFilter.getValue();
                if (statusValue != null && !statusValue.equals("Semua Status")) {
                    if (!item.getStatus().equals(statusValue)) {
                        return false;
                    }
                }
                
                // Category filter
                String categoryValue = categoryFilter.getValue();
                if (categoryValue != null && !categoryValue.equals("Semua Kategori")) {
                    if (!item.getKategori().equals(categoryValue)) {
                        return false;
                    }
                }
                
                return true;
            });
            
            // Update statistics
            updateStatistics(totalLabel, availableLabel, borrowedLabel, maintenanceLabel);
        };
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        statusFilter.setOnAction(e -> updateFilter.run());
        categoryFilter.setOnAction(e -> updateFilter.run());
        
        // Initial statistics update
        updateFilter.run();
        
        // Close button
        Button closeButton = new Button("Tutup");
        closeButton.setPrefWidth(100);
        closeButton.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.getChildren().add(closeButton);
        
        mainContainer.getChildren().addAll(headerLabel, searchContainer, tableContainer, buttonBox);
        
        Scene scene = new Scene(mainContainer, 900, 700);
        dialog.setScene(scene);
    }
    
    private TableView<InventoryItem> createAvailabilityTable() {
        TableView<InventoryItem> table = new TableView<>();
        table.setItems(filteredData);
        table.setPrefHeight(300);
        
        TableColumn<InventoryItem, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<InventoryItem, String> namaCol = new TableColumn<>("Nama");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        namaCol.setPrefWidth(200);
        
        TableColumn<InventoryItem, String> kategoriCol = new TableColumn<>("Kategori");
        kategoriCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        kategoriCol.setPrefWidth(100);
        
        TableColumn<InventoryItem, String> merkCol = new TableColumn<>("Merk");
        merkCol.setCellValueFactory(new PropertyValueFactory<>("merk"));
        merkCol.setPrefWidth(100);
        
        TableColumn<InventoryItem, String> kondisiCol = new TableColumn<>("Kondisi");
        kondisiCol.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        kondisiCol.setPrefWidth(100);
        
        TableColumn<InventoryItem, String> lokasiCol = new TableColumn<>("Lokasi");
        lokasiCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        lokasiCol.setPrefWidth(120);
        
        TableColumn<InventoryItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        // Custom cell factory for status column to show colors
        statusCol.setCellFactory(col -> new TableCell<InventoryItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Tersedia":
                            setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                            break;
                        case "Dipinjam":
                            setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");
                            break;
                        case "Maintenance":
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #2C3E50;");
                    }
                }
            }
        });
        
        table.getColumns().addAll(idCol, namaCol, kategoriCol, merkCol, kondisiCol, lokasiCol, statusCol);
        
        return table;
    }
    
    private void updateStatistics(Label totalLabel, Label availableLabel, Label borrowedLabel, Label maintenanceLabel) {
        int total = filteredData.size();
        long available = filteredData.stream().filter(item -> "Tersedia".equals(item.getStatus())).count();
        long borrowed = filteredData.stream().filter(item -> "Dipinjam".equals(item.getStatus())).count();
        long maintenance = filteredData.stream().filter(item -> "Maintenance".equals(item.getStatus())).count();
        
        totalLabel.setText("Total: " + total);
        availableLabel.setText("Tersedia: " + available);
        borrowedLabel.setText("Dipinjam: " + borrowed);
        maintenanceLabel.setText("Maintenance: " + maintenance);
    }
    
    public void showAndWait() {
        dialog.showAndWait();
    }
}