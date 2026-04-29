package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.coin.CoinPackage;
import com.subsmanager.coin.CoinService;
import com.subsmanager.coin.CoinTransaction;
import com.subsmanager.coin.PaymentMethod;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.io.File;
import java.util.ResourceBundle;

/**
 * CoinStoreController - Controller untuk coinstore.fxml
 *
 * Dependency:
 * - CoinStoreController ..> CoinService (proses pembelian koin)
 * - CoinStoreController ..> CoinPackage (paket yang dipilih)
 * - CoinStoreController ..> PaymentMethod (metode bayar)
 * - CoinStoreController ..> SessionManager (navigasi + ambil user)
 */
public class CoinStoreController implements Initializable {
	/**
	 * Tampilkan FileChooser dan export struk transaksi ke PDF.
	 */
	private void cetakStruk(CoinTransaction transaction,
	                         User user,
	                         int saldoSebelum) {
	    javafx.stage.FileChooser fileChooser =
	        new javafx.stage.FileChooser();
	    fileChooser.setTitle("Simpan Bukti Transaksi");
	    fileChooser.setInitialFileName(
	        "struk_" + transaction.getTransactionCode() + ".pdf");
	    fileChooser.getExtensionFilters().add(
	        new javafx.stage.FileChooser.ExtensionFilter(
	            "PDF Files", "*.pdf"));

	    File file = fileChooser.showSaveDialog(
	        SessionManager.getPrimaryStage());
	    if (file == null) return;

	    new Thread(() -> {
	        try {
	            ExportService exportService =
	                new ExportService(user,
	                    new com.subsmanager.currency
	                        .CurrencyConverter());
	            exportService.exportReceipt(
	                transaction, user,
	                saldoSebelum,
	                file.getAbsolutePath());

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
	                    "Gagal menyimpan struk: "
	                    + e.getMessage())
	                .showAndWait()
	            );
	            e.printStackTrace();
	        }
	    }).start();
	}
    // ===== Sidebar =====
    @FXML private Label userEmailLabel;

    // ===== Saldo =====
    @FXML private Label coinBalanceLabel;

    // ===== Kartu paket =====
    @FXML private VBox cardStarter;
    @FXML private VBox cardRegular;
    @FXML private VBox cardPro;

    // ===== Panel pembayaran =====
    @FXML private VBox            paymentPanel;
    @FXML private Label           selectedPackageLabel;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private VBox            paymentInfoBox;
    @FXML private Label           paymentInfoLabel;
    @FXML private Label           paymentNumberLabel;
    @FXML private Label           statusLabel;
    @FXML private Button          bayarBtn;

    /** Service untuk proses pembelian koin */
    private CoinService coinService;

    /** Paket yang sedang dipilih user */
    private CoinPackage selectedPackage;

    /** Style kartu normal dan kartu terpilih */
    private static final String STYLE_CARD_NORMAL =
        "-fx-background-color: white;" +
        "-fx-background-radius: 14;" +
        "-fx-padding: 24;" +
        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);" +
        "-fx-cursor: hand;";

    private static final String STYLE_CARD_SELECTED =
        "-fx-background-color: white;" +
        "-fx-background-radius: 14;" +
        "-fx-padding: 24;" +
        "-fx-border-color: #4f46e5;" +
        "-fx-border-radius: 14;" +
        "-fx-border-width: 2;" +
        "-fx-effect: dropshadow(gaussian,rgba(79,70,229,0.2),12,0,0,4);" +
        "-fx-cursor: hand;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coinService = new CoinService();
        setupPaymentMethods();
        loadUserData();
    }

    /**
     * Isi ComboBox metode pembayaran dari enum PaymentMethod.
     */
    private void setupPaymentMethods() {
        for (PaymentMethod pm : PaymentMethod.values()) {
            paymentMethodCombo.getItems().add(pm.getLabel());
        }

        // Listener: tampilkan info nomor pembayaran saat metode dipilih
        paymentMethodCombo.valueProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal == null) return;
                showPaymentInfo(newVal);
            });
    }

    /**
     * Tampilkan info nomor rekening / dompet digital
     * sesuai metode yang dipilih.
     *
     * @param label label metode pembayaran yang dipilih
     */
    private void showPaymentInfo(String label) {
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.getLabel().equals(label)) {
                paymentInfoBox.setVisible(true);
                paymentInfoBox.setManaged(true);
                paymentInfoLabel.setText(
                    pm.getType() + " — " + pm.getLabel());
                paymentNumberLabel.setText(
                    pm.getPaymentNumber());
                break;
            }
        }
    }

    /**
     * Load data user ke UI.
     */
    private void loadUserData() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        userEmailLabel.setText(user.getEmail());
        updateSaldoLabel(user);
    }

    /**
     * Update label saldo koin.
     *
     * @param user user yang sedang login
     */
    private void updateSaldoLabel(User user) {
    	coinBalanceLabel.setText(
    	    user.getCoinBalance().getBalance() + " koin");
    }

    // ===== Pilih Paket =====

    @FXML
    private void selectStarter() {
        selectedPackage = coinService.getPackageById(1L);
        highlightCard(cardStarter);
        showPaymentPanel();
    }

    @FXML
    private void selectRegular() {
        selectedPackage = coinService.getPackageById(2L);
        highlightCard(cardRegular);
        showPaymentPanel();
    }

    @FXML
    private void selectPro() {
        selectedPackage = coinService.getPackageById(3L);
        highlightCard(cardPro);
        showPaymentPanel();
    }

    /**
     * Highlight kartu yang dipilih, reset kartu lainnya.
     *
     * @param selected kartu yang dipilih
     */
    private void highlightCard(VBox selected) {
        cardStarter.setStyle(STYLE_CARD_NORMAL);
        cardRegular.setStyle(STYLE_CARD_NORMAL);
        cardPro.setStyle(STYLE_CARD_NORMAL);
        selected.setStyle(STYLE_CARD_SELECTED);
    }

    /**
     * Tampilkan panel pembayaran setelah paket dipilih.
     */
    private void showPaymentPanel() {
        if (selectedPackage == null) return;

        paymentPanel.setVisible(true);
        paymentPanel.setManaged(true);

        selectedPackageLabel.setText(
            selectedPackage.getName()
            + " — " + selectedPackage.getCoinAmount() + " koin"
            + " (Rp " + (int) selectedPackage.getPrice() + ")");

        // Reset pilihan metode bayar
        paymentMethodCombo.setValue(null);
        paymentInfoBox.setVisible(false);
        paymentInfoBox.setManaged(false);
        statusLabel.setText("");
    }

    /**
     * Proses pembayaran saat tombol Bayar diklik.
     * Menggunakan PaymentProcessor dummy dengan Thread.sleep(1500).
     */
    @FXML
    private void handleBayar() {
        if (selectedPackage == null) {
            statusLabel.setText("Pilih paket terlebih dahulu.");
            return;
        }

        String metodeLbl = paymentMethodCombo.getValue();
        if (metodeLbl == null) {
            statusLabel.setText("Pilih metode pembayaran.");
            return;
        }

        // Cari PaymentMethod enum dari label
        PaymentMethod method = null;
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.getLabel().equals(metodeLbl)) {
                method = pm;
                break;
            }
        }
        if (method == null) return;

        final PaymentMethod finalMethod = method;
        final User user = SessionManager.getCurrentUser();
        final int[] saldoSebelum = {
        	    user.getCoinBalance().getBalance()
        	};
        // Disable tombol saat memproses
        bayarBtn.setDisable(true);
        bayarBtn.setText("⏳ Memproses...");
        statusLabel.setText("Sedang memproses pembayaran...");

        // Proses di background thread agar UI tidak freeze
        new Thread(() -> {
            try {
                coinService.purchaseCoin(
                    user,
                    selectedPackage.getId(),
                    finalMethod
                );

                Platform.runLater(() -> {
                    updateSaldoLabel(user);
                    bayarBtn.setDisable(false);
                    bayarBtn.setText("💳 Bayar Sekarang");
                    statusLabel.setStyle(
                        "-fx-text-fill: #27ae60; -fx-font-size: 12px;");
                    statusLabel.setText(
                        "✅ Pembayaran berhasil! +"
                        + selectedPackage.getCoinAmount()
                        + " koin ditambahkan.");

                    // Tanya user apakah mau cetak struk
                    CoinTransaction lastTrx =
                        coinService.getLastTransaction();
                    if (lastTrx != null) {
                        Alert tanyaStruk = new Alert(
                            Alert.AlertType.CONFIRMATION);
                        tanyaStruk.setTitle("Cetak Struk");
                        tanyaStruk.setHeaderText(
                            "Pembelian berhasil!");
                        tanyaStruk.setContentText(
                            "Apakah kamu ingin mencetak bukti transaksi?");

                        tanyaStruk.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                cetakStruk(lastTrx, user, saldoSebelum[0]);
                            }
                        });
                    }

                    // Reset panel setelah dialog
                    new Thread(() -> {
                        try { Thread.sleep(500); }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        Platform.runLater(this::resetPanel);
                    }).start();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    bayarBtn.setDisable(false);
                    bayarBtn.setText("💳 Bayar Sekarang");
                    statusLabel.setStyle(
                        "-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                    statusLabel.setText(
                        "❌ Pembayaran gagal: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Batalkan pemilihan paket & sembunyikan panel pembayaran.
     */
    @FXML
    private void handleBatalkan() {
        resetPanel();
    }

    /**
     * Reset semua pilihan ke kondisi awal.
     */
    private void resetPanel() {
        selectedPackage = null;
        cardStarter.setStyle(STYLE_CARD_NORMAL);
        cardRegular.setStyle(STYLE_CARD_NORMAL);
        cardPro.setStyle(STYLE_CARD_NORMAL);
        paymentPanel.setVisible(false);
        paymentPanel.setManaged(false);
        paymentMethodCombo.setValue(null);
        paymentInfoBox.setVisible(false);
        paymentInfoBox.setManaged(false);
        statusLabel.setText("");
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

    @FXML private void handleLogout() {
        SessionManager.logout();
    }
}