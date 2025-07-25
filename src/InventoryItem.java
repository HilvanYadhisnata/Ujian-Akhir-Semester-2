import javafx.beans.property.SimpleStringProperty;

public class InventoryItem {
    private SimpleStringProperty id;
    private SimpleStringProperty nama;
    private SimpleStringProperty kategori;
    private SimpleStringProperty merk;
    private SimpleStringProperty kondisi;
    private SimpleStringProperty lokasi;
    private SimpleStringProperty status;
    private SimpleStringProperty tanggalMasuk;
    
    public InventoryItem(String id, String nama, String kategori, String merk, 
                       String kondisi, String lokasi, String status, String tanggalMasuk) {
        this.id = new SimpleStringProperty(id);
        this.nama = new SimpleStringProperty(nama);
        this.kategori = new SimpleStringProperty(kategori);
        this.merk = new SimpleStringProperty(merk);
        this.kondisi = new SimpleStringProperty(kondisi);
        this.lokasi = new SimpleStringProperty(lokasi);
        this.status = new SimpleStringProperty(status);
        this.tanggalMasuk = new SimpleStringProperty(tanggalMasuk);
    }
    
    // Getters
    public String getId() { return id.get(); }
    public String getNama() { return nama.get(); }
    public String getKategori() { return kategori.get(); }
    public String getMerk() { return merk.get(); }
    public String getKondisi() { return kondisi.get(); }
    public String getLokasi() { return lokasi.get(); }
    public String getStatus() { return status.get(); }
    public String getTanggalMasuk() { return tanggalMasuk.get(); }
    
    // Property getters for TableView
    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty namaProperty() { return nama; }
    public SimpleStringProperty kategoriProperty() { return kategori; }
    public SimpleStringProperty merkProperty() { return merk; }
    public SimpleStringProperty kondisiProperty() { return kondisi; }
    public SimpleStringProperty lokasiProperty() { return lokasi; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty tanggalMasukProperty() { return tanggalMasuk; }
    
    // Setters
    public void setId(String id) { this.id.set(id); }
    public void setNama(String nama) { this.nama.set(nama); }
    public void setKategori(String kategori) { this.kategori.set(kategori); }
    public void setMerk(String merk) { this.merk.set(merk); }
    public void setKondisi(String kondisi) { this.kondisi.set(kondisi); }
    public void setLokasi(String lokasi) { this.lokasi.set(lokasi); }
    public void setStatus(String status) { this.status.set(status); }
    public void setTanggalMasuk(String tanggalMasuk) { this.tanggalMasuk.set(tanggalMasuk); }
    
    // Convert to CSV format
    public String toCSV() {
        return String.join(",", getId(), getNama(), getKategori(), getMerk(), 
                          getKondisi(), getLokasi(), getStatus(), getTanggalMasuk());
    }
    
    // Create from CSV line
    public static InventoryItem fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 8) {
            return new InventoryItem(parts[0], parts[1], parts[2], parts[3], 
                                   parts[4], parts[5], parts[6], parts[7]);
        }
        return null;
    }
}