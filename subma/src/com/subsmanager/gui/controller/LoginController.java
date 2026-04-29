package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.db.SubscriptionDAO;
import com.subsmanager.db.UserDAO;
import com.subsmanager.subscription.model.Subscription;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * LoginController - Controller untuk login.fxml
 *
 * Dependency:
 * - LoginController ..> UserDAO         (autentikasi ke DB)
 * - LoginController ..> SubscriptionDAO (load langganan setelah login)
 * - LoginController ..> SessionManager  (set user + navigasi)
 */
public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Label         loadingLabel;

    /**
     * Dipanggil saat tombol Login diklik.
     * Autentikasi ke DB di background thread agar UI tidak freeze.
     */
    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Validasi field kosong
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email dan password tidak boleh kosong.");
            return;
        }

        // Nonaktifkan form saat proses login
        setFormDisabled(true);
        showLoading("Sedang masuk...");

        // Query DB di background thread
        new Thread(() -> {
            User user = UserDAO.login(email, password);

            if (user == null) {
                // Login gagal
                Platform.runLater(() -> {
                    setFormDisabled(false);
                    hideLoading();
                    showError("Email atau password salah.");
                });
                return;
            }

            // Login berhasil — load langganan user
            List<Subscription> subscriptions =
                SubscriptionDAO.loadByUser(user);
            subscriptions.forEach(user::addSubscription);

            System.out.println("[LoginController] Login berhasil: "
                + email + ", " + subscriptions.size()
                + " langganan dimuat.");

            // Set session lalu navigasi ke dashboard
            Platform.runLater(() -> {
                SessionManager.setCurrentUser(user);
                SessionManager.navigateTo(
                    "/com/subsmanager/gui/fxml/dashboard.fxml");
            });

        }).start();
    }

    /**
     * Dipanggil saat tombol Daftar diklik — pindah ke halaman register.
     */
    @FXML
    private void handleRegister() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/register.fxml");
    }

    // ── Helper UI ────────────────────────────────────────

    /** Tampilkan pesan error di bawah form. */
    private void showError(String message) {
        errorLabel.setText(message);
    }

    /** Tampilkan teks loading. */
    private void showLoading(String message) {
        if (loadingLabel != null) {
            loadingLabel.setText(message);
            loadingLabel.setVisible(true);
            loadingLabel.setManaged(true);
        }
    }

    /** Sembunyikan teks loading. */
    private void hideLoading() {
        if (loadingLabel != null) {
            loadingLabel.setVisible(false);
            loadingLabel.setManaged(false);
        }
    }

    /**
     * Enable/disable field dan tombol saat proses login.
     *
     * @param disabled true = nonaktifkan form
     */
    private void setFormDisabled(boolean disabled) {
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
    }
}