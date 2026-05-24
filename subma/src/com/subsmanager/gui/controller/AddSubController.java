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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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

    @FXML private TextField         serviceSearchField;
    @FXML private ListView<String>   serviceListView;
    @FXML private HBox               selectedServiceBox;
    @FXML private Label              selectedServiceLabel;
    @FXML private TextField         customNamaField;
    @FXML private ComboBox<String>  tierCombo;
    @FXML private ComboBox<String>  currencyCombo;
    @FXML private TextField         biayaField;
    @FXML private ComboBox<String>  siklusCombo;
    @FXML private DatePicker        tanggalPicker;
    @FXML private TextField         customUrlField;
    @FXML private Label             errorLabel;
    @FXML private Label             katalogWarningLabel;

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
    /** Jumlah maksimum saran yang ditampilkan di ListView. */
    private static final int MAX_SUGGESTIONS = 6;

    /** Layanan yang sedang dipilih user dari hasil pencarian. */
    private Service selectedService = null;

    /**
     * Load katalog layanan dari DB di background thread.
     * Tidak mengisi ComboBox lagi — data tersimpan di katalogList
     * dan difilter secara live saat user mengetik.
     */
    private void setupKatalog() {
        new Thread(() -> {
            List<Service> services = ServiceDAO.loadAllServices();

            javafx.application.Platform.runLater(() -> {
                katalogList.clear();
                for (Service s : services) {
                    List<com.subsmanager.catalog.ServiceTier> tiers =
                        ServiceDAO.loadTiersByService(s.getId());
                    tiers.forEach(s::addTier);
                    katalogList.add(s);
                }
                System.out.println("[AddSubController] Katalog dimuat: "
                    + katalogList.size() + " layanan.");
            });
        }).start();
    }

    /**
     * Filter katalog berdasarkan keyword dan tampilkan di ListView.
     * Sembunyikan ListView jika keyword kosong atau layanan sudah dipilih.
     *
     * @param keyword teks yang diketik user
     */
    private void tampilkanSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            serviceListView.setVisible(false);
            serviceListView.setManaged(false);
            return;
        }

        String kw = keyword.trim().toLowerCase();
        List<String> hasil = katalogList.stream()
            .filter(s -> s.getName().toLowerCase().contains(kw))
            .limit(MAX_SUGGESTIONS)
            .map(Service::getName)
            .collect(java.util.stream.Collectors.toList());

        if (hasil.isEmpty()) {
            serviceListView.setVisible(false);
            serviceListView.setManaged(false);
        } else {
            serviceListView.getItems().setAll(hasil);
            serviceListView.setVisible(true);
            serviceListView.setManaged(true);
        }
    }

    /**
     * Dipanggil saat user mengklik salah satu item di ListView saran.
     * Simpan layanan yang dipilih, tampilkan chip, sembunyikan ListView.
     *
     * @param nama nama layanan yang dipilih
     */
    private void handleServicePicked(String nama) {
        if (nama == null) return;

        selectedService = katalogList.stream()
            .filter(s -> s.getName().equals(nama))
            .findFirst().orElse(null);

        if (selectedService != null) {
            // Tampilkan chip
            selectedServiceLabel.setText(selectedService.getName());
            selectedServiceBox.setVisible(true);
            selectedServiceBox.setManaged(true);

            // Sembunyikan search field dan ListView
            serviceSearchField.setText("");
            serviceListView.setVisible(false);
            serviceListView.setManaged(false);
            serviceSearchField.setVisible(false);
            serviceSearchField.setManaged(false);

            // Isi tierCombo dari tiers layanan
            tierCombo.getItems().clear();
            for (ServiceTier t : selectedService.getAvailableTiers()) {
                tierCombo.getItems().add(t.getTierName());
            }
            if (!tierCombo.getItems().isEmpty()) {
                tierCombo.setValue(tierCombo.getItems().get(0));
            }
            currencyCombo.setValue(selectedService.getDefaultCurrency());
            updatePreview();
        }
    }

    /**
     * Hapus layanan yang dipilih dan kembalikan tampilan ke search field.
     * Dipanggil saat user menekan tombol 'x' pada chip.
     */
    @FXML
    private void handleClearService() {
        selectedService = null;
        selectedServiceLabel.setText("");
        selectedServiceBox.setVisible(false);
        selectedServiceBox.setManaged(false);
        serviceSearchField.setVisible(true);
        serviceSearchField.setManaged(true);
        serviceSearchField.setText("");
        tierCombo.getItems().clear();
        updatePreview();
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
        // Listener search field — filter saran saat mengetik
        serviceSearchField.textProperty().addListener(
            (o, ov, nv) -> tampilkanSuggestions(nv));

        // Listener ListView — pilih layanan saat klik item
        serviceListView.setOnMouseClicked(event -> {
            String dipilih = serviceListView.getSelectionModel()
                .getSelectedItem();
            if (dipilih != null) handleServicePicked(dipilih);
        });

        customNamaField.textProperty().addListener(
            (o, ov, nv) -> {
                updatePreview();
                cekNamaCustomVsKatalog(nv);
            });
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
        selectedService = null;
        selectedServiceLabel.setText("");
        selectedServiceBox.setVisible(false);
        selectedServiceBox.setManaged(false);
        serviceSearchField.setText("");
        serviceSearchField.setVisible(true);
        serviceSearchField.setManaged(true);
        serviceListView.setVisible(false);
        serviceListView.setManaged(false);
        tierCombo.getItems().clear();
        errorLabel.setText("");
    }

    /**
     * Cek apakah nama custom yang diketik user cocok dengan
     * layanan yang sudah ada di katalog predefined.
     * Tampilkan warning jika cocok, sembunyikan jika tidak.
     *
     * @param input teks yang sedang diketik di customNamaField
     */
    private void cekNamaCustomVsKatalog(String input) {
        if (katalogWarningLabel == null) return;

        if (input == null || input.trim().isEmpty()) {
            katalogWarningLabel.setVisible(false);
            katalogWarningLabel.setManaged(false);
            return;
        }

        String keyword = input.trim().toLowerCase();

        // Cari layanan di katalog yang namanya mengandung keyword
        Service cocok = katalogList.stream()
            .filter(s -> s.getName().toLowerCase().contains(keyword)
                || keyword.contains(s.getName().toLowerCase()))
            .findFirst().orElse(null);

        if (cocok != null) {
            // FIXED: Escaped the double quotes correctly using \"
            katalogWarningLabel.setText(
                "\"" + cocok.getName() + "\" tersedia di katalog. "
                + "Gunakan mode Dari Katalog untuk data lebih lengkap.");
            katalogWarningLabel.setVisible(true);
            katalogWarningLabel.setManaged(true);
        } else {
            katalogWarningLabel.setVisible(false);
            katalogWarningLabel.setManaged(false);
        }
    }

    /**
     * Update panel preview secara live.
     */
    private void updatePreview() {
        boolean isPredefined = radioPredefined.isSelected();

        String nama = isPredefined
            ? (selectedService != null
                ? selectedService.getName() : "Nama Layanan")
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
            // selectedService sudah diset via handleServicePicked()
            Service pickedService = selectedService;

            ServiceTier pickedTier = pickedService
                .getAvailableTiers().stream()
                .filter(t -> t.getTierName()
                    .equals(tierCombo.getValue()))
                .findFirst().orElse(null);

            PredefinedSubscription ps = new PredefinedSubscription();
            ps.setId(System.currentTimeMillis());
            ps.setServiceName(pickedService.getName());
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
            ps.setService(pickedService);
            ps.setSelectedTier(pickedTier);
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
                && selectedService == null) {
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