import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class untuk operasi pencetakan dan export laporan
 */
public class PrintUtilities {
    
    /**
     * Print text content menggunakan JavaFX Print API
     */
    public static boolean printTextContent(String content, String title, Stage parentStage) {
        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob == null) {
                showAlert("Error", "Tidak dapat mengakses printer!", Alert.AlertType.ERROR);
                return false;
            }
            
            // Show print dialog
            boolean proceed = printerJob.showPrintDialog(parentStage);
            if (!proceed) {
                return false; // User cancelled
            }
            
            // Create TextFlow for printing
            TextFlow textFlow = createPrintableTextFlow(content);
            
            // Get printer and setup page layout
            Printer printer = printerJob.getPrinter();
            PageLayout pageLayout = printer.createPageLayout(
                Paper.A4, 
                PageOrientation.PORTRAIT, 
                Printer.MarginType.DEFAULT
            );
            
            // Set up the text flow for printing
            textFlow.setPrefWidth(pageLayout.getPrintableWidth());
            textFlow.autosize();
            
            // Print the content
            boolean success = printerJob.printPage(pageLayout, textFlow);
            
            if (success) {
                printerJob.endJob();
                showAlert("Sukses", "Laporan berhasil dicetak!", Alert.AlertType.INFORMATION);
                return true;
            } else {
                showAlert("Error", "Gagal mencetak laporan!", Alert.AlertType.ERROR);
                return false;
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error saat mencetak: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    /**
     * Create TextFlow yang dapat dicetak
     */
    private static TextFlow createPrintableTextFlow(String content) {
        TextFlow textFlow = new TextFlow();
        
        // Split content into lines
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            Text text = new Text(line + "\n");
            
            // Set font berdasarkan konten
            if (line.contains("=====") || line.contains("LAB INVENTORY SYSTEM")) {
                text.setFont(Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 12));
            } else if (line.trim().toUpperCase().equals(line.trim()) && 
                      !line.trim().isEmpty() && 
                      !line.contains(":") && 
                      line.length() < 50) {
                // Headers
                text.setFont(Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 11));
            } else {
                text.setFont(Font.font("Courier New", 10));
            }
            
