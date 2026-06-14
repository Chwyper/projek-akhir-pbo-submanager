package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.currency.CurrencyConverter;
import com.subsmanager.financial.FinancialSummary;
import com.subsmanager.manager.SubscriptionManager;
import com.subsmanager.subscription.model.Subscription;
import com.subsmanager.gui.controller.ExportService;
import com.subsmanager.coin.CoinService;


import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.io.File;

import java.net.URL;
import java.util.List;
//import java.util.Map;
import java.util.ResourceBundle;

/**
 * FinancialController - Controller untuk financial.fxml
 *
 * Dependency:
 * - FinancialController ..> FinancialSummary (hitung pengeluaran)
 * - FinancialController ..> CurrencyConverter (ambil kurs)
 * - FinancialController ..> SubscriptionManager (load data)
 * - FinancialController ..> SessionManager (navigasi + ambil user)
 */
public class FinancialController implements Initializable {

    // ===== Sidebar =====
    @FXML private Label userEmailLabel;

    // ===== Header =====
    @FXML private Label lastUpdatedLabel;

    // ===== Kartu ringkasan =====
    @FXML private Label monthlyTotalLabel;
    @FXML private Label yearlyTotalLabel;
    @FXML private Label exchangeRateLabel;
    @FXML private Label avgLabel;

    // ===== Tabel breakdown =====
    @FXML private TableView<Subscription>           breakdownTable;
    @FXML private TableColumn<Subscription, String> colNama;
    @FXML private TableColumn<Subscription, String> colBiayaAsli;
    @FXML private TableColumn<Subscription, String> colBiayaBulanan;
    @FXML private TableColumn<Subscription, String> colBiayaTahunan;
    @FXML private TableColumn<Subscription, String> colSiklus;

