package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.db.SubscriptionDAO;
import com.subsmanager.subscription.model.Subscription;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * SubscriptionController - Controller untuk subscription.fxml
 *
 * Aggregasi:
 * - SubscriptionController o-- Subscription (list yang ditampilkan)
 *
 * Dependency:
 * - SubscriptionController ..> SessionManager (navigasi + ambil user)
 * - SubscriptionController ..> User (ambil daftar langganan)
 */
public class SubscriptionController implements Initializable {

    // ===== Sidebar =====
    @FXML private Label userEmailLabel;

    // ===== Header =====
    @FXML private Label subtitleLabel;

    // ===== Search & Filter =====
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterCombo;

    // ===== Tabel =====
    @FXML private TableView<Subscription>           subscriptionTable;
    @FXML private TableColumn<Subscription, String> colNama;
    @FXML private TableColumn<Subscription, String> colBiaya;
    @FXML private TableColumn<Subscription, String> colSiklus;
    @FXML private TableColumn<Subscription, String> colTanggal;
    @FXML private TableColumn<Subscription, String> colTier;
    @FXML private TableColumn<Subscription, Void>   colAksi;

    /** Data asli dari user */
    private ObservableList<Subscription> masterList;

    /** Data yang sudah difilter */
    private FilteredList<Subscription> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilter();
        loadData();
    }

    /**
     * Setup kolom tabel.
     */
    private void setupTable() {
        colNama.setCellValueFactory(
            new PropertyValueFactory<>("serviceName"));
        colBiaya.setCellValueFactory(
            new PropertyValueFactory<>("cost"));
        colSiklus.setCellValueFactory(
            new PropertyValueFactory<>("billingCycle"));
        colTanggal.setCellValueFactory(
            new PropertyValueFactory<>("billingDate"));
        colTier.setCellValueFactory(
            new PropertyValueFactory<>("tier"));

        // Kolom aksi: tombol Cancel + Hapus per baris
        colAksi.setCellFactory(col -> new TableCell<>() {

            private final Button cancelBtn = new Button("Batal Langganan");
            private final Button hapusBtn  = new Button("Hapus");
            private final HBox   box       = new HBox(6, cancelBtn, hapusBtn);

            {
                // Style tombol Cancel
                cancelBtn.setStyle(
                    "-fx-background-color: #fff3cd;" +
                    "-fx-text-fill: #856404;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;"
                );

                // Style tombol Hapus
                hapusBtn.setStyle(
                    "-fx-background-color: #fee2e2;" +
                    "-fx-text-fill: #e74c3c;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 11px;"
                );

                cancelBtn.setOnAction(e -> {
                    Subscription sub = getTableView()
                        .getItems().get(getIndex());
                    handleCancel(sub);
                });

                hapusBtn.setOnAction(e -> {
                    Subscription sub = getTableView()
                        .getItems().get(getIndex());
                    handleHapus(sub);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    /**
     * Setup ComboBox filter kategori dan search field listener.
     */
    private void setupFilter() {
        filterCombo.setItems(FXCollections.observableArrayList(
            "Semua", "Streaming", "Musik", "Produktivitas",
            "Gaming", "Cloud", "Lainnya"
        ));
        filterCombo.setValue("Semua");

        searchField.textProperty().addListener(
            (obs, oldVal, newVal) -> applyFilter());
        filterCombo.valueProperty().addListener(
            (obs, oldVal, newVal) -> applyFilter());
    }

    /**
     * Load data dari user yang sedang login.
     */
    private void loadData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        userEmailLabel.setText(user.getEmail());

        masterList   = FXCollections.observableArrayList(
            user.getSubscriptions());
        filteredList = new FilteredList<>(masterList, p -> true);

        subscriptionTable.setItems(filteredList);
        updateSubtitle();
    }

    /**
     * Filter tabel berdasarkan input search dan ComboBox.
     */
    private void applyFilter() {
        String keyword  = searchField.getText().toLowerCase().trim();
        String kategori = filterCombo.getValue();

        filteredList.setPredicate(sub -> {
            boolean matchSearch = keyword.isEmpty() ||
                sub.getServiceName().toLowerCase().contains(keyword);

            boolean matchKategori = kategori == null ||
                kategori.equals("Semua") ||
                sub.getTier().toLowerCase().contains(
                    kategori.toLowerCase());

            return matchSearch && matchKategori;
        });

        updateSubtitle();
    }

    /**
     * Update teks subtitle jumlah langganan.
     */
    private void updateSubtitle() {
        int total = filteredList != null ? filteredList.size() : 0;
        subtitleLabel.setText(total + " langganan aktif");
    }

    /**
     * Tampilkan dialog cancel langganan dengan link ke halaman
     * pembatalan resmi layanan.
     * Polymorphism: getCancelPageURL() dipanggil sama untuk semua
     * jenis Subscription, hasilnya berbeda sesuai implementasi.
     *
     * @param sub langganan yang akan dibatalkan
     */
    private void handleCancel(Subscription sub) {
        String cancelUrl = sub.getCancelPageURL();

        // Buat dialog dengan 2 tombol custom
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Batalkan Langganan");
        dialog.setHeaderText("Batalkan " + sub.getServiceName() + "?");

        // Isi konten dialog
        String content =
            "Untuk membatalkan langganan ini, kamu perlu mengunjungi\n" +
            "halaman resmi layanan tersebut.\n\n" +
            "URL Pembatalan:\n" + cancelUrl;
        dialog.setContentText(content);

        // Ganti tombol default dengan tombol custom
        ButtonType btnBuka   = new ButtonType("Buka di Browser");
        ButtonType btnTutup  = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(btnBuka, btnTutup);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnBuka) {
                bukaUrl(cancelUrl);
            }
        });
    }

    /**
     * Buka URL di browser default sistem operasi.
     * Menggunakan java.awt.Desktop — berjalan di Windows, Mac, Linux.
     *
     * @param url URL yang akan dibuka
     */
    private void bukaUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println(
                    "[SubscriptionController] Membuka URL: " + url);
            } else {
                // Fallback jika Desktop tidak support
                showAlert("Tidak Bisa Membuka Browser",
                    "Salin URL ini ke browser kamu:\n" + url,
                    Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            showAlert("Gagal Membuka URL",
                "Salin URL ini ke browser kamu:\n" + url,
                Alert.AlertType.ERROR);
            System.err.println(
                "[SubscriptionController] Gagal buka URL: " + e.getMessage());
        }
    }

    /**
     * Hapus langganan dari list user.
     *
     * @param sub langganan yang akan dihapus
     */
    private void handleHapus(Subscription sub) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Hapus Langganan");
        konfirmasi.setHeaderText("Hapus " + sub.getServiceName() + "?");
        konfirmasi.setContentText(
            "Langganan ini akan dihapus dari daftar.");

        konfirmasi.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                User user = SessionManager.getCurrentUser();

                // Hapus dari DB di background thread
                new Thread(() -> {
                    boolean berhasil =
                        SubscriptionDAO.delete(sub.getId());

                    javafx.application.Platform.runLater(() -> {
                        if (berhasil) {
                            // Hapus dari memori dan UI
                            user.removeSubscription(sub);
                            masterList.remove(sub);
                            updateSubtitle();
                            System.out.println(
                                "[SubscriptionController] Hapus berhasil: "
                                + sub.getServiceName());
                        } else {
                            showAlert("Gagal Menghapus",
                                "Langganan gagal dihapus dari database." +
                                " Coba lagi.",
                                Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            }
        });
    }

    /**
     * Helper tampilkan Alert.
     */
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ===== Navigasi =====

    @FXML private void handleAdd() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/addsub.fxml");
    }

    @FXML private void handleReset() {
        searchField.clear();
        filterCombo.setValue("Semua");
    }

    @FXML private void showDashboard() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/dashboard.fxml");
    }

    @FXML private void showFinancial() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/financial.fxml");
    }


    @FXML private void showCoinHistory() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/coinhistory.fxml");
    }
    @FXML private void showCoinStore() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/coinstore.fxml");
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
    }
}