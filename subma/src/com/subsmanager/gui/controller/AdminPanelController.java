package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.Admin;
import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;
import com.subsmanager.db.ServiceDAO;
import com.subsmanager.db.ServiceDAO.UserRecord;
import com.subsmanager.db.ServiceDAO.PurchaseRecord;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * AdminPanelController - Controller untuk adminpanel.fxml
 *
 * 3 tab:
 * - Tab User       : lihat & hapus user
 * - Tab Pemasukan  : lihat semua transaksi PURCHASE + total
 * - Tab Katalog    : CRUD services dan service_tiers
 *
 * Dependency:
 * - AdminPanelController ..> ServiceDAO (semua query admin)
 * - AdminPanelController ..> SessionManager (ambil admin + logout)
 */
public class AdminPanelController implements Initializable {

    // ── Header ────────────────────────────────────────────
    @FXML private Label adminEmailLabel;

    // ═══════════════════════════════════════════════════════
    // TAB 1 — USER
    // ═══════════════════════════════════════════════════════
    @FXML private Label totalUserLabel;

    @FXML private TableView<UserRecord>           userTable;
    @FXML private TableColumn<UserRecord, Long>   colUserId;
    @FXML private TableColumn<UserRecord, String> colUserEmail;
    @FXML private TableColumn<UserRecord, String> colUserTgl;
    @FXML private TableColumn<UserRecord, Integer> colUserKoin;
    @FXML private TableColumn<UserRecord, String> colUserRole;
    @FXML private TableColumn<UserRecord, Void>   colUserAksi;

    // ═══════════════════════════════════════════════════════
    // TAB 2 — PEMASUKAN
    // ═══════════════════════════════════════════════════════
    @FXML private Label totalPemasukanLabel;
    @FXML private Label totalTransaksiLabel;

    @FXML private TableView<PurchaseRecord>           pemasukanTable;
    @FXML private TableColumn<PurchaseRecord, String> colPEmail;
    @FXML private TableColumn<PurchaseRecord, String> colPTanggal;
    @FXML private TableColumn<PurchaseRecord, String> colPKode;
    @FXML private TableColumn<PurchaseRecord, Integer> colPKoin;
    @FXML private TableColumn<PurchaseRecord, String> colPHarga;
    @FXML private TableColumn<PurchaseRecord, String> colPMetode;
    @FXML private TableColumn<PurchaseRecord, String> colPStatus;

    // ═══════════════════════════════════════════════════════
    // TAB 3 — KATALOG
    // ═══════════════════════════════════════════════════════

    // -- Services --
    @FXML private TableView<Service>           serviceTable;
    @FXML private TableColumn<Service, String> colSvcNama;
    @FXML private TableColumn<Service, String> colSvcDomain;
    @FXML private TableColumn<Service, String> colSvcKategori;
    @FXML private TableColumn<Service, String> colSvcCurrency;
    @FXML private TableColumn<Service, Void>   colSvcAksi;

    // Form tambah service
    @FXML private TextField tfSvcNama;
    @FXML private TextField tfSvcDomain;
    @FXML private TextField tfSvcCancelUrl;
    @FXML private TextField tfSvcKategori;
    @FXML private Label labelSvcError;

    // -- Tiers --
    @FXML private Label tierHeaderLabel;
    @FXML private TableView<ServiceTier>           tierTable;
    @FXML private TableColumn<ServiceTier, String> colTierNama;
    @FXML private TableColumn<ServiceTier, String> colTierDesc;
    @FXML private TableColumn<ServiceTier, Void>   colTierAksi;

    // Form tambah tier
    @FXML private TextField tfTierNama;
    @FXML private TextField tfTierDesc;
    @FXML private Label     labelTierError;

    // ── State ─────────────────────────────────────────────
    private ObservableList<UserRecord>    userList;
    private ObservableList<PurchaseRecord> pemasukanList;
    private ObservableList<Service>       serviceList;
    private ObservableList<ServiceTier>   tierList;

    /** Service yang sedang dipilih untuk manage tier-nya */
    private Service selectedService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Admin admin = (Admin) SessionManager.getCurrentUser();
        adminEmailLabel.setText(admin.getEmail() + " (Admin)");

        setupUserTab();
        setupPemasukanTab();
        setupKatalogTab();
        loadAllData();
    }

    // ═══════════════════════════════════════════════════════
    // SETUP TABEL
    // ═══════════════════════════════════════════════════════

    private void setupUserTab() {
        colUserId.setCellValueFactory(d ->
            new javafx.beans.property.SimpleLongProperty(
                d.getValue().getId()).asObject());
        colUserEmail.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getEmail()));
        colUserTgl.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTanggalDaftar()));
        colUserKoin.setCellValueFactory(d ->
            new SimpleIntegerProperty(
                d.getValue().getSaldoKoin()).asObject());
        colUserRole.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getRoleLabel()));

        colUserAksi.setCellFactory(col -> new TableCell<>() {
            private final Button hapusBtn = new Button("Hapus");
            {
                hapusBtn.setStyle(
                    "-fx-background-color: #fee2e2;" +
                    "-fx-text-fill: #e74c3c;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand; -fx-font-size: 11px;");
                hapusBtn.setOnAction(e -> {
                    UserRecord rec = getTableView()
                        .getItems().get(getIndex());
                    handleHapusUser(rec);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                // Jangan tampilkan hapus untuk admin
                UserRecord rec = getTableView()
                    .getItems().get(getIndex());
                setGraphic(rec.isAdmin() ? null : hapusBtn);
            }
        });
    }

    private void setupPemasukanTab() {
        colPEmail.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getEmail()));
        colPTanggal.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTanggal()));
        colPKode.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getKode()));
        colPKoin.setCellValueFactory(d ->
            new SimpleIntegerProperty(
                d.getValue().getCoinAmount()).asObject());
        colPHarga.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getHarga()));
        colPMetode.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getMetodeBayar()));
        colPStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getStatus()));
    }

    private void setupKatalogTab() {
        // Services
        colSvcNama.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getName()));
        colSvcDomain.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDomain()));
        colSvcKategori.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCategory()));
        colSvcCurrency.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDefaultCurrency()));

        colSvcAksi.setCellFactory(col -> new TableCell<>() {
            private final Button hapusBtn = new Button("Hapus");
            {
                hapusBtn.setStyle(
                    "-fx-background-color: #fee2e2;" +
                    "-fx-text-fill: #e74c3c;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand; -fx-font-size: 11px;");
                hapusBtn.setOnAction(e -> {
                    Service svc = getTableView()
                        .getItems().get(getIndex());
                    handleHapusService(svc);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : hapusBtn);
            }
        });

        // Listener: saat service dipilih, load tier-nya
        serviceTable.getSelectionModel()
            .selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedService = newVal;
                    if (newVal != null) {
                        tierHeaderLabel.setText(
                            "Tier untuk: " + newVal.getName());
                        loadTiers(newVal.getId());
                    } else {
                        tierHeaderLabel.setText("Tier");
                        if (tierList != null) tierList.clear();
                    }
                });

        // Tiers
        colTierNama.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTierName()));
        colTierDesc.setCellValueFactory(d ->
            new SimpleStringProperty(
                d.getValue().getDescription() != null
                    ? d.getValue().getDescription() : "-"));

        colTierAksi.setCellFactory(col -> new TableCell<>() {
            private final Button hapusBtn = new Button("Hapus");
            {
                hapusBtn.setStyle(
                    "-fx-background-color: #fee2e2;" +
                    "-fx-text-fill: #e74c3c;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand; -fx-font-size: 11px;");
                hapusBtn.setOnAction(e -> {
                    ServiceTier tier = getTableView()
                        .getItems().get(getIndex());
                    handleHapusTier(tier);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : hapusBtn);
            }
        });


    }

    // ═══════════════════════════════════════════════════════
    // LOAD DATA
    // ═══════════════════════════════════════════════════════

    private void loadAllData() {
        new Thread(() -> {
            List<UserRecord>    users     = ServiceDAO.loadAllUsers();
            List<PurchaseRecord> purchases = ServiceDAO.loadAllPurchases();
            List<Service>       services  = ServiceDAO.loadAllServices();
            long revenue = ServiceDAO.getTotalRevenue();

            Platform.runLater(() -> {
                // Tab User
                userList = FXCollections.observableArrayList(users);
                userTable.setItems(userList);
                totalUserLabel.setText(users.size() + " user terdaftar");

                // Tab Pemasukan
                pemasukanList = FXCollections.observableArrayList(purchases);
                pemasukanTable.setItems(pemasukanList);
                totalPemasukanLabel.setText(
                    "Rp " + String.format("%,d", revenue));
                totalTransaksiLabel.setText(
                    purchases.size() + " transaksi");

                // Tab Katalog
                serviceList = FXCollections.observableArrayList(services);
                serviceTable.setItems(serviceList);
                tierList = FXCollections.observableArrayList();
                tierTable.setItems(tierList);
            });
        }).start();
    }

    private void loadTiers(long serviceId) {
        new Thread(() -> {
            List<ServiceTier> tiers =
                ServiceDAO.loadTiersByService(serviceId);
            Platform.runLater(() -> {
                tierList.setAll(tiers);
            });
        }).start();
    }

    // ═══════════════════════════════════════════════════════
    // HANDLER — USER TAB
    // ═══════════════════════════════════════════════════════

    private void handleHapusUser(UserRecord rec) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus User");
        konfirmasi.setHeaderText("Hapus " + rec.getEmail() + "?");
        konfirmasi.setContentText(
            "Semua data user ini (langganan, koin, transaksi) " +
            "akan ikut dihapus. Tindakan ini tidak bisa dibatalkan.");

        konfirmasi.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = ServiceDAO.deleteUser(rec.getId());
                    Platform.runLater(() -> {
                        if (ok) {
                            userList.remove(rec);
                            totalUserLabel.setText(
                                userList.size() + " user terdaftar");
                        } else {
                            showAlert("Gagal",
                                "User gagal dihapus.", 
                                Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    // HANDLER — KATALOG TAB
    // ═══════════════════════════════════════════════════════

    @FXML
    private void handleTambahService() {
        labelSvcError.setText("");
        String nama   = tfSvcNama.getText().trim();
        String domain = tfSvcDomain.getText().trim();

        if (nama.isEmpty() || domain.isEmpty()) {
            labelSvcError.setText("Nama dan domain wajib diisi.");
            return;
        }

        Service svc = new Service();
        svc.setName(nama);
        svc.setDomain(domain);
        svc.setCancellationUrl(tfSvcCancelUrl.getText().trim());
        svc.setCategory(tfSvcKategori.getText().trim());
        svc.setDefaultCurrency("USD"); // default USD

        new Thread(() -> {
            long id = ServiceDAO.saveService(svc);
            Platform.runLater(() -> {
                if (id != -1L) {
                    svc.setId(id);
                    serviceList.add(svc);
                    tfSvcNama.clear();
                    tfSvcDomain.clear();
                    tfSvcCancelUrl.clear();
                    tfSvcKategori.clear();
                } else {
                    labelSvcError.setText(
                        "Gagal menyimpan layanan ke database.");
                }
            });
        }).start();
    }

    private void handleHapusService(Service svc) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Layanan");
        konfirmasi.setHeaderText("Hapus " + svc.getName() + "?");
        konfirmasi.setContentText(
            "Semua tier layanan ini juga akan dihapus.");

        konfirmasi.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = ServiceDAO.deleteService(svc.getId());
                    Platform.runLater(() -> {
                        if (ok) {
                            serviceList.remove(svc);
                            if (selectedService != null &&
                                selectedService.getId().equals(svc.getId())) {
                                tierList.clear();
                                tierHeaderLabel.setText("Tier");
                                selectedService = null;
                            }
                        } else {
                            showAlert("Gagal",
                                "Layanan gagal dihapus.",
                                Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            }
        });
    }

    @FXML
    private void handleTambahTier() {
        labelTierError.setText("");

        if (selectedService == null) {
            labelTierError.setText(
                "Pilih layanan di tabel atas terlebih dahulu.");
            return;
        }

        String namaTier = tfTierNama.getText().trim();
        if (namaTier.isEmpty()) {
            labelTierError.setText("Nama tier wajib diisi.");
            return;
        }

        ServiceTier tier = new ServiceTier();
        tier.setTierName(namaTier);
        tier.setDescription(tfTierDesc.getText().trim());

        new Thread(() -> {
            long id = ServiceDAO.saveTier(tier, selectedService.getId());
            Platform.runLater(() -> {
                if (id != -1L) {
                    tier.setId(id);
                    tierList.add(tier);
                    tfTierNama.clear();
                    tfTierDesc.clear();
                } else {
                    labelTierError.setText(
                        "Gagal menyimpan tier ke database.");
                }
            });
        }).start();
    }

    private void handleHapusTier(ServiceTier tier) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Tier");
        konfirmasi.setHeaderText("Hapus tier " + tier.getTierName() + "?");

        konfirmasi.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = ServiceDAO.deleteTier(tier.getId());
                    Platform.runLater(() -> {
                        if (ok) {
                            tierList.remove(tier);
                        } else {
                            showAlert("Gagal",
                                "Tier gagal dihapus.",
                                Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════

    private void showAlert(String title, String msg,
                            Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
    }
}