    /** Komponen keuangan */
    private CurrencyConverter  converter;
    private SubscriptionManager manager;
    private FinancialSummary   summary;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    /**
     * Setup kolom tabel dengan SimpleStringProperty
     * karena nilai sudah diformat sebagai String.
     */
    private void setupTable() {
        colNama.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getServiceName()));

        colBiayaAsli.setCellValueFactory(data -> {
            Subscription s = data.getValue();
            return new SimpleStringProperty(
                s.getCurrency() + " " + s.getCost());
        });

        colBiayaBulanan.setCellValueFactory(data -> {
            Subscription s = data.getValue();
            if (converter == null) return
                new SimpleStringProperty("-");
            double rate = converter.getRate();
            double monthly = s.getMonthlyCostInIDR(rate);
            return new SimpleStringProperty(
                converter.formatIDR(monthly));
        });

        colBiayaTahunan.setCellValueFactory(data -> {
            Subscription s = data.getValue();
            if (converter == null) return
                new SimpleStringProperty("-");
            double rate = converter.getRate();
            double monthly = s.getMonthlyCostInIDR(rate);
            return new SimpleStringProperty(
                converter.formatIDR(monthly * 12));
        });

        colSiklus.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getBillingCycle().getLabel()));
    }

    /**
     * Load data keuangan dari user yang sedang login.
     * CurrencyConverter di-fetch di background thread
     * agar UI tidak freeze.
     */
    private void loadData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        userEmailLabel.setText(user.getEmail());

        // Setup manager & summary
        converter = new CurrencyConverter();
        manager   = new SubscriptionManager();
        manager.loadSubscriptions(user);
        summary   = new FinancialSummary(manager, converter);

        // Isi tabel dulu dengan data yang ada
        ObservableList<Subscription> list =
            FXCollections.observableArrayList(
                user.getSubscriptions());
        breakdownTable.setItems(list);

        // Fetch kurs di background thread
        new Thread(() -> {
            converter.fetchLatestRate();
            Platform.runLater(() -> updateLabels(user));
        }).start();
    }

    /**
     * Update semua label ringkasan setelah kurs berhasil di-fetch.
     *
     * @param user user yang sedang login
     */
    private void updateLabels(User user) {
        double rate    = converter.getRate();
        double monthly = summary.getMonthlyOutcome();
        double yearly  = summary.getYearlyProjection();

        List<Subscription> subs = user.getSubscriptions();
        double avg = subs.isEmpty() ? 0 : monthly / subs.size();

        monthlyTotalLabel.setText(converter.formatIDR(monthly));
        yearlyTotalLabel.setText(converter.formatIDR(yearly));
        exchangeRateLabel.setText(converter.formatIDR(rate));
        avgLabel.setText(converter.formatIDR(avg));

        lastUpdatedLabel.setText("Kurs diperbarui: "
            + converter.getLastUpdated());

        // Refresh tabel agar kolom IDR ter-update
        breakdownTable.refresh();
    }

    /**
     * Perbarui kurs secara manual saat tombol diklik.
     */
    @FXML
    private void handleRefreshRate() {
        lastUpdatedLabel.setText("Memperbarui kurs...");
        User user = SessionManager.getCurrentUser();

        new Thread(() -> {
            converter.fetchLatestRate();
            Platform.runLater(() -> updateLabels(user));
        }).start();
    }

 
    @FXML
    private void handleExportPDF() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        int cost = 10;

        // Cek saldo koin
        if (!user.getCoinBalance().hasSufficientBalance(cost)) {
            showAlert("Koin Tidak Cukup",
                "Export PDF membutuhkan " + cost + " koin.\n" +
                "Saldo kamu: " + user.getCoinBalance().getBalance() +
                " koin.\nBeli koin di Toko Koin.",
                Alert.AlertType.WARNING);
            return;
        }

        // Dialog konfirmasi
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Export PDF");
        konfirmasi.setHeaderText("Export membutuhkan " + cost + " koin");
        konfirmasi.setContentText(
            "Saldo koin kamu: " + user.getCoinBalance().getBalance() +
            "\nLanjutkan export PDF?");

        konfirmasi.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Pilih lokasi simpan file
                javafx.stage.FileChooser fileChooser =
                    new javafx.stage.FileChooser();
                fileChooser.setTitle("Simpan PDF");
                fileChooser.setInitialDirectory(
                	    new File(System.getProperty("user.home") + "/Documents"));
                fileChooser.setInitialFileName(
                    "langganan_" + user.getEmail()
                        .replace("@", "_")
                        .replace(".", "_") + ".pdf");
                fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter(
                        "PDF Files", "*.pdf"));

                File file = fileChooser.showSaveDialog(
                    SessionManager.getPrimaryStage());
                if (file == null) return;

                // Proses export di background thread
                new Thread(() -> {
                    try {
                        ExportService exportService =
                            new ExportService(user, converter);
                        exportService.exportPDF(file.getAbsolutePath());

                        // Kurangi koin + catat ke DB via CoinService
                        new CoinService().useCoins(user, cost, "Export PDF");

                        javafx.application.Platform.runLater(() -> {
                            showAlert("Export Berhasil",
                                "PDF berhasil disimpan di:\n" +
                                file.getAbsolutePath() + "\n\n" +
                                "Sisa koin: " +
                                user.getCoinBalance().getBalance(),
                                Alert.AlertType.INFORMATION);
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Export Gagal",
                                "Terjadi error: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                        });
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }


    @FXML
    private void handleExportExcel() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        int cost = 15;

        // Cek saldo koin
        if (!user.getCoinBalance().hasSufficientBalance(cost)) {
            showAlert("Koin Tidak Cukup",
                "Export Excel membutuhkan " + cost + " koin.\n" +
                "Saldo kamu: " + user.getCoinBalance().getBalance() +
                " koin.\nBeli koin di Toko Koin.",
                Alert.AlertType.WARNING);
            return;
        }

        // Dialog konfirmasi
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Export Excel");
        konfirmasi.setHeaderText("Export membutuhkan " + cost + " koin");
        konfirmasi.setContentText(
            "Saldo koin kamu: " + user.getCoinBalance().getBalance() +
            "\nLanjutkan export Excel?");

        konfirmasi.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Pilih lokasi simpan file
                javafx.stage.FileChooser fileChooser =
                    new javafx.stage.FileChooser();
                fileChooser.setTitle("Simpan Excel");
                fileChooser.setInitialDirectory(
                	    new File(System.getProperty("user.home") + "/Documents"));
                fileChooser.setInitialFileName(
                    "langganan_" + user.getEmail()
                        .replace("@", "_")
                        .replace(".", "_") + ".xlsx");
                fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter(
                        "Excel Files", "*.xlsx"));

                File file = fileChooser.showSaveDialog(
                    SessionManager.getPrimaryStage());
                if (file == null) return;

                // Proses export di background thread
                new Thread(() -> {
                    try {
                        ExportService exportService =
                            new ExportService(user, converter);
                        exportService.exportExcel(file.getAbsolutePath());

                        // Kurangi koin + catat ke DB via CoinService
                        new CoinService().useCoins(user, cost, "Export Excel");

                        javafx.application.Platform.runLater(() -> {
                            showAlert("Export Berhasil",
                                "Excel berhasil disimpan di:\n" +
                                file.getAbsolutePath() + "\n\n" +
                                "Sisa koin: " +
                                user.getCoinBalance().getBalance(),
                                Alert.AlertType.INFORMATION);
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Export Gagal",
                                "Terjadi error: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                        });
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /**
     * Helper tampilkan Alert dialog.
     */
    private void showAlert(String title,
                            String message,
                            Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== Navigasi =====

    @FXML private void showDashboard() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/dashboard.fxml");
    }

    @FXML private void showSubscriptions() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/subscription.fxml");
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