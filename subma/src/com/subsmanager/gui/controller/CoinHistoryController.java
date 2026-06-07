package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.db.CoinDAO;
import com.subsmanager.db.CoinDAO.TransactionRecord;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

/**
 * CoinHistoryController - Controller untuk coinhistory.fxml
 *
 * Menampilkan riwayat transaksi koin user dengan filter:
 * - Semua transaksi
 * - Top Up (PURCHASE)
 * - Penggunaan (USAGE)
 *
 * Dependency:
 * - CoinHistoryController ..> CoinDAO (load riwayat dari DB)
 * - CoinHistoryController ..> SessionManager (ambil user + navigasi)
 */
public class CoinHistoryController implements Initializable {

    /**
     * Tampilkan FileChooser lalu cetak bukti top up koin ke PDF.
     * Hanya dipanggil untuk transaksi bertipe PURCHASE.
     *
     * @param rec data transaksi yang akan dicetak
     */
    private void cetakStrukRiwayat(TransactionRecord rec) {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Bukti Transaksi");
        fileChooser.setInitialFileName(
            "struk_" + rec.getKode() + ".pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(
            SessionManager.getPrimaryStage());
        if (file == null) return;

        new Thread(() -> {
            try {
                ExportService exportService = new ExportService(
                    user,
                    new com.subsmanager.currency.CurrencyConverter());
                exportService.exportReceiptFromRecord(
                    rec, user, file.getAbsolutePath());

                Platform.runLater(() ->
                    new Alert(
                        Alert.AlertType.INFORMATION,
                        "Struk berhasil disimpan di:\n"
                        + file.getAbsolutePath())
                    .showAndWait()
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                    new Alert(
                        Alert.AlertType.ERROR,
                        "Gagal mencetak struk: "
                        + e.getMessage())
                    .showAndWait()
                );
                e.printStackTrace();
            }
        }).start();
    }

    // ── Sidebar ───────────────────────────────────────────
    @FXML private Label userEmailLabel;

    // ── Header ────────────────────────────────────────────
    @FXML private Label coinBalanceLabel;
    @FXML private Label totalTransaksiLabel;

    // ── Filter ────────────────────────────────────────────
    @FXML private ToggleGroup filterToggle;
    @FXML private RadioButton rbSemua;
    @FXML private RadioButton rbTopUp;
    @FXML private RadioButton rbPenggunaan;

    // ── Tabel ─────────────────────────────────────────────
    @FXML private TableView<TransactionRecord>           historyTable;
    @FXML private TableColumn<TransactionRecord, String> colTanggal;
    @FXML private TableColumn<TransactionRecord, String> colKode;
    @FXML private TableColumn<TransactionRecord, String> colTipe;
    @FXML private TableColumn<TransactionRecord, String> colDeskripsi;
    @FXML private TableColumn<TransactionRecord, Integer> colKoin;
    @FXML private TableColumn<TransactionRecord, String> colHarga;
    @FXML private TableColumn<TransactionRecord, String> colMetode;
    @FXML private TableColumn<TransactionRecord, String> colStatus;
    @FXML private TableColumn<TransactionRecord, Void>   colAksi;

    // ── Label kosong ──────────────────────────────────────
    @FXML private Label emptyLabel;

