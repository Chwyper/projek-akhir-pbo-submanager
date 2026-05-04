package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;

import com.subsmanager.auth.User;
import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;
import com.subsmanager.db.ServiceDAO;
import com.subsmanager.db.SubscriptionDAO;
import com.subsmanager.subscription.model.BillingCycle;
import com.subsmanager.subscription.model.CustomSubscription;
import com.subsmanager.subscription.model.PredefinedSubscription;
import com.subsmanager.subscription.model.Subscription;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.util.Date;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * AddSubController - Controller untuk addsub.fxml
 *
 * Dependency:
 * - AddSubController ..> SessionManager (navigasi + ambil user)
 * - AddSubController ..> User (tambah langganan)
 * - AddSubController ..> PredefinedSubscription (buat dari katalog)
 * - AddSubController ..> CustomSubscription (buat custom)
 * - AddSubController ..> BillingCycle (isi ComboBox siklus)
 */
public class AddSubController implements Initializable {

    // ===== Sidebar =====
    @FXML private Label userEmailLabel;

    // ===== Toggle tipe =====
    @FXML private RadioButton radioPredefined;
    @FXML private RadioButton radioCustom;

    // ===== Form fields =====
    @FXML private VBox       katalogBox;
    @FXML private VBox       customNamaBox;
    @FXML private VBox       customUrlBox;

    @FXML private ComboBox<String>  serviceCombo;
    @FXML private TextField         customNamaField;
    @FXML private ComboBox<String>  tierCombo;
    @FXML private ComboBox<String>  currencyCombo;
    @FXML private TextField         biayaField;
    @FXML private ComboBox<String>  siklusCombo;
    @FXML private DatePicker        tanggalPicker;
    @FXML private TextField         customUrlField;
    @FXML private Label             errorLabel;

    // ===== Preview =====
    @FXML private Label previewNama;
    @FXML private Label previewTier;
    @FXML private Label previewBiaya;
    @FXML private Label previewSiklus;
    @FXML private Label previewTanggal;