            textFlow.getChildren().add(text);
        }
        
        return textFlow;
    }
    
    /**
     * Export content ke file TXT dengan format yang baik
     */
    public static boolean exportToTXT(String content, String filename, File directory) {
        try {
            File file = new File(directory, filename + ".txt");
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
            
            return true;
            
        } catch (IOException e) {
            showAlert("Error", "Gagal menyimpan file TXT: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    /**
     * Generate HTML content untuk export PDF
     */
    public static String generateHTMLForPDF(String textContent, String title) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"id\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>").append(title).append("</title>\n");
        
        // Enhanced CSS for better PDF output
        html.append("<style>\n");
        html.append("@page {\n");
        html.append("  size: A4;\n");
        html.append("  margin: 2cm 1.5cm;\n");
        html.append("}\n");
        html.append("body {\n");
        html.append("  font-family: 'Courier New', 'DejaVu Sans Mono', monospace;\n");
        html.append("  font-size: 11px;\n");
        html.append("  line-height: 1.4;\n");
        html.append("  color: #000;\n");
        html.append("  margin: 0;\n");
        html.append("  padding: 0;\n");
        html.append("}\n");
        html.append(".header {\n");
        html.append("  text-align: center;\n");
        html.append("  margin-bottom: 30px;\n");
        html.append("  border-bottom: 2px solid #000;\n");
        html.append("  padding-bottom: 15px;\n");
        html.append("}\n");
        html.append(".header h1 {\n");
        html.append("  font-size: 18px;\n");
        html.append("  font-weight: bold;\n");
        html.append("  margin: 0 0 10px 0;\n");
        html.append("}\n");
        html.append(".header h2 {\n");
        html.append("  font-size: 14px;\n");
        html.append("  font-weight: bold;\n");
        html.append("  margin: 0;\n");
        html.append("}\n");
        html.append(".content {\n");
        html.append("  white-space: pre-line;\n");
        html.append("  font-family: 'Courier New', monospace;\n");
        html.append("}\n");
        html.append(".footer {\n");
        html.append("  margin-top: 40px;\n");
        html.append("  text-align: center;\n");
        html.append("  font-size: 10px;\n");
        html.append("  color: #666;\n");
        html.append("  border-top: 1px solid #ccc;\n");
        html.append("  padding-top: 15px;\n");
        html.append("}\n");
        html.append("@media print {\n");
        html.append("  body { margin: 0; }\n");
        html.append("  .no-print { display: none; }\n");
        html.append("}\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        // Header
        html.append("<div class=\"header\">\n");
        html.append("<h1>LAB INVENTORY SYSTEM</h1>\n");
        html.append("<h2>").append(title).append("</h2>\n");
        html.append("</div>\n");
        
        // Content
        html.append("<div class=\"content\">\n");
        html.append(escapeHtml(textContent));
        html.append("</div>\n");
        
        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("<p>Generated by Lab Inventory System - ");
        html.append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        html.append("</p>\n");
        html.append("</div>\n");
        
        // Print instructions (hidden when printing)
        html.append("<div class=\"no-print\" style=\"position: fixed; top: 10px; right: 10px; background: #f0f0f0; padding: 10px; border: 1px solid #ccc; border-radius: 5px;\">\n");
        html.append("<strong>Untuk menyimpan sebagai PDF:</strong><br>\n");
        html.append("1. Tekan Ctrl+P<br>\n");
        html.append("2. Pilih 'Save as PDF'<br>\n");
        html.append("3. Klik Save\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    /**
     * Export content ke file HTML untuk konversi PDF
     */
    public static boolean exportToHTML(String content, String title, String filename, File directory) {
        try {
            File file = new File(directory, filename + ".html");
            String htmlContent = generateHTMLForPDF(content, title);
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(htmlContent);
            }
            
            return true;
            
        } catch (IOException e) {
            showAlert("Error", "Gagal menyimpan file HTML: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    /**
     * Escape HTML characters untuk keamanan
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    /**
     * Generate filename berdasarkan jenis laporan dan tanggal
     */
    public static String generateFilename(String reportType, String extension) {
        String cleanReportType = reportType.replace(" ", "_")
                                          .replace("&", "dan")
                                          .replaceAll("[^a-zA-Z0-9_]", "");
        
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        return cleanReportType + "_" + dateStr + "." + extension;
    }
    
    /**
     * Validasi apakah printer tersedia
     */
    public static boolean isPrinterAvailable() {
        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            return printerJob != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get informasi printer yang tersedia
     */
    public static String getPrinterInfo() {
        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null) {
                Printer printer = printerJob.getPrinter();
                return "Printer: " + printer.getName();
            }
            return "Tidak ada printer yang tersedia";
        } catch (Exception e) {
            return "Error mengakses printer: " + e.getMessage();
        }
    }
    
    /**
     * Print preview - membuat estimasi jumlah halaman
     */
    public static int estimatePageCount(String content, PageLayout pageLayout) {
        try {
            // Estimasi berdasarkan jumlah baris dan tinggi halaman
            String[] lines = content.split("\n");
            int linesPerPage = (int) (pageLayout.getPrintableHeight() / 12); // Asumsi 12pt line height
            
            return (int) Math.ceil((double) lines.length / linesPerPage);
        } catch (Exception e) {
            return 1; // Default 1 halaman jika error
        }
    }
    
    /**
     * Format content untuk pencetakan yang lebih baik
     */
    public static String formatContentForPrint(String content) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            // Pastikan line tidak terlalu panjang untuk printer
            if (line.length() > 80) {
                // Word wrap untuk line yang panjang
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                
                for (String word : words) {
                    if (currentLine.length() + word.length() + 1 <= 80) {
                        if (currentLine.length() > 0) {
                            currentLine.append(" ");
                        }
                        currentLine.append(word);
                    } else {
                        if (currentLine.length() > 0) {
                            formatted.append(currentLine.toString()).append("\n");
                            currentLine = new StringBuilder(word);
                        } else {
                            // Word terlalu panjang, potong paksa
                            formatted.append(word.substring(0, 80)).append("\n");
                            if (word.length() > 80) {
                                currentLine = new StringBuilder(word.substring(80));
                            }
                        }
                    }
                }
                
                if (currentLine.length() > 0) {
                    formatted.append(currentLine.toString()).append("\n");
                }
            } else {
                formatted.append(line).append("\n");
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Show alert dialog
     */
    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Create page header untuk multi-page documents
     */
    public static String createPageHeader(String title, int pageNumber, int totalPages) {
        StringBuilder header = new StringBuilder();
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        header.append(String.format("%-40s %20s %s %d/%d\n", 
            title, timestamp, "Hal.", pageNumber, totalPages));
        header.append("=".repeat(80)).append("\n");
        
        return header.toString();
    }
    
    /**
     * Split content into pages untuk multi-page printing
     */
    public static String[] splitContentIntoPages(String content, int linesPerPage) {
        String[] lines = content.split("\n");
        int totalPages = (int) Math.ceil((double) lines.length / linesPerPage);
        String[] pages = new String[totalPages];
        
        for (int page = 0; page < totalPages; page++) {
            StringBuilder pageContent = new StringBuilder();
            int startLine = page * linesPerPage;
            int endLine = Math.min(startLine + linesPerPage, lines.length);
            
            for (int i = startLine; i < endLine; i++) {
                pageContent.append(lines[i]).append("\n");
            }
            
            pages[page] = pageContent.toString();
        }
        
        return pages;
    }
}