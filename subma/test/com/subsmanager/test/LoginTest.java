package com.subsmanager.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Autentikasi (Login & Register).
 * Mencakup TC-AU-01 hingga TC-AU-08 sesuai Test Plan.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Autentikasi — Login & Register")
class LoginTest extends TestBase {

    // ══════════════════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════════════════

    /** TC-AU-01: Login berhasil — user valid → navigasi ke dashboard. */
    @Test @Order(1)
    @DisplayName("TC-AU-01: Login berhasil — user valid")
    void loginBerhasil_userValid() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#greetingLabel"),
            "Seharusnya navigasi ke dashboard setelah login berhasil"
        );
        String emailTampil = TestHelper.getTeksLabel(this, "userEmailLabel");
        assertTrue(
            emailTampil.contains(AppContext.USER_EMAIL),
            "Email user seharusnya tampil di sidebar, dapat: " + emailTampil
        );
    }

    /**
     * TC-AU-02: Login gagal — password salah.
     * Fix: tunggu TIMEOUT_DB agar DB response sempat update errorLabel.
     */
    @Test @Order(2)
    @DisplayName("TC-AU-02: Login gagal — password salah")
    void loginGagal_passwordSalah() {
        TestHelper.login(this, AppContext.USER_EMAIL, "passwordsalah999");
        // Tunggu DB response — login async butuh waktu lebih dari sekedar waitForFxEvents
        TestHelper.tungguMs(AppContext.TIMEOUT_DB);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#emailField"),
            "Seharusnya tetap di halaman login"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#greetingLabel"),
            "Seharusnya tidak navigasi ke dashboard"
        );
        String error = TestHelper.getTeksLabel(this, "errorLabel");
        assertFalse(
            error.isBlank(),
            "errorLabel seharusnya menampilkan pesan error, dapat: '" + error + "'"
        );
    }

    /**
     * TC-AU-03: Login gagal — email tidak terdaftar.
     * Fix: sama dengan AU-02, tunggu DB response.
     */
    @Test @Order(3)
    @DisplayName("TC-AU-03: Login gagal — email tidak terdaftar")
    void loginGagal_emailTidakTerdaftar() {
        TestHelper.login(this, "tidakada_xyz@email.com", "apapun123");
        TestHelper.tungguMs(AppContext.TIMEOUT_DB);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#emailField"),
            "Seharusnya tetap di halaman login"
        );
        String error = TestHelper.getTeksLabel(this, "errorLabel");
        assertFalse(
            error.isBlank(),
            "errorLabel seharusnya menampilkan pesan error"
        );
    }

    /**
     * TC-AU-04: Login admin → redirect ke adminpanel.
     * Fix: tunggu TIMEOUT_DB untuk DB query + navigasi.
     */
    @Test @Order(4)
    @DisplayName("TC-AU-04: Login admin — redirect ke adminpanel")
    void loginAdmin_redirectKeAdminPanel() {
        TestHelper.loginSebagaiAdmin(this);
        TestHelper.tungguMs(AppContext.TIMEOUT_DB);
        TestHelper.tungguNode(this, "#adminEmailLabel", AppContext.TIMEOUT_DB);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#adminEmailLabel"),
            "Seharusnya navigasi ke adminpanel setelah login admin"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#greetingLabel"),
            "Admin seharusnya tidak masuk ke dashboard user"
        );
    }

    /**
     * TC-AU-05: Login — field kosong → tidak navigasi.
     * Fix: tunggu sebentar agar validasi client-side sempat jalan.
     */
    @Test @Order(5)
    @DisplayName("TC-AU-05: Login — field kosong")
    void loginGagal_fieldKosong() {
        clickOn("Login");
        TestHelper.tungguMs(1000);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#emailField"),
            "Seharusnya tetap di halaman login"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#greetingLabel"),
            "Seharusnya tidak navigasi ke dashboard"
        );
    }

    // ══════════════════════════════════════════════════════
    // REGISTER
    // ══════════════════════════════════════════════════════

    /**
     * TC-AU-06: Register berhasil — data valid.
     * Email unik per run agar tidak bentrok di DB.
     */
    @Test @Order(6)
    @DisplayName("TC-AU-06: Register berhasil — data valid")
    void registerBerhasil_dataValid() {
        clickOn("Belum punya akun? Daftar");
        TestHelper.tungguNode(this, "#confirmPasswordField");

        String emailBaru = "test_" + System.currentTimeMillis() + "@email.com";

        clickOn("#emailField").write(emailBaru);
        clickOn("#passwordField").write("Password123!");
        clickOn("#confirmPasswordField").write("Password123!");
        clickOn("Daftar Sekarang");
        TestHelper.tungguMs(AppContext.TIMEOUT_DB);

        // Setelah register berhasil → navigasi ke login
        assertTrue(
            TestHelper.isHalamanAktif(this, "#emailField"),
            "Seharusnya kembali ke halaman login setelah register berhasil"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#confirmPasswordField"),
            "Seharusnya sudah keluar dari halaman register"
        );
    }

    /**
     * TC-AU-07: Register gagal — email duplikat.
     * Fix: tunggu DB response sebelum cek errorLabel.
     */
    @Test @Order(7)
    @DisplayName("TC-AU-07: Register gagal — email duplikat")
    void registerGagal_emailDuplikat() {
        clickOn("Belum punya akun? Daftar");
        TestHelper.tungguNode(this, "#confirmPasswordField");

        // Gunakan email yang sudah pasti ada di DB
        clickOn("#emailField").write(AppContext.USER_EMAIL);
        clickOn("#passwordField").write("Password123!");
        clickOn("#confirmPasswordField").write("Password123!");
        clickOn("Daftar Sekarang");
        TestHelper.tungguMs(AppContext.TIMEOUT_DB);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#confirmPasswordField"),
            "Seharusnya tetap di halaman register"
        );
        String error = TestHelper.getTeksLabel(this, "errorLabel");
        assertFalse(
            error.isBlank(),
            "errorLabel seharusnya menampilkan pesan error duplikat, dapat: '" + error + "'"
        );
    }

    /**
     * TC-AU-08: Password strength bar berubah sesuai kekuatan password.
     *
     * Fix: clear field sebelum tiap write agar progress bisa dibandingkan
     * dari nol, bukan terakumulasi. Gunakan eraseText() untuk reset.
     * Kriteria scoring:
     *   >= 6 char    → +1
     *   >= 10 char   → +1
     *   ada angka    → +1
     *   ada huruf besar → +1
     *   ada simbol   → +1
     * Total max = 5, progress = score/5.0
     */
    @Test @Order(8)
    @DisplayName("TC-AU-08: Register — password strength bar berubah")
    void register_passwordStrengthBarBerubah() {
        clickOn("Belum punya akun? Daftar");
        TestHelper.tungguNode(this, "#passwordStrengthBar");

        // Ambil progress awal (kosong = 0.0)
        double progressAwal = lookup("#passwordStrengthBar")
            .queryAs(javafx.scene.control.ProgressBar.class)
            .getProgress();

        // Ketik password pendek — hanya 3 char, score = 0
        clickOn("#passwordField").write("abc");
        TestHelper.tungguFX();
        double progressPendek = lookup("#passwordStrengthBar")
            .queryAs(javafx.scene.control.ProgressBar.class)
            .getProgress();

        // Clear field lalu ketik password kuat — score = 5 (>=6, >=10, angka, besar, simbol)
        clickOn("#passwordField");
        eraseText(3); // hapus "abc"
        write("StrongPass123!@#");
        TestHelper.tungguFX();
        double progressKuat = lookup("#passwordStrengthBar")
            .queryAs(javafx.scene.control.ProgressBar.class)
            .getProgress();

        // Assert: password kuat harus dapat score lebih tinggi dari pendek
        assertTrue(
            progressKuat > progressPendek,
            String.format(
                "Strength bar password kuat (%.2f) seharusnya > pendek (%.2f)",
                progressKuat, progressPendek
            )
        );

        // Assert: password kuat harus dapat score lebih tinggi dari awal
        assertTrue(
            progressKuat > progressAwal,
            String.format(
                "Strength bar password kuat (%.2f) seharusnya > awal (%.2f)",
                progressKuat, progressAwal
            )
        );
    }

    /**
     * TC-AU-09: Link "Sudah punya akun?" dari register ke login berfungsi.
     */
    @Test @Order(9)
    @DisplayName("TC-AU-09: Link \"Sudah punya akun?\" dari register ke login")
    void register_linkSudahPunyaAkunKeLogin() {
        clickOn("Belum punya akun? Daftar");
        TestHelper.tungguNode(this, "#confirmPasswordField");
        
        clickOn("Sudah punya akun? Login");
        TestHelper.tungguNode(this, "#emailField");
        
        assertTrue(
            TestHelper.isHalamanAktif(this, "#emailField"),
            "Seharusnya kembali ke halaman login"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#confirmPasswordField"),
            "Seharusnya form register tertutup"
        );
    }
}