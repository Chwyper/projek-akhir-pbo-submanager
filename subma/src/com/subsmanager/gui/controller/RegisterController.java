package com.subsmanager.gui.controller;

import com.subsmanager.SessionManager;
import com.subsmanager.auth.User;
import com.subsmanager.db.UserDAO;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * RegisterController - Controller untuk register.fxml
 *
 * Dependency:
 * - RegisterController ..> UserDAO      (simpan akun baru ke DB)
 * - RegisterController ..> SessionManager (navigasi setelah daftar)
 */
public class RegisterController implements Initializable {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar   passwordStrengthBar;
    @FXML private Label         passwordStrengthLabel;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Listener: update indikator kekuatan password secara live
        passwordField.textProperty().addListener(
            (obs, oldVal, newVal) -> updatePasswordStrength(newVal));
    }

    /**
     * Update ProgressBar kekuatan password secara live.
     * Kriteria: panjang, angka, huruf besar, karakter spesial.
     *
     * @param password password yang sedang diketik
     */
    private void updatePasswordStrength(String password) {
        int score = 0;

        if (password.length() >= 6)                    score++;
        if (password.length() >= 10)                   score++;
        if (password.matches(".*[0-9].*"))             score++;
        if (password.matches(".*[A-Z].*"))             score++;
        if (password.matches(".*[!@#$%^&*()_+].*"))   score++;

        passwordStrengthBar.setProgress(score / 5.0);

        String styleBase = "-fx-accent: %s;";

        if (score <= 1) {
            passwordStrengthLabel.setText("Lemah");
            passwordStrengthLabel.setStyle(
                "-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            passwordStrengthBar.setStyle(
                String.format(styleBase, "#e74c3c"));
        } else if (score <= 3) {
            passwordStrengthLabel.setText("Sedang");
            passwordStrengthLabel.setStyle(
                "-fx-text-fill: #f39c12; -fx-font-size: 11px;");
            passwordStrengthBar.setStyle(
                String.format(styleBase, "#f39c12"));
        } else {
            passwordStrengthLabel.setText("Kuat");
            passwordStrengthLabel.setStyle(
                "-fx-text-fill: #27ae60; -fx-font-size: 11px;");
            passwordStrengthBar.setStyle(
                String.format(styleBase, "#27ae60"));
        }
    }

    /**
     * Dipanggil saat tombol Daftar diklik.
     * Register ke DB di background thread agar UI tidak freeze.
     */
    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();

        // Validasi input di UI thread sebelum ke DB
        if (!validasiInput(email, password, confirm)) return;

        // Nonaktifkan form saat proses
        setFormDisabled(true);

        // Eksekusi register di background thread
        new Thread(() -> {
            User newUser = UserDAO.register(email, password);

            Platform.runLater(() -> {
                setFormDisabled(false);

                if (newUser == null) {
                    // Email sudah terdaftar atau error DB
                    errorLabel.setText(
                        "Email sudah terdaftar atau terjadi kesalahan." +
                        " Coba email lain.");
                    return;
                }

                System.out.println(
                    "[RegisterController] Akun berhasil dibuat: "
                    + email + " (id=" + newUser.getId() + ")");

                successLabel.setText(
                    "Akun berhasil dibuat! Mengarahkan ke halaman login...");

                // Delay singkat lalu navigasi ke login
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() ->
                        SessionManager.navigateTo(
                            "/com/subsmanager/gui/fxml/login.fxml")
                    );
                }).start();
            });

        }).start();
    }

    /**
     * Validasi semua field register sebelum dikirim ke DB.
     *
     * @param email    email yang diinput
     * @param password password yang diinput
     * @param confirm  konfirmasi password
     * @return true jika semua valid
     */
    private boolean validasiInput(String email,
                                   String password,
                                   String confirm) {
        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Semua field harus diisi.");
            return false;
        }

        if (!email.contains("@") || !email.contains(".")) {
            errorLabel.setText("Format email tidak valid.");
            return false;
        }

        if (password.length() < 6) {
            errorLabel.setText("Password minimal 6 karakter.");
            return false;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Password dan konfirmasi tidak cocok.");
            return false;
        }

        return true;
    }

    /**
     * Kembali ke halaman login.
     */
    @FXML
    private void handleBackToLogin() {
        SessionManager.navigateTo(
            "/com/subsmanager/gui/fxml/login.fxml");
    }

    /**
     * Enable/disable semua field form.
     *
     * @param disabled true = nonaktifkan form
     */
    private void setFormDisabled(boolean disabled) {
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
        confirmPasswordField.setDisable(disabled);
    }
}