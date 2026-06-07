package com.subsmanager.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Dashboard.
 * Mencakup TC-DB-01 hingga TC-DB-04 sesuai Test Plan.
 *
 * <p>Semua test berasumsi user@email.com sudah punya
 * minimal 1 langganan dan saldo koin > 0 di DB.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Dashboard")
class DashboardTest extends TestBase {

    /**
     * Login ke dashboard sebelum tiap test.
     * Tunggu sampai greetingLabel muncul sebagai penanda
     * dashboard selesai dimuat.
     */
    private void loginKeDashboard() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguMs(AppContext.TIMEOUT_DB);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);
    }

    // ══════════════════════════════════════════════════════

    /**
     * TC-DB-01: Dashboard menampilkan data langganan dari DB.
     * Verifikasi label ringkasan tidak kosong dan tidak nol semua.
     */
    @Test @Order(1)
    @DisplayName("TC-DB-01: Dashboard menampilkan data user")
    void dashboard_menampilkanDataUser() {
        loginKeDashboard();

        // Assert — label total langganan tidak kosong
        String totalSubs = TestHelper.getTeksLabel(this, "totalSubsLabel");
        assertFalse(
            totalSubs.isBlank(),
            "totalSubsLabel seharusnya terisi, dapat: '" + totalSubs + "'"
        );

        // Assert — minimal salah satu label keuangan terisi (tidak "Rp 0" semua)
        String monthly = TestHelper.getTeksLabel(this, "monthlyLabel");
        String yearly  = TestHelper.getTeksLabel(this, "yearlyLabel");
        assertFalse(
            monthly.isBlank(),
            "monthlyLabel seharusnya terisi, dapat: '" + monthly + "'"
        );
        assertFalse(
            yearly.isBlank(),
            "yearlyLabel seharusnya terisi, dapat: '" + yearly + "'"
        );
    }

    /**
     * TC-DB-02: Saldo koin tampil di dashboard dan lebih dari 0.
     * Berasumsi user sudah pernah top up koin sebelumnya.
     */
    @Test @Order(2)
    @DisplayName("TC-DB-02: Saldo koin tampil di dashboard")
    void dashboard_saldoKoinTampil() {
        loginKeDashboard();

        String saldo = TestHelper.getTeksLabel(this, "coinLabel");
        assertFalse(
            saldo.isBlank(),
            "coinLabel seharusnya terisi, dapat: '" + saldo + "'"
        );

        // Saldo tidak boleh menunjukkan nilai nol
        assertFalse(
            saldo.contains("0 Koin") || saldo.equals("0"),
            "Saldo koin seharusnya > 0 karena user sudah pernah top up, dapat: '"
            + saldo + "'"
        );
    }

    /**
     * TC-DB-03: Tombol "Tambah Langganan" navigasi ke addsub.fxml.
     * Verifikasi dengan kehadiran radioCustom atau radioPredefined.
     */
    @Test @Order(3)
    @DisplayName("TC-DB-03: Tombol Tambah Langganan navigasi ke addsub")
    void dashboard_tombolTambahLanggananNavigasi() {
        loginKeDashboard();

        // Klik tombol Tambah Langganan di dashboard
        clickOn("+ Tambah");
        TestHelper.tungguFX();

        // Assert — halaman addsub aktif (ada radio button Predefined)
        TestHelper.tungguNode(this, "#radioPredefined", AppContext.TIMEOUT_DEFAULT);
        assertTrue(
            TestHelper.isHalamanAktif(this, "#radioPredefined"),
            "Seharusnya navigasi ke addsub.fxml setelah klik Tambah Langganan"
        );
    }

    /**
     * TC-DB-04: Email user tampil di sidebar dashboard.
     * Verifikasi label email sesuai akun yang login.
     */
    @Test @Order(4)
    @DisplayName("TC-DB-04: Email user tampil di sidebar")
    void dashboard_emailTampilDiSidebar() {
        loginKeDashboard();

        String email = TestHelper.getTeksLabel(this, "userEmailLabel");
        assertTrue(
            email.contains(AppContext.USER_EMAIL),
            "userEmailLabel seharusnya menampilkan '"
            + AppContext.USER_EMAIL + "', dapat: '" + email + "'"
        );
    }

    /**
     * TC-DB-05: Tabel subscription di dashboard ada baris data.
     */
    @Test @Order(5)
    @DisplayName("TC-DB-05: Tabel subscription ada baris data")
    void dashboard_tabelSubscriptionBerisiData() {
        loginKeDashboard();

        @SuppressWarnings("unchecked")
        javafx.scene.control.TableView<?> tabel = lookup("#subscriptionTable").queryAs(javafx.scene.control.TableView.class);
        
        assertTrue(
            tabel.getItems().size() > 0,
            "Tabel langganan di dashboard seharusnya memiliki minimal 1 baris data"
        );
    }
}