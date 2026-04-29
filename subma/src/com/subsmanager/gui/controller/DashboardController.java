package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.subscription.model.Subscription;
import com.subsmanager.currency.CurrencyConverter;
import com.subsmanager.financial.FinancialSummary;
import com.subsmanager.manager.SubscriptionManager;
import com.subsmanager.overlay.OverlayController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * DashboardController - Controller untuk dashboard.fxml
 *
 * Aggregasi:
 * - DashboardController o-- Subscription (ditampilkan di tabel)
 *
 * Dependency:
 * - DashboardController ..> SessionManager (ambil user, navigasi)
 * - DashboardController ..> User (ambil data langganan & koin)
 */
public class DashboardController implements Initializable {

    // ===== Label kartu ringkasan =====
    @FXML private Label greetingLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label totalSubsLabel;
    @FXML private Label monthlyLabel;
    @FXML private Label yearlyLabel;
    @FXML private Label coinLabel;

    // ===== Tabel langganan =====
    @FXML private TableView<Subscription>         subscriptionTable;
    @FXML private TableColumn<Subscription, String> colNama;
    @FXML private TableColumn<Subscription, String> colBiaya;
    @FXML private TableColumn<Subscription, String> colSiklus;
    @FXML private TableColumn<Subscription, String> colTanggal;
    @FXML private TableColumn<Subscription, String> colKategori;

    /**
     * initialize() dipanggil otomatis oleh JavaFX setelah FXML di-load.
     * Di sini kita setup tabel dan isi data dari user yang login.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadUserData();
    }

    /**
     * Setup kolom TableView dengan PropertyValueFactory.
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
        colKategori.setCellValueFactory(
            new PropertyValueFactory<>("tier"));
    }

    /**
     * Load data user yang sedang login ke UI.
     */
    private void loadUserData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        // Greeting
        greetingLabel.setText("Selamat datang, " + user.getEmail() + "!");
        userEmailLabel.setText(user.getEmail());

        // Saldo koin
        coinLabel.setText(String.valueOf(
            user.getCoinBalance().getBalance()));

        // Data langganan
        ObservableList<Subscription> list =
            FXCollections.observableArrayList(user.getSubscriptions());
        subscriptionTable.setItems(list);

        // Total langganan
        totalSubsLabel.setText(String.valueOf(list.size()));

        // Hitung biaya di background thread agar tidak freeze
        new Thread(() -> {
            try {
            	CurrencyConverter converter = new CurrencyConverter();

            	OverlayController overlay = new OverlayController(0, 0);
            	SubscriptionManager manager = new SubscriptionManager(overlay);

            	FinancialSummary summary = new FinancialSummary(manager, converter);

                double monthly = summary.getMonthlyOutcome();
                double yearly  = summary.getYearlyProjection();

                javafx.application.Platform.runLater(() -> {
                    monthlyLabel.setText(converter.formatIDR(monthly));
                    yearlyLabel.setText(converter.formatIDR(yearly));
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    monthlyLabel.setText("Rp -");
                    yearlyLabel.setText("Rp -");
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ===== Navigasi Sidebar =====

    @FXML private void showDashboard() {
        loadUserData(); // refresh
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

    @FXML private void showAddSubscription() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/addsub.fxml");
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
    }
}