    /**
     * Dummy katalog layanan.
     * TODO: ganti dengan load dari database nanti.
     */
    private List<Service> katalogList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupKatalog();
        setupComboBoxes();
        setupPreviewListeners();
        loadUserData();
    }

    /**
     * Load katalog layanan dari database di background thread.
     * Menggantikan katalog hardcoded sebelumnya.
     */
    private void setupKatalog() {
        serviceCombo.setDisable(true);

        new Thread(() -> {
            List<Service> services = ServiceDAO.loadAllServices();

            javafx.application.Platform.runLater(() -> {
                katalogList.clear();
                serviceCombo.getItems().clear();

                for (Service s : services) {
                    // Load tier tiap service dari DB
                    List<com.subsmanager.catalog.ServiceTier> tiers =
                        ServiceDAO.loadTiersByService(s.getId());
                    tiers.forEach(s::addTier);
                    katalogList.add(s);
                    serviceCombo.getItems().add(s.getName());
                }

                serviceCombo.setDisable(false);
                System.out.println("[AddSubController] Katalog dimuat: "
                    + katalogList.size() + " layanan.");
            });
        }).start();
    }

    /**
     * Setup ComboBox currency dan siklus.
     */
    private void setupComboBoxes() {
        currencyCombo.getItems().addAll("IDR", "USD", "EUR", "GBP");
        currencyCombo.setValue("IDR");

        siklusCombo.getItems().addAll(
            BillingCycle.MONTHLY.getLabel(),
            BillingCycle.YEARLY.getLabel()
        );
        siklusCombo.setValue(BillingCycle.MONTHLY.getLabel());

        tanggalPicker.setValue(LocalDate.now());
    }

    /**
     * Setup listener untuk live preview.
     */
    private void setupPreviewListeners() {
        customNamaField.textProperty().addListener(
            (o, ov, nv) -> updatePreview());
        biayaField.textProperty().addListener(
            (o, ov, nv) -> updatePreview());
        tierCombo.valueProperty().addListener(
            (o, ov, nv) -> updatePreview());
        siklusCombo.valueProperty().addListener(
            (o, ov, nv) -> updatePreview());
        tanggalPicker.valueProperty().addListener(
            (o, ov, nv) -> updatePreview());
        currencyCombo.valueProperty().addListener(
            (o, ov, nv) -> updatePreview());
    }

    /**
     * Load data user ke UI.
     */
    private void loadUserData() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            userEmailLabel.setText(user.getEmail());
        }
    }

    /**
     * Dipanggil saat RadioButton tipe berubah.
     * Toggle tampilan form katalog vs custom.
     */
    @FXML
    private void handleTypeChange() {
        boolean isPredefined = radioPredefined.isSelected();

        katalogBox.setVisible(isPredefined);
        katalogBox.setManaged(isPredefined);

        customNamaBox.setVisible(!isPredefined);
        customNamaBox.setManaged(!isPredefined);

        customUrlBox.setVisible(!isPredefined);
        customUrlBox.setManaged(!isPredefined);

        // Reset field
        serviceCombo.setValue(null);
        tierCombo.getItems().clear();
        errorLabel.setText("");
    }

    /**
     * Dipanggil saat layanan dipilih dari ComboBox katalog.
     * Update ComboBox tier sesuai layanan yang dipilih.
     */
    @FXML
    private void handleServiceSelected() {
        String selectedName = serviceCombo.getValue();
        if (selectedName == null) return;

        Service selected = katalogList.stream()
            .filter(s -> s.getName().equals(selectedName))
            .findFirst().orElse(null);

        if (selected != null) {
            tierCombo.getItems().clear();
            for (ServiceTier t : selected.getAvailableTiers()) {
                tierCombo.getItems().add(t.getTierName());
            }
            if (!tierCombo.getItems().isEmpty()) {
                tierCombo.setValue(tierCombo.getItems().get(0));
            }
            // Set currency default layanan
            currencyCombo.setValue(selected.getDefaultCurrency());
        }

        updatePreview();
    }

    /**
     * Update panel preview secara live.
     */
    private void updatePreview() {
        boolean isPredefined = radioPredefined.isSelected();

        String nama = isPredefined
            ? (serviceCombo.getValue() != null
                ? serviceCombo.getValue() : "Nama Layanan")
            : (customNamaField.getText().isEmpty()
                ? "Nama Layanan" : customNamaField.getText());

        previewNama.setText(nama);
        previewTier.setText("Tier: " +
            (tierCombo.getValue() != null
                ? tierCombo.getValue() : "-"));
        previewBiaya.setText("Biaya: " +
            currencyCombo.getValue() + " " +
            (biayaField.getText().isEmpty()
                ? "0" : biayaField.getText()));
        previewSiklus.setText("Siklus: " +
            (siklusCombo.getValue() != null
                ? siklusCombo.getValue() : "-"));
        previewTanggal.setText("Tgl Tagihan: " +
            (tanggalPicker.getValue() != null
                ? tanggalPicker.getValue().toString() : "-"));
    }

    /**
     * Simpan langganan baru ke user yang sedang login.
     * Polymorphism: buat PredefinedSubscription atau CustomSubscription
     * tergantung pilihan user.
     */
    @FXML
    private void handleSimpan() {
        if (!validasiForm()) return;

        User user = SessionManager.getCurrentUser();
        Subscription sub;

        if (radioPredefined.isSelected()) {
            // Buat PredefinedSubscription
            Service selectedService = katalogList.stream()
                .filter(s -> s.getName()
                    .equals(serviceCombo.getValue()))
                .findFirst().orElse(null);

            ServiceTier selectedTier = selectedService
                .getAvailableTiers().stream()
                .filter(t -> t.getTierName()
                    .equals(tierCombo.getValue()))
                .findFirst().orElse(null);

            PredefinedSubscription ps = new PredefinedSubscription();
            ps.setId(System.currentTimeMillis());
            ps.setServiceName(selectedService.getName());
            ps.setCost(Double.parseDouble(biayaField.getText()));
            ps.setCurrency(currencyCombo.getValue());
            ps.setBillingDate(Date.from(tanggalPicker.getValue()
            	    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            ps.setBillingCycle(
                siklusCombo.getValue().equals(
                    BillingCycle.MONTHLY.getLabel())
                ? BillingCycle.MONTHLY
                : BillingCycle.YEARLY
            );
            ps.setTier(tierCombo.getValue());
            ps.setService(selectedService);
            ps.setSelectedTier(selectedTier);
            sub = ps;

        } else {
            // Buat CustomSubscription
            CustomSubscription cs = new CustomSubscription();
            cs.setId(System.currentTimeMillis());
            cs.setServiceName(customNamaField.getText().trim());
            cs.setCost(Double.parseDouble(biayaField.getText()));
            cs.setCurrency(currencyCombo.getValue());
            cs.setBillingDate(Date.from(tanggalPicker.getValue()
            	    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cs.setBillingCycle(
                siklusCombo.getValue().equals(
                    BillingCycle.MONTHLY.getLabel())
                ? BillingCycle.MONTHLY
                : BillingCycle.YEARLY
            );
            cs.setTier(tierCombo.getValue() != null
                ? tierCombo.getValue() : "Custom");
            cs.setCustomCancelUrl(customUrlField.getText().trim());
            cs.setCustomDomain(
                customNamaField.getText().toLowerCase()
                    .replace(" ", "") + ".com");
            sub = cs;
        }

        // Simpan ke DB di background thread
        final Subscription finalSub = sub;
        errorLabel.setText("Menyimpan...");

        new Thread(() -> {
            long generatedId = SubscriptionDAO.save(
                finalSub, user.getId());

            javafx.application.Platform.runLater(() -> {
                if (generatedId == -1L) {
                    errorLabel.setText(
                        "Gagal menyimpan ke database. Coba lagi.");
                    return;
                }

                // Set ID dari DB (bukan System.currentTimeMillis)
                finalSub.setId(generatedId);
                user.addSubscription(finalSub);

                System.out.println(
                    "[AddSubController] Langganan disimpan: "
                    + finalSub.getServiceName()
                    + " (id=" + generatedId + ")");

                // Kembali ke halaman langganan
                SessionManager.navigateTo(
                    "/com/subsmanager/gui/fxml/subscription.fxml");
            });
        }).start();
    }

    /**
     * Validasi semua input form sebelum disimpan.
     *
     * @return true jika valid
     */
    private boolean validasiForm() {
        errorLabel.setText("");

        if (radioPredefined.isSelected()
                && serviceCombo.getValue() == null) {
            errorLabel.setText("Pilih layanan dari katalog.");
            return false;
        }

        if (radioCustom.isSelected()
                && customNamaField.getText().trim().isEmpty()) {
            errorLabel.setText("Nama layanan tidak boleh kosong.");
            return false;
        }

        if (biayaField.getText().trim().isEmpty()) {
            errorLabel.setText("Biaya tidak boleh kosong.");
            return false;
        }

        try {
            double biaya = Double.parseDouble(biayaField.getText());
            if (biaya < 0) {
                errorLabel.setText("Biaya tidak boleh negatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Biaya harus berupa angka.");
            return false;
        }

        if (tanggalPicker.getValue() == null) {
            errorLabel.setText("Pilih tanggal tagihan.");
            return false;
        }

        return true;
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