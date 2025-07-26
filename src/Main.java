import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main extends Application {
    private Stage primaryStage;
    private String currentUser;
    private String currentRole;
    private VBox currentMainContent;
    private ObservableList<InventoryItem> inventoryData;
    private ObservableList<BorrowRecord> borrowData;
    private ObservableList<MaintenanceRecord> maintenanceData;
    private TableView<InventoryItem> inventoryTable;
    private FilteredList<InventoryItem> filteredData;
    
    // Data pengguna (username:password:role)
    private Map<String, String[]> users = new HashMap<>();
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUsers();
        loadAllData();
        checkMaintenanceSchedule(); // Check for routine maintenance
        showLoginPage();
    }
    
    private void initializeUsers() {
        users.put("admin", new String[]{"admin123", "Admin"});
        users.put("petugas", new String[]{"petugas123", "Petugas Lab"});
    }
    
    private void loadAllData() {
        inventoryData = CSVManager.loadInventoryData();
        borrowData = BorrowManager.loadBorrowData();
        maintenanceData = MaintenanceManager.loadMaintenanceData();
        filteredData = new FilteredList<>(inventoryData, p -> true);
        
        // Update inventory status based on borrowing and maintenance
        updateInventoryStatus();
    }
    
    private void updateInventoryStatus() {
        for (InventoryItem item : inventoryData) {
            // Check if borrowed
            if (BorrowManager.isItemBorrowed(borrowData, item.getId())) {
                item.setStatus("Dipinjam");
            }
            // Check if under maintenance
            else if (MaintenanceManager.isItemUnderMaintenance(maintenanceData, item.getId())) {
                item.setStatus("Maintenance");
            }
            // If not borrowed or maintenance, set as available (if condition is good)
            else if ("Baik".equals(item.getKondisi())) {
                item.setStatus("Tersedia");
            }
        }
        
        // Check for overdue items
        BorrowManager.getOverdueItems(borrowData);
        
        // Save updated status
        CSVManager.saveInventoryData(inventoryData);
    }
    
    private void checkMaintenanceSchedule() {
        List<InventoryItem> itemsNeedingMaintenance = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (InventoryItem item : inventoryData) {
            try {
                LocalDate itemDate = LocalDate.parse(item.getTanggalMasuk());
                long monthsSinceAcquisition = ChronoUnit.MONTHS.between(itemDate, today);
                
                // Check if item needs routine maintenance (every 6 months)
                if (monthsSinceAcquisition > 0 && monthsSinceAcquisition % 6 == 0) {
                    // Check if maintenance already scheduled for this period
                    boolean alreadyScheduled = maintenanceData.stream()
                        .anyMatch(m -> m.getInventoryId().equals(item.getId()) && 
                                 m.getIssueType().equals("Maintenance Rutin") &&
                                 m.getReportedDate().equals(today.toString()));
                    
                    if (!alreadyScheduled && !item.getStatus().equals("Maintenance")) {
                        itemsNeedingMaintenance.add(item);
                    }
                }
            } catch (Exception e) {
                // Skip items with invalid dates
            }
        }
        
        // Create automatic maintenance records
        for (InventoryItem item : itemsNeedingMaintenance) {
            MaintenanceRecord autoMaintenance = new MaintenanceRecord(
                MaintenanceManager.generateNewMaintenanceId(maintenanceData),
                item.getId(),
                item.getNama(),
                "Maintenance Rutin",
                "Sedang",
                "Sistem Otomatis",
                "Maintenance rutin terjadwal (setiap 6 bulan)",
                today.toString(),
                null,
                "Menunggu Penanganan"
            );
            
            maintenanceData.add(autoMaintenance);
            item.setStatus("Maintenance");
        }
        
        if (!itemsNeedingMaintenance.isEmpty()) {
            MaintenanceManager.saveMaintenanceData(maintenanceData);
            CSVManager.saveInventoryData(inventoryData);
        }
    }
    
    private void showLoginPage() {
        VBox loginContainer = new VBox(20);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setPadding(new Insets(40));
        
        // Header
        Label titleLabel = new Label("Lab Inventory System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Label subtitleLabel = new Label("Sistem Pengelolaan Inventaris Laboratorium");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.web("#7F8C8D"));
        
        // Login Form
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        formBox.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));
        
        Label loginLabel = new Label("Login");
        loginLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        loginLabel.setTextFill(Color.web("#2C3E50"));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);
        usernameField.setPrefHeight(35);
        usernameField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);
        passwordField.setPrefHeight(35);
        passwordField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(250);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Login button action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (authenticateUser(username, password)) {
                showDashboard();
            } else {
                showAlert("Login Gagal", "Username atau password salah!", Alert.AlertType.ERROR);
            }
        });
        
        // Enter key support
        passwordField.setOnAction(e -> loginButton.fire());
        
        formBox.getChildren().addAll(loginLabel, usernameField, passwordField, loginButton);
        loginContainer.getChildren().addAll(titleLabel, subtitleLabel, formBox);
        
        // Background gradient
        loginContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #ECF0F1, #BDC3C7);");
        
        Scene loginScene = new Scene(loginContainer, 600, 500);
        primaryStage.setTitle("Lab Inventory System - Login");
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private boolean authenticateUser(String username, String password) {
        if (users.containsKey(username)) {
            String[] userData = users.get(username);
            if (userData[0].equals(password)) {
                currentUser = username;
                currentRole = userData[1];
                return true;
            }
        }
        return false;
    }
    
    private void showDashboard() {
        BorderPane dashboardLayout = new BorderPane();
        
        // Header
        HBox header = createHeader();
        dashboardLayout.setTop(header);
        
        // Sidebar
        VBox sidebar = createSidebar();
        dashboardLayout.setLeft(sidebar);
        
        // Main content
        currentMainContent = createMainContent();
        dashboardLayout.setCenter(currentMainContent);
        
        Scene dashboardScene = new Scene(dashboardLayout, 1200, 800);
        primaryStage.setTitle("Lab Inventory System - Dashboard");
        primaryStage.setScene(dashboardScene);
        primaryStage.setMaximized(true);
        
        // Fade transition
        FadeTransition fade = new FadeTransition(Duration.millis(500), dashboardLayout);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #2C3E50;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Lab Inventory System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Maintenance notification
        long pendingMaintenance = maintenanceData.stream()
            .filter(m -> "Menunggu Penanganan".equals(m.getStatus()) || "Dalam Proses".equals(m.getStatus()))
            .count();
        
        if (pendingMaintenance > 0) {
            Label notificationLabel = new Label("🔔 " + pendingMaintenance + " maintenance pending");
            notificationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            notificationLabel.setTextFill(Color.web("#E74C3C"));
            notificationLabel.setStyle("-fx-background-color: #FCF3CF; -fx-padding: 5; -fx-background-radius: 5;");
            header.getChildren().add(notificationLabel);
        }
        
        Label userLabel = new Label("Welcome, " + currentRole + " (" + currentUser + ")");
        userLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        userLabel.setTextFill(Color.web("#ECF0F1"));
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> showLoginPage());
        
        header.getChildren().addAll(titleLabel, spacer);
        if (pendingMaintenance > 0) {
            Label notificationLabel = new Label("🔔 " + pendingMaintenance + " maintenance pending");
            notificationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            notificationLabel.setTextFill(Color.web("#E74C3C"));
            notificationLabel.setStyle("-fx-background-color: #FCF3CF; -fx-padding: 5; -fx-background-radius: 5; -fx-text-fill: #8B4513;");
            header.getChildren().add(notificationLabel);
        }
        header.getChildren().addAll(userLabel, logoutButton);
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #34495E;");
        
        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        menuLabel.setTextFill(Color.web("#BDC3C7"));
        
        Button dashboardBtn = createMenuButton("📊 Dashboard", true);
        Button inventoryBtn = createMenuButton("📦 Data Inventaris");
        Button checkBtn = createMenuButton("🔍 Cek Ketersediaan");
        Button borrowBtn = createMenuButton("📋 Peminjaman");
        Button returnBtn = createMenuButton("↩️ Pengembalian");
        Button maintenanceBtn = createMenuButton("🔧 Maintenance");
        Button reportBtn = createMenuButton("📄 Laporan");
        
        // Menu actions
        dashboardBtn.setOnAction(e -> switchToContent(createMainContent(), dashboardBtn));
        inventoryBtn.setOnAction(e -> switchToContent(createInventoryContent(), inventoryBtn));
        checkBtn.setOnAction(e -> switchToContent(createAvailabilityContent(), checkBtn));
        borrowBtn.setOnAction(e -> switchToContent(createBorrowingContent(), borrowBtn));
        returnBtn.setOnAction(e -> switchToContent(createReturnContent(), returnBtn));
        maintenanceBtn.setOnAction(e -> switchToContent(createMaintenanceContent(), maintenanceBtn));
        reportBtn.setOnAction(e -> switchToContent(createReportContent(), reportBtn));
        
        sidebar.getChildren().addAll(menuLabel, dashboardBtn, inventoryBtn, checkBtn, borrowBtn, returnBtn, maintenanceBtn, reportBtn);
        return sidebar;
    }
    
    private void switchToContent(VBox newContent, Button activeButton) {
        // Update active button style
        VBox sidebar = (VBox) ((BorderPane) primaryStage.getScene().getRoot()).getLeft();
        for (int i = 1; i < sidebar.getChildren().size(); i++) {
            Button btn = (Button) sidebar.getChildren().get(i);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ECF0F1; -fx-background-radius: 5;");
        }
        activeButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Switch content
        ((BorderPane) primaryStage.getScene().getRoot()).setCenter(newContent);
        currentMainContent = newContent;
    }
    
    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.setPrefWidth(210);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        
        if (active) {
            button.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ECF0F1; -fx-background-radius: 5;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-background-radius: 5;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #ECF0F1; -fx-background-radius: 5;"));
        }
        
        return button;
    }
    
    private Button createMenuButton(String text) {
        return createMenuButton(text, false);
    }
    
    // AVAILABILITY CONTENT - No longer popup
    private VBox createAvailabilityContent() {
        VBox availabilityContent = new VBox(20);
        availabilityContent.setPadding(new Insets(30));
        availabilityContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label("Cek Ketersediaan Alat");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
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
        
        // Create filtered data for availability checking
        FilteredList<InventoryItem> availabilityFilteredData = new FilteredList<>(inventoryData, p -> true);
        
        // Table
        TableView<InventoryItem> table = createAvailabilityTable(availabilityFilteredData);
        
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tableContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label tableLabel = new Label("Daftar Inventaris");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tableLabel.setTextFill(Color.web("#2C3E50"));
        
        tableContainer.getChildren().addAll(tableLabel, table);
        
        // Filter functionality
        Runnable updateFilter = () -> {
            availabilityFilteredData.setPredicate(item -> {
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
            updateAvailabilityStatistics(availabilityFilteredData, totalLabel, availableLabel, borrowedLabel, maintenanceLabel);
        };
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        statusFilter.setOnAction(e -> updateFilter.run());
        categoryFilter.setOnAction(e -> updateFilter.run());
        resetButton.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("Tersedia");
            categoryFilter.setValue("Semua Kategori");
        });
        
        // Initial statistics update
        updateFilter.run();
        
        availabilityContent.getChildren().addAll(headerLabel, searchContainer, tableContainer);
        
        return availabilityContent;
    }
    
    private TableView<InventoryItem> createAvailabilityTable(FilteredList<InventoryItem> data) {
        TableView<InventoryItem> table = new TableView<>();
        table.setItems(data);
        table.setPrefHeight(400);
        
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
    
    private void updateAvailabilityStatistics(FilteredList<InventoryItem> data, Label totalLabel, Label availableLabel, Label borrowedLabel, Label maintenanceLabel) {
        int total = data.size();
        long available = data.stream().filter(item -> "Tersedia".equals(item.getStatus())).count();
        long borrowed = data.stream().filter(item -> "Dipinjam".equals(item.getStatus())).count();
        long maintenance = data.stream().filter(item -> "Maintenance".equals(item.getStatus())).count();
        
        totalLabel.setText("Total: " + total);
        availableLabel.setText("Tersedia: " + available);
        borrowedLabel.setText("Dipinjam: " + borrowed);
        maintenanceLabel.setText("Maintenance: " + maintenance);
    }
    
    // REPORT CONTENT - No longer popup
    private VBox createReportContent() {
        VBox reportContent = new VBox(20);
        reportContent.setPadding(new Insets(30));
        reportContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        Label headerLabel = new Label("📄 Laporan Sistem");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web("#2C3E50"));
        
        // Report Options
        VBox optionsContainer = new VBox(15);
        optionsContainer.setPadding(new Insets(20));
        optionsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        optionsContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label optionsLabel = new Label("Pilih Jenis Laporan");
        optionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        optionsLabel.setTextFill(Color.web("#2C3E50"));
        
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        
        Button inventoryReportBtn = new Button("📦 Laporan Inventaris");
        inventoryReportBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-pref-width: 160;");
        
        Button borrowReportBtn = new Button("📋 Laporan Peminjaman");
        borrowReportBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-pref-width: 160;");
        
        Button maintenanceReportBtn = new Button("🔧 Laporan Maintenance");
        maintenanceReportBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-pref-width: 160;");
        
        Button summaryReportBtn = new Button("📊 Laporan Ringkasan");
        summaryReportBtn.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-pref-width: 160;");
        
        buttonContainer.getChildren().addAll(inventoryReportBtn, borrowReportBtn, maintenanceReportBtn, summaryReportBtn);
        
        // Report display area
        VBox reportDisplayArea = new VBox(10);
        reportDisplayArea.setPadding(new Insets(20));
        reportDisplayArea.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        reportDisplayArea.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label reportTitleLabel = new Label("Pilih jenis laporan untuk ditampilkan");
        reportTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        reportTitleLabel.setTextFill(Color.web("#7F8C8D"));
        reportTitleLabel.setAlignment(Pos.CENTER);
        
        reportDisplayArea.getChildren().add(reportTitleLabel);
        
        // Button actions
        inventoryReportBtn.setOnAction(e -> showInventoryReport(reportDisplayArea));
        borrowReportBtn.setOnAction(e -> showBorrowingReport(reportDisplayArea));
        maintenanceReportBtn.setOnAction(e -> showMaintenanceReport(reportDisplayArea));
        summaryReportBtn.setOnAction(e -> showSummaryReport(reportDisplayArea));
        
        optionsContainer.getChildren().addAll(optionsLabel, buttonContainer);
        reportContent.getChildren().addAll(headerLabel, optionsContainer, reportDisplayArea);
        
        return reportContent;
    }
    
    private void showInventoryReport(VBox displayArea) {
        displayArea.getChildren().clear();
        
        Label titleLabel = new Label("📦 Laporan Inventaris");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        // Summary statistics
        VBox statsBox = new VBox(10);
        long totalItems = inventoryData.size();
        long availableItems = inventoryData.stream().filter(item -> "Tersedia".equals(item.getStatus())).count();
        long borrowedItems = inventoryData.stream().filter(item -> "Dipinjam".equals(item.getStatus())).count();
        long maintenanceItems = inventoryData.stream().filter(item -> "Maintenance".equals(item.getStatus())).count();
        
        Label statsLabel = new Label(String.format(
            "Total Item: %d | Tersedia: %d | Dipinjam: %d | Maintenance: %d",
            totalItems, availableItems, borrowedItems, maintenanceItems
        ));
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsLabel.setTextFill(Color.web("#34495E"));
        
        // Table with all inventory data
        TableView<InventoryItem> reportTable = new TableView<>();
        reportTable.setItems(inventoryData);
        reportTable.setPrefHeight(400);
        
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
        
        TableColumn<InventoryItem, String> tanggalCol = new TableColumn<>("Tanggal Masuk");
        tanggalCol.setCellValueFactory(new PropertyValueFactory<>("tanggalMasuk"));
        tanggalCol.setPrefWidth(120);
        
        reportTable.getColumns().addAll(idCol, namaCol, kategoriCol, merkCol, kondisiCol, lokasiCol, statusCol, tanggalCol);
        
        displayArea.getChildren().addAll(titleLabel, statsLabel, new Separator(), reportTable);
    }
    
private void showBorrowingReport(VBox displayArea) {
    displayArea.getChildren().clear();
    
    // Header with enhanced report button
    HBox headerBox = new HBox(20);
    headerBox.setAlignment(Pos.CENTER_LEFT);
    
    Label titleLabel = new Label("📋 Laporan Peminjaman");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
    titleLabel.setTextFill(Color.web("#2C3E50"));
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Button printReportButton = new Button("🖨️ Cetak & Export");
    printReportButton.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
    printReportButton.setOnAction(e -> openBorrowingReportGenerator());
    
    headerBox.getChildren().addAll(titleLabel, spacer, printReportButton);
    
    // Summary statistics
    long totalBorrows = borrowData.size();
    long activeBorrows = borrowData.stream().filter(b -> "Dipinjam".equals(b.getStatus())).count();
    long overdueBorrows = borrowData.stream().filter(b -> "Terlambat".equals(b.getStatus())).count();
    long returnedBorrows = borrowData.stream().filter(b -> "Dikembalikan".equals(b.getStatus())).count();
    
    Label statsLabel = new Label(String.format(
        "Total Peminjaman: %d | Aktif: %d | Terlambat: %d | Dikembalikan: %d",
        totalBorrows, activeBorrows, overdueBorrows, returnedBorrows
    ));
    statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    statsLabel.setTextFill(Color.web("#34495E"));
    
    // Table with borrowing data
    TableView<BorrowRecord> reportTable = new TableView<>();
    reportTable.setItems(borrowData);
    reportTable.setPrefHeight(400);
    
    TableColumn<BorrowRecord, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
    idCol.setPrefWidth(80);
    
    TableColumn<BorrowRecord, String> itemCol = new TableColumn<>("Alat");
    itemCol.setCellValueFactory(new PropertyValueFactory<>("inventoryName"));
    itemCol.setPrefWidth(200);
    
    TableColumn<BorrowRecord, String> borrowerCol = new TableColumn<>("Peminjam");
    borrowerCol.setCellValueFactory(new PropertyValueFactory<>("borrowerName"));
    borrowerCol.setPrefWidth(150);
    
    TableColumn<BorrowRecord, String> typeCol = new TableColumn<>("Tipe");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("borrowerType"));
    typeCol.setPrefWidth(100);
    
    TableColumn<BorrowRecord, String> borrowDateCol = new TableColumn<>("Tgl Pinjam");
    borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
    borrowDateCol.setPrefWidth(100);
    
    TableColumn<BorrowRecord, String> returnDateCol = new TableColumn<>("Tgl Kembali");
    returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
    returnDateCol.setPrefWidth(100);
    
    TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(100);
    
    reportTable.getColumns().addAll(idCol, itemCol, borrowerCol, typeCol, borrowDateCol, returnDateCol, statusCol);
    
    displayArea.getChildren().addAll(headerBox, statsLabel, new Separator(), reportTable);
}
    
   private void showMaintenanceReport(VBox displayArea) {
    displayArea.getChildren().clear();
    
    // Header with enhanced report button
    HBox headerBox = new HBox(20);
    headerBox.setAlignment(Pos.CENTER_LEFT);
    
    Label titleLabel = new Label("🔧 Laporan Maintenance");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
    titleLabel.setTextFill(Color.web("#2C3E50"));
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Button printReportButton = new Button("🖨️ Cetak & Export");
    printReportButton.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
    printReportButton.setOnAction(e -> openMaintenanceReportGenerator());
    
    headerBox.getChildren().addAll(titleLabel, spacer, printReportButton);
    
    // Summary statistics
    long totalMaintenance = maintenanceData.size();
    long pendingMaintenance = maintenanceData.stream().filter(m -> "Menunggu Penanganan".equals(m.getStatus())).count();
    long inProgressMaintenance = maintenanceData.stream().filter(m -> "Dalam Proses".equals(m.getStatus())).count();
    long completedMaintenance = maintenanceData.stream().filter(m -> "Selesai".equals(m.getStatus())).count();
    
    Label statsLabel = new Label(String.format(
        "Total Maintenance: %d | Pending: %d | Dalam Proses: %d | Selesai: %d",
        totalMaintenance, pendingMaintenance, inProgressMaintenance, completedMaintenance
    ));
    statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    statsLabel.setTextFill(Color.web("#34495E"));
    
    // Table with maintenance data
    TableView<MaintenanceRecord> reportTable = new TableView<>();
    reportTable.setItems(maintenanceData);
    reportTable.setPrefHeight(400);
    
    TableColumn<MaintenanceRecord, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("maintenanceId"));
    idCol.setPrefWidth(80);
    
    TableColumn<MaintenanceRecord, String> itemCol = new TableColumn<>("Alat");
    itemCol.setCellValueFactory(new PropertyValueFactory<>("inventoryName"));
    itemCol.setPrefWidth(180);
    
    TableColumn<MaintenanceRecord, String> typeCol = new TableColumn<>("Jenis");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("issueType"));
    typeCol.setPrefWidth(120);
    
    TableColumn<MaintenanceRecord, String> priorityCol = new TableColumn<>("Prioritas");
    priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
    priorityCol.setPrefWidth(80);
    
    TableColumn<MaintenanceRecord, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(120);
    
    TableColumn<MaintenanceRecord, String> reporterCol = new TableColumn<>("Pelapor");
    reporterCol.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
    reporterCol.setPrefWidth(100);
    
    TableColumn<MaintenanceRecord, String> dateCol = new TableColumn<>("Tanggal");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("reportedDate"));
    dateCol.setPrefWidth(100);
    
    reportTable.getColumns().addAll(idCol, itemCol, typeCol, priorityCol, statusCol, reporterCol, dateCol);
    
    displayArea.getChildren().addAll(headerBox, statsLabel, new Separator(), reportTable);
}
    
   private void showSummaryReport(VBox displayArea) {
    displayArea.getChildren().clear();
    
    Label titleLabel = new Label("📊 Laporan Ringkasan");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
    titleLabel.setTextFill(Color.web("#2C3E50"));
    
    // CREATE ENHANCED REPORT BUTTON
    HBox headerBox = new HBox(20);
    headerBox.setAlignment(Pos.CENTER_LEFT);
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Button enhancedReportButton = new Button("📄 Generator Laporan Lengkap");
    enhancedReportButton.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
    enhancedReportButton.setOnAction(e -> openEnhancedReportGenerator());
    
    headerBox.getChildren().addAll(titleLabel, spacer, enhancedReportButton);
    
    // Create summary cards
    HBox summaryCards = new HBox(20);
    summaryCards.setAlignment(Pos.CENTER);
    
    // Inventory summary
    long totalItems = inventoryData.size();
    long availableItems = inventoryData.stream().filter(item -> "Tersedia".equals(item.getStatus())).count();
    long borrowedItems = inventoryData.stream().filter(item -> "Dipinjam".equals(item.getStatus())).count();
    long maintenanceItems = inventoryData.stream().filter(item -> "Maintenance".equals(item.getStatus())).count();
    
    VBox inventoryCard = createSummaryCard("Inventaris", 
        String.format("Total: %d\nTersedia: %d\nDipinjam: %d\nMaintenance: %d", 
            totalItems, availableItems, borrowedItems, maintenanceItems), "#3498DB");
    
    // Borrowing summary
    long activeBorrows = borrowData.stream().filter(b -> "Dipinjam".equals(b.getStatus())).count();
    long overdueBorrows = borrowData.stream().filter(b -> "Terlambat".equals(b.getStatus())).count();
    
    VBox borrowCard = createSummaryCard("Peminjaman", 
        String.format("Aktif: %d\nTerlambat: %d\nTotal: %d", 
            activeBorrows, overdueBorrows, borrowData.size()), "#27AE60");
    
    // Maintenance summary
    long pendingMaintenance = maintenanceData.stream().filter(m -> "Menunggu Penanganan".equals(m.getStatus())).count();
    long inProgressMaintenance = maintenanceData.stream().filter(m -> "Dalam Proses".equals(m.getStatus())).count();
    
    VBox maintenanceCard = createSummaryCard("Maintenance", 
        String.format("Pending: %d\nProses: %d\nTotal: %d", 
            pendingMaintenance, inProgressMaintenance, maintenanceData.size()), "#E74C3C");
    
    summaryCards.getChildren().addAll(inventoryCard, borrowCard, maintenanceCard);
    
    // Recent activities
    Label recentLabel = new Label("Aktivitas Terbaru");
    recentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    recentLabel.setTextFill(Color.web("#2C3E50"));
    
    VBox recentActivities = new VBox(5);
    recentActivities.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 15; -fx-background-radius: 5;");
    
    // Show recent borrows
    borrowData.stream()
        .filter(b -> "Dipinjam".equals(b.getStatus()) || "Terlambat".equals(b.getStatus()))
        .limit(5)
        .forEach(b -> {
            Label activityLabel = new Label("📋 " + b.getInventoryName() + " dipinjam oleh " + b.getBorrowerName());
            activityLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            recentActivities.getChildren().add(activityLabel);
        });
    
    // Show recent maintenance
    maintenanceData.stream()
        .filter(m -> !"Selesai".equals(m.getStatus()))
        .limit(5)
        .forEach(m -> {
            Label activityLabel = new Label("🔧 " + m.getInventoryName() + " - " + m.getIssueType());
            activityLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            recentActivities.getChildren().add(activityLabel);
        });
    
    if (recentActivities.getChildren().isEmpty()) {
        Label noActivityLabel = new Label("Tidak ada aktivitas terbaru");
        noActivityLabel.setFont(Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 12));
        noActivityLabel.setTextFill(Color.web("#7F8C8D"));
        recentActivities.getChildren().add(noActivityLabel);
    }
    
    displayArea.getChildren().addAll(headerBox, new Separator(), summaryCards, new Separator(), recentLabel, recentActivities);
}

private VBox createSummaryCard(String title, String content, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 10;");
        card.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web(color));
        
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        contentLabel.setTextFill(Color.web("#2C3E50"));
        contentLabel.setWrapText(true);
        contentLabel.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
    }
    
    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Welcome section
        Label welcomeLabel = new Label("Dashboard");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.web("#2C3E50"));
        
        Label dateLabel = new Label("Tanggal: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")));
        dateLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        dateLabel.setTextFill(Color.web("#7F8C8D"));
        
        // Statistics cards
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        
        long totalItems = inventoryData.size();
        long borrowedItems = inventoryData.stream().filter(item -> "Dipinjam".equals(item.getStatus())).count();
        long maintenanceItems = inventoryData.stream().filter(item -> "Maintenance".equals(item.getStatus())).count();
        long availableItems = inventoryData.stream().filter(item -> "Tersedia".equals(item.getStatus())).count();
        
        VBox totalCard = createStatCard("Total Inventaris", String.valueOf(totalItems), "#3498DB");
        VBox borrowedCard = createStatCard("Sedang Dipinjam", String.valueOf(borrowedItems), "#E67E22");
        VBox maintenanceCard = createStatCard("Maintenance", String.valueOf(maintenanceItems), "#E74C3C");
        VBox availableCard = createStatCard("Tersedia", String.valueOf(availableItems), "#27AE60");
        
        statsContainer.getChildren().addAll(totalCard, borrowedCard, maintenanceCard, availableCard);
        
        // Quick actions
        Label quickActionsLabel = new Label("Aksi Cepat");
        quickActionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        quickActionsLabel.setTextFill(Color.web("#2C3E50"));
        
        HBox actionsContainer = new HBox(15);
        actionsContainer.setAlignment(Pos.CENTER_LEFT);
        
        Button checkButton = createActionButton("Cek Ketersediaan", "#3498DB");
        checkButton.setOnAction(e -> {
            // Switch to availability content
            VBox sidebar = (VBox) ((BorderPane) primaryStage.getScene().getRoot()).getLeft();
            Button checkBtn = (Button) sidebar.getChildren().get(3); // Check availability button
            switchToContent(createAvailabilityContent(), checkBtn);
        });
        
        Button borrowButton = createActionButton("Peminjaman Baru", "#27AE60");
        borrowButton.setOnAction(e -> showBorrowForm());
        
        Button returnButton = createActionButton("Pengembalian", "#E67E22");
        returnButton.setOnAction(e -> showReturnForm());
        
        Button reportButton = createActionButton("Buat Laporan", "#9B59B6");
        reportButton.setOnAction(e -> {
            // Switch to report content
            VBox sidebar = (VBox) ((BorderPane) primaryStage.getScene().getRoot()).getLeft();
            Button reportBtn = (Button) sidebar.getChildren().get(7); // Report button
            switchToContent(createReportContent(), reportBtn);
        });
        
        actionsContainer.getChildren().addAll(checkButton, borrowButton, returnButton, reportButton);
        
        mainContent.getChildren().addAll(welcomeLabel, dateLabel, new Separator(), statsContainer, new Separator(), quickActionsLabel, actionsContainer);
        
        return mainContent;
    }
    
    // Modified inventory content - removed status column
    private VBox createInventoryContent() {
        VBox inventoryContent = new VBox(20);
        inventoryContent.setPadding(new Insets(30));
        inventoryContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Data Inventaris");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("+ Tambah Item");
        addButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addButton.setOnAction(e -> showAddItemDialog());
        
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> refreshInventoryData());
        
        headerBox.getChildren().addAll(titleLabel, spacer, addButton, refreshButton);
        
        // Search and Filter
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Cari:");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        TextField searchField = new TextField();
        searchField.setPromptText("Cari berdasarkan nama, kategori, atau merk...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #BDC3C7;");
        
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Semua Kategori", "Hardware", "Software", "Aksesoris", "Networking", "Furniture");
        categoryFilter.setValue("Semua Kategori");
        categoryFilter.setStyle("-fx-background-radius: 5;");
        
        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateInventoryFilter(newValue, categoryFilter.getValue());
        });
        
        categoryFilter.setOnAction(e -> {
            updateInventoryFilter(searchField.getText(), categoryFilter.getValue());
        });
        
        searchBox.getChildren().addAll(searchLabel, searchField, categoryFilter);
        
        // Table (without status column)
        inventoryTable = createInventoryTableWithoutStatus();
        
        // Table container with styling
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tableContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label tableLabel = new Label("Daftar Inventaris");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.web("#2C3E50"));
        
        tableContainer.getChildren().addAll(tableLabel, inventoryTable);
        
        inventoryContent.getChildren().addAll(headerBox, searchBox, tableContainer);
        
        return inventoryContent;
    }
    
    private void updateInventoryFilter(String searchText, String categoryFilter) {
        filteredData.setPredicate(item -> {
            // Search text filter
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!item.getNama().toLowerCase().contains(lowerCaseFilter) &&
                    !item.getKategori().toLowerCase().contains(lowerCaseFilter) &&
                    !item.getMerk().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }
            
            // Category filter
            if (categoryFilter != null && !categoryFilter.equals("Semua Kategori")) {
                if (!item.getKategori().equals(categoryFilter)) {
                    return false;
                }
            }
            
            return true;
        });
    }
    
    // Create inventory table without status column
    private TableView<InventoryItem> createInventoryTableWithoutStatus() {
        TableView<InventoryItem> table = new TableView<>();
        table.setItems(filteredData);
        table.setPrefHeight(400);
        
        // Columns (removed status column)
        TableColumn<InventoryItem, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<InventoryItem, String> namaCol = new TableColumn<>("Nama");
        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        namaCol.setPrefWidth(250);
        
        TableColumn<InventoryItem, String> kategoriCol = new TableColumn<>("Kategori");
        kategoriCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        kategoriCol.setPrefWidth(120);
        
        TableColumn<InventoryItem, String> merkCol = new TableColumn<>("Merk");
        merkCol.setCellValueFactory(new PropertyValueFactory<>("merk"));
        merkCol.setPrefWidth(120);
        
        TableColumn<InventoryItem, String> kondisiCol = new TableColumn<>("Kondisi");
        kondisiCol.setCellValueFactory(new PropertyValueFactory<>("kondisi"));
        kondisiCol.setPrefWidth(100);
        
        TableColumn<InventoryItem, String> lokasiCol = new TableColumn<>("Lokasi");
        lokasiCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        lokasiCol.setPrefWidth(120);
        
        TableColumn<InventoryItem, String> tanggalCol = new TableColumn<>("Tanggal Masuk");
        tanggalCol.setCellValueFactory(new PropertyValueFactory<>("tanggalMasuk"));
        tanggalCol.setPrefWidth(120);
        
        // Action column
        TableColumn<InventoryItem, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> {
            return new TableCell<InventoryItem, Void>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Hapus");
                private final HBox actionBox = new HBox(5);
                
                {
                    editBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                    deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                    
                    editBtn.setOnAction(e -> {
                        InventoryItem item = getTableView().getItems().get(getIndex());
                        showEditItemDialog(item);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        InventoryItem item = getTableView().getItems().get(getIndex());
                        showDeleteConfirmation(item);
                    });
                    
                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionBox);
                    }
                }
            };
        });
        
        table.getColumns().addAll(idCol, namaCol, kategoriCol, merkCol, kondisiCol, lokasiCol, tanggalCol, actionCol);
        
        return table;
    }
    
    private VBox createBorrowingContent() {
        VBox borrowContent = new VBox(20);
        borrowContent.setPadding(new Insets(30));
        borrowContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Peminjaman Alat");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button newBorrowButton = new Button("+ Peminjaman Baru");
        newBorrowButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        newBorrowButton.setOnAction(e -> showBorrowForm());
        
        headerBox.getChildren().addAll(titleLabel, spacer, newBorrowButton);
        
        // Borrowing table
        TableView<BorrowRecord> borrowTable = createBorrowingTable();
        
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tableContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label tableLabel = new Label("Daftar Peminjaman");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.web("#2C3E50"));
        
        tableContainer.getChildren().addAll(tableLabel, borrowTable);
        borrowContent.getChildren().addAll(headerBox, tableContainer);
        
        return borrowContent;
    }
    
    private VBox createReturnContent() {
        VBox returnContent = new VBox(20);
        returnContent.setPadding(new Insets(30));
        returnContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Pengembalian Alat");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button newReturnButton = new Button("+ Pengembalian Baru");
        newReturnButton.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        newReturnButton.setOnAction(e -> showReturnForm());
        
        headerBox.getChildren().addAll(titleLabel, spacer, newReturnButton);
        
        // Show borrowed items only
        FilteredList<BorrowRecord> activeBorrows = new FilteredList<>(borrowData, 
            record -> "Dipinjam".equals(record.getStatus()) || "Terlambat".equals(record.getStatus()));
        
        TableView<BorrowRecord> returnTable = new TableView<>();
        returnTable.setItems(activeBorrows);
        returnTable.setPrefHeight(400);
        
        // Columns for return table
        TableColumn<BorrowRecord, String> borrowIdCol = new TableColumn<>("ID");
        borrowIdCol.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        borrowIdCol.setPrefWidth(80);
        
        TableColumn<BorrowRecord, String> itemCol = new TableColumn<>("Alat");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("inventoryName"));
        itemCol.setPrefWidth(200);
        
        TableColumn<BorrowRecord, String> borrowerCol = new TableColumn<>("Peminjam");
        borrowerCol.setCellValueFactory(new PropertyValueFactory<>("borrowerName"));
        borrowerCol.setPrefWidth(150);
        
        TableColumn<BorrowRecord, String> borrowDateCol = new TableColumn<>("Tgl Pinjam");
        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowDateCol.setPrefWidth(100);
        
        TableColumn<BorrowRecord, String> returnDateCol = new TableColumn<>("Tgl Kembali");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        returnDateCol.setPrefWidth(100);
        
        TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        returnTable.getColumns().addAll(borrowIdCol, itemCol, borrowerCol, borrowDateCol, returnDateCol, statusCol);
        
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tableContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label tableLabel = new Label("Item yang Sedang Dipinjam");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.web("#2C3E50"));
        
        tableContainer.getChildren().addAll(tableLabel, returnTable);
        returnContent.getChildren().addAll(headerBox, tableContainer);
        
        return returnContent;
    }
    
    private VBox createMaintenanceContent() {
        VBox maintenanceContent = new VBox(20);
        maintenanceContent.setPadding(new Insets(30));
        maintenanceContent.setStyle("-fx-background-color: #ECF0F1;");
        
        // Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🔧 Maintenance & Kerusakan");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button reportIssueButton = new Button("+ Lapor Kerusakan");
        reportIssueButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        reportIssueButton.setOnAction(e -> showMaintenanceForm());
        
        headerBox.getChildren().addAll(titleLabel, spacer, reportIssueButton);
        
        // Maintenance table
        TableView<MaintenanceRecord> maintenanceTable = createMaintenanceTable();
        
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tableContainer.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label tableLabel = new Label("Daftar Maintenance");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.web("#2C3E50"));
        
        tableContainer.getChildren().addAll(tableLabel, maintenanceTable);
        maintenanceContent.getChildren().addAll(headerBox, tableContainer);
        
        return maintenanceContent;
    }
    
    private TableView<MaintenanceRecord> createMaintenanceTable() {
        TableView<MaintenanceRecord> table = new TableView<>();
        table.setItems(maintenanceData);
        table.setPrefHeight(400);
        
        TableColumn<MaintenanceRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("maintenanceId"));
        idCol.setPrefWidth(80);
        
        TableColumn<MaintenanceRecord, String> itemCol = new TableColumn<>("Alat");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("inventoryName"));
        itemCol.setPrefWidth(150);
        
        TableColumn<MaintenanceRecord, String> typeCol = new TableColumn<>("Jenis");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("issueType"));
        typeCol.setPrefWidth(100);
        
        TableColumn<MaintenanceRecord, String> priorityCol = new TableColumn<>("Prioritas");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(80);
        
        TableColumn<MaintenanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        
        TableColumn<MaintenanceRecord, String> reporterCol = new TableColumn<>("Pelapor");
        reporterCol.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
        reporterCol.setPrefWidth(100);
        
        TableColumn<MaintenanceRecord, String> dateCol = new TableColumn<>("Tanggal");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("reportedDate"));
        dateCol.setPrefWidth(100);
        
        // Action column
        TableColumn<MaintenanceRecord, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(col -> {
            return new TableCell<MaintenanceRecord, Void>() {
                private final Button updateBtn = new Button("Update");
                
                {
                    updateBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                    updateBtn.setOnAction(e -> {
                        MaintenanceRecord record = getTableView().getItems().get(getIndex());
                        showUpdateMaintenanceForm(record);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(updateBtn);
                    }
                }
            };
        });
        
        table.getColumns().addAll(idCol, itemCol, typeCol, priorityCol, statusCol, reporterCol, dateCol, actionCol);
        
        return table;
    }
    
    private TableView<InventoryItem> createInventoryTable() {
        TableView<InventoryItem> table = new TableView<>();
        table.setItems(filteredData);
        table.setPrefHeight(400);
        
        // Columns
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
        
        TableColumn<InventoryItem, String> tanggalCol = new TableColumn<>("Tanggal Masuk");
        tanggalCol.setCellValueFactory(new PropertyValueFactory<>("tanggalMasuk"));
        tanggalCol.setPrefWidth(120);
        
        // Action column
        TableColumn<InventoryItem, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> {
            return new TableCell<InventoryItem, Void>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Hapus");
                private final HBox actionBox = new HBox(5);
                
                {
                    editBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                    deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3;");
                    
                    editBtn.setOnAction(e -> {
                        InventoryItem item = getTableView().getItems().get(getIndex());
                        showEditItemDialog(item);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        InventoryItem item = getTableView().getItems().get(getIndex());
                        showDeleteConfirmation(item);
                    });
                    
                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionBox);
                    }
                }
            };
        });
        
        table.getColumns().addAll(idCol, namaCol, kategoriCol, merkCol, kondisiCol, lokasiCol, statusCol, tanggalCol, actionCol);
        
        return table;
    }
    
    private TableView<BorrowRecord> createBorrowingTable() {
        TableView<BorrowRecord> table = new TableView<>();
        table.setItems(borrowData);
        table.setPrefHeight(400);
        
        TableColumn<BorrowRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        idCol.setPrefWidth(80);
        
        TableColumn<BorrowRecord, String> itemCol = new TableColumn<>("Alat");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("inventoryName"));
        itemCol.setPrefWidth(200);
        
        TableColumn<BorrowRecord, String> borrowerCol = new TableColumn<>("Peminjam");
        borrowerCol.setCellValueFactory(new PropertyValueFactory<>("borrowerName"));
        borrowerCol.setPrefWidth(150);
        
        TableColumn<BorrowRecord, String> typeCol = new TableColumn<>("Tipe");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("borrowerType"));
        typeCol.setPrefWidth(100);
        
        TableColumn<BorrowRecord, String> borrowDateCol = new TableColumn<>("Tgl Pinjam");
        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowDateCol.setPrefWidth(100);
        
        TableColumn<BorrowRecord, String> returnDateCol = new TableColumn<>("Tgl Kembali");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        returnDateCol.setPrefWidth(100);
        
        TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, itemCol, borrowerCol, typeCol, borrowDateCol, returnDateCol, statusCol);
        
        return table;
    }
    
    // Dialog methods (keeping existing form dialogs for data entry)
    private void showBorrowForm() {
        BorrowForm form = new BorrowForm(primaryStage, inventoryData);
        BorrowRecord newRecord = form.showAndWait();
        
        if (newRecord != null) {
            String borrowId = BorrowManager.generateNewBorrowId(borrowData);
            newRecord.setBorrowId(borrowId);
            borrowData.add(newRecord);
            
            // Update inventory status
            InventoryItem item = inventoryData.stream()
                .filter(i -> i.getId().equals(newRecord.getInventoryId()))
                .findFirst()
                .orElse(null);
            if (item != null) {
                item.setStatus("Dipinjam");
            }
            
            showAlert("Sukses", "Peminjaman berhasil dicatat dengan ID: " + borrowId, Alert.AlertType.INFORMATION);
            
            // Refresh current content if showing borrowing or dashboard
            if (currentMainContent != null) {
                refreshCurrentContent();
            }
        }
    }
    
    private void showReturnForm() {
        ReturnForm form = new ReturnForm(primaryStage, borrowData);
        String returnNotes = form.showAndWait();
        
        if (returnNotes != null && form.getSelectedRecord() != null) {
            BorrowRecord record = form.getSelectedRecord();
            
            // Update borrow record
            if (BorrowManager.returnItem(borrowData, record.getBorrowId(), returnNotes)) {
                // Update inventory status
                InventoryItem item = inventoryData.stream()
                    .filter(i -> i.getId().equals(record.getInventoryId()))
                    .findFirst()
                    .orElse(null);
                if (item != null) {
                    item.setStatus("Tersedia");
                }
                
                // Save data
                BorrowManager.saveBorrowData(borrowData);
                CSVManager.saveInventoryData(inventoryData);
                
                showAlert("Sukses", "Alat berhasil dikembalikan!", Alert.AlertType.INFORMATION);
                refreshCurrentContent();
            }
        }
    }
    
    private void showMaintenanceForm() {
        MaintenanceForm form = new MaintenanceForm(primaryStage, inventoryData, null);
        MaintenanceRecord newRecord = form.showAndWait();
        
        if (newRecord != null) {
            String maintenanceId = MaintenanceManager.generateNewMaintenanceId(maintenanceData);
            newRecord.setMaintenanceId(maintenanceId);
            maintenanceData.add(newRecord);
            
            // Update inventory status
            InventoryItem item = inventoryData.stream()
                .filter(i -> i.getId().equals(newRecord.getInventoryId()))
                .findFirst()
                .orElse(null);
            if (item != null) {
                item.setStatus("Maintenance");
            }
            
            // Save data
            MaintenanceManager.saveMaintenanceData(maintenanceData);
            CSVManager.saveInventoryData(inventoryData);
            
            showAlert("Sukses", "Laporan maintenance berhasil dicatat dengan ID: " + maintenanceId, Alert.AlertType.INFORMATION);
            refreshCurrentContent();
        }
    }
    
   private void showUpdateMaintenanceForm(MaintenanceRecord record) {
    MaintenanceForm form = new MaintenanceForm(primaryStage, inventoryData, record);
    MaintenanceRecord updatedRecord = form.showAndWait();
    
    if (updatedRecord != null) {
        // Update record yang ada dengan data baru
        if (MaintenanceManager.updateMaintenanceRecord(maintenanceData, updatedRecord)) {
            // Update inventory status based on maintenance status
            InventoryItem item = inventoryData.stream()
                .filter(i -> i.getId().equals(record.getInventoryId()))
                .findFirst()
                .orElse(null);
            
            if (item != null) {
                if ("Selesai".equals(updatedRecord.getStatus())) {
                    // Check if item is borrowed, otherwise set as available
                    if (!BorrowManager.isItemBorrowed(borrowData, item.getId())) {
                        item.setStatus("Tersedia");
                    }
                } else {
                    item.setStatus("Maintenance");
                }
            }
            
            // Save data
            MaintenanceManager.saveMaintenanceData(maintenanceData);
            CSVManager.saveInventoryData(inventoryData);
            
            showAlert("Sukses", "Status maintenance berhasil diperbarui!", Alert.AlertType.INFORMATION);
            refreshCurrentContent();
        } else {
            showAlert("Error", "Gagal memperbarui status maintenance!", Alert.AlertType.ERROR);
        }
    }
}
    
    private void showAddItemDialog() {
        InventoryForm form = new InventoryForm(primaryStage, null);
        InventoryItem newItem = form.showAndWait();
        
        if (newItem != null) {
            // Generate new ID based on category
            String newId = CSVManager.generateNewId(inventoryData, newItem.getKategori());
            newItem.setId(newId);
            
            inventoryData.add(newItem);
            CSVManager.saveInventoryData(inventoryData);
            showAlert("Sukses", "Data inventaris berhasil ditambahkan dengan ID: " + newId, Alert.AlertType.INFORMATION);
            
            refreshCurrentContent();
        }
    }
    
    private void showEditItemDialog(InventoryItem item) {
        InventoryForm form = new InventoryForm(primaryStage, item);
        InventoryItem updatedItem = form.showAndWait();
        
        if (updatedItem != null) {
            // Update item properties
            item.setNama(updatedItem.getNama());
            item.setKategori(updatedItem.getKategori());
            item.setMerk(updatedItem.getMerk());
            item.setKondisi(updatedItem.getKondisi());
            item.setLokasi(updatedItem.getLokasi());
            item.setStatus(updatedItem.getStatus());
            item.setTanggalMasuk(updatedItem.getTanggalMasuk());
            
            CSVManager.saveInventoryData(inventoryData);
            inventoryTable.refresh();
            showAlert("Sukses", "Data inventaris berhasil diperbarui!", Alert.AlertType.INFORMATION);
        }
    }
    
    private void showDeleteConfirmation(InventoryItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Data Inventaris");
        alert.setContentText("Apakah Anda yakin ingin menghapus item: " + item.getNama() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            inventoryData.remove(item);
            CSVManager.saveInventoryData(inventoryData);
            showAlert("Sukses", "Data inventaris berhasil dihapus!", Alert.AlertType.INFORMATION);
        }
    }
    
    private void refreshInventoryData() {
        loadAllData();
        if (inventoryTable != null) {
            inventoryTable.refresh();
        }
        showAlert("Sukses", "Data inventaris berhasil direfresh!", Alert.AlertType.INFORMATION);
    }
    
    private void refreshCurrentContent() {
        // Refresh the current content based on what's being displayed
        if (currentMainContent != null) {
            // Get current active button to determine content type
            VBox sidebar = (VBox) ((BorderPane) primaryStage.getScene().getRoot()).getLeft();
            for (int i = 1; i < sidebar.getChildren().size(); i++) {
                Button btn = (Button) sidebar.getChildren().get(i);
                if (btn.getStyle().contains("#3498DB")) { // Active button
                    String text = btn.getText();
                    if (text.contains("Dashboard")) {
                        switchToContent(createMainContent(), btn);
                    } else if (text.contains("Inventaris")) {
                        switchToContent(createInventoryContent(), btn);
                    } else if (text.contains("Ketersediaan")) {
                        switchToContent(createAvailabilityContent(), btn);
                    } else if (text.contains("Peminjaman")) {
                        switchToContent(createBorrowingContent(), btn);
                    } else if (text.contains("Pengembalian")) {
                        switchToContent(createReturnContent(), btn);
                    } else if (text.contains("Maintenance")) {
                        switchToContent(createMaintenanceContent(), btn);
                    } else if (text.contains("Laporan")) {
                        switchToContent(createReportContent(), btn);
                    }
                    break;
                }
            }
        }
    }

    
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(150);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#7F8C8D"));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }
    
    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(50);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));
        
        return button;
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openEnhancedReportGenerator() {
        try {
            ReportGenerator reportGenerator = new ReportGenerator(primaryStage, inventoryData, borrowData, maintenanceData);
            reportGenerator.showAndWait();
        } catch (Exception e) {
            showAlert("Error", "Gagal membuka generator laporan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openInventoryReportGenerator() {
    try {
        ReportGenerator reportGenerator = new ReportGenerator(primaryStage, inventoryData, borrowData, maintenanceData);
        reportGenerator.showAndWait();
    } catch (Exception e) {
        showAlert("Error", "Gagal membuka generator laporan: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}

private void openBorrowingReportGenerator() {
    try {
        ReportGenerator reportGenerator = new ReportGenerator(primaryStage, inventoryData, borrowData, maintenanceData);
        reportGenerator.showAndWait();
    } catch (Exception e) {
        showAlert("Error", "Gagal membuka generator laporan: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}

private void openMaintenanceReportGenerator() {
    try {
        ReportGenerator reportGenerator = new ReportGenerator(primaryStage, inventoryData, borrowData, maintenanceData);
        reportGenerator.showAndWait();
    } catch (Exception e) {
        showAlert("Error", "Gagal membuka generator laporan: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}
    
    public static void main(String[] args) {
        launch(args);
    }
}