    // ── Data ──────────────────────────────────────────────
    private ObservableList<TransactionRecord> masterList;
    private FilteredList<TransactionRecord>   filteredList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilter();
        loadData();
    }

    /**
     * Setup kolom tabel dengan cell value factory.
     */
    private void setupTable() {
        colTanggal.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTanggal()));

        colKode.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getKode()));

        colTipe.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTipeLabel()));

        // Warnai kolom tipe: hijau=Top Up, merah=Penggunaan
        colTipe.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Top Up".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colDeskripsi.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDeskripsi()));

        colKoin.setCellValueFactory(data ->
            new SimpleIntegerProperty(
                data.getValue().getJumlahKoin()).asObject());

        // Warnai kolom koin: hijau=Top Up (tambah), merah=Penggunaan (kurang)
        colKoin.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    TransactionRecord rec = getTableView()
                        .getItems().get(getIndex());
                    if ("PURCHASE".equals(rec.getTipe())) {
                        setText("+" + item);
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("-" + item);
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colHarga.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getHarga()));

        colMetode.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getMetodeBayar()));

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus()));

        // Kolom Aksi — tombol Print hanya untuk PURCHASE
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnPrint = new Button("Print");
            {
                btnPrint.setStyle(
                    "-fx-background-color: transparent;"
                    + "-fx-border-color: #4f46e5;"
                    + "-fx-border-radius: 5;"
                    + "-fx-background-radius: 5;"
                    + "-fx-text-fill: #4f46e5;"
                    + "-fx-font-size: 11px;"
                    + "-fx-cursor: hand;"
                    + "-fx-padding: 3 8 3 8;");
                btnPrint.setOnAction(e -> {
                    TransactionRecord rec = getTableView()
                        .getItems().get(getIndex());
                    cetakStrukRiwayat(rec);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                TransactionRecord rec = getTableView()
                    .getItems().get(getIndex());
                // Hanya tampilkan tombol untuk transaksi top up
                setGraphic("PURCHASE".equals(rec.getTipe())
                    ? btnPrint : null);
            }
        });
    }

    /**
     * Setup listener filter RadioButton.
     * Setiap kali pilihan berubah, FilteredList di-update.
     */
    private void setupFilter() {
        // Listener akan dipasang setelah data di-load
    }

    /**
     * Load data riwayat transaksi dari DB di background thread.
     */
    private void loadData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        userEmailLabel.setText(user.getEmail());
        coinBalanceLabel.setText(
            String.valueOf(user.getCoinAmount()) + " Koin");

        // Load dari DB di background thread
        new Thread(() -> {
            List<TransactionRecord> records =
                CoinDAO.loadTransactions(user);

            Platform.runLater(() -> {
                masterList   = FXCollections.observableArrayList(records);
                filteredList = new FilteredList<>(masterList, p -> true);
                historyTable.setItems(filteredList);

                updateSubtitle();
                pasangFilterListener();
                updateEmptyLabel();
            });

        }).start();
    }

    /**
     * Pasang listener ke RadioButton setelah data tersedia.
     */
    private void pasangFilterListener() {
        filterToggle.selectedToggleProperty().addListener(
            (obs, oldVal, newVal) -> applyFilter());
    }

    /**
     * Terapkan filter berdasarkan RadioButton yang dipilih.
     */
    private void applyFilter() {
        if (filteredList == null) return;

        if (rbTopUp.isSelected()) {
            filteredList.setPredicate(
                r -> "PURCHASE".equals(r.getTipe()));
        } else if (rbPenggunaan.isSelected()) {
            filteredList.setPredicate(
                r -> "USAGE".equals(r.getTipe()));
        } else {
            // Semua
            filteredList.setPredicate(p -> true);
        }

        updateSubtitle();
        updateEmptyLabel();
    }

    /**
     * Update label jumlah transaksi yang tampil.
     */
    private void updateSubtitle() {
        int total = filteredList != null ? filteredList.size() : 0;
        totalTransaksiLabel.setText(total + " transaksi");
    }

    /**
     * Tampilkan/sembunyikan label "Belum ada transaksi".
     */
    private void updateEmptyLabel() {
        boolean isEmpty = filteredList == null || filteredList.isEmpty();
        emptyLabel.setVisible(isEmpty);
        emptyLabel.setManaged(isEmpty);
        historyTable.setVisible(!isEmpty);
        historyTable.setManaged(!isEmpty);
    }

    // ── Navigasi Sidebar ──────────────────────────────────

    @FXML private void showDashboard() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/dashboard.fxml");
    }

    @FXML private void showSubscriptions() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/subscription.fxml");
    }

    @FXML private void showFinancial() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/financial.fxml");
    }

    @FXML private void showCoinStore() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/coinstore.fxml");
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
    }
}