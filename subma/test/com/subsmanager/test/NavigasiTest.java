package com.subsmanager.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk verifikasi navigasi sidebar (NavigasiTest).
 *
 * <p>Mencakup TC-NV-01 hingga TC-NV-05 sesuai Test Plan.
 * Memastikan setiap tombol di sidebar membawa user ke halaman yang tepat.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Navigasi Sidebar")
class NavigasiTest extends TestBase {

    private void loginDanTungguDashboard() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);
    }

    @Test
    @Order(1)
    @DisplayName("TC-NV-01: Navigasi ke Dashboard")
    void navigasi_keDashboard() {
        loginDanTungguDashboard();
        TestHelper.klikSidebar(this, "🏠  Dashboard");
        
        assertTrue(TestHelper.isHalamanAktif(this, "#greetingLabel"), "Gagal navigasi ke Dashboard");
    }

    @Test
    @Order(2)
    @DisplayName("TC-NV-02: Navigasi ke Langganan Saya")
    void navigasi_keLangganan() {
        loginDanTungguDashboard();
        TestHelper.klikSidebar(this, "\uD83D\uDCCB  Langganan Saya"); // 📋
        
        // Cek halaman langganan aktif via searchField
        TestHelper.tungguNode(this, "#searchField", AppContext.TIMEOUT_DEFAULT);
        assertTrue(TestHelper.isHalamanAktif(this, "#searchField"), "Gagal navigasi ke Langganan Saya");
    }

    @Test
    @Order(3)
    @DisplayName("TC-NV-03: Navigasi ke Keuangan")
    void navigasi_keKeuangan() {
        loginDanTungguDashboard();
        TestHelper.klikSidebar(this, "\uD83D\uDCB0  Keuangan"); // 💰
        
        TestHelper.tungguNode(this, "#monthlyTotalLabel", AppContext.TIMEOUT_DEFAULT);
        assertTrue(TestHelper.isHalamanAktif(this, "#monthlyTotalLabel"), "Gagal navigasi ke Keuangan");
    }

    @Test
    @Order(4)
    @DisplayName("TC-NV-04: Navigasi ke Toko Koin")
    void navigasi_keTokoKoin() {
        loginDanTungguDashboard();
        TestHelper.klikSidebar(this, "\uD83E\uDE99  Toko Koin"); // 🪙
        
        TestHelper.tungguNode(this, "#coinBalanceLabel", AppContext.TIMEOUT_DEFAULT);
        assertTrue(TestHelper.isHalamanAktif(this, "#coinBalanceLabel"), "Gagal navigasi ke Toko Koin");
    }

    @Test
    @Order(5)
    @DisplayName("TC-NV-05: Navigasi ke Riwayat Koin")
    void navigasi_keRiwayatKoin() {
        loginDanTungguDashboard();
        TestHelper.klikSidebar(this, "\uD83D\uDCDC  Riwayat Koin"); // 📜
        
        TestHelper.tungguNode(this, "#historyTable", AppContext.TIMEOUT_DEFAULT);
        assertTrue(TestHelper.isHalamanAktif(this, "#historyTable"), "Gagal navigasi ke Riwayat Koin");
    }

    @Test
    @Order(6)
    @DisplayName("TC-NV-06: Navigasi — Logout dari sidebar kembali ke login")
    void navigasi_logoutKembaliKeLogin() {
        loginDanTungguDashboard();
        
        // Klik menggunakan substring karena mungkin ada emoji
        javafx.scene.control.Button btnLogout = lookup(".button").queryAllAs(javafx.scene.control.Button.class)
            .stream().filter(b -> b.getText() != null && b.getText().contains("Logout"))
            .findFirst().orElse(null);
            
        if (btnLogout != null) {
            interact(btnLogout::fire);
            TestHelper.tungguNode(this, "#emailField", AppContext.TIMEOUT_DEFAULT);
            assertTrue(TestHelper.isHalamanAktif(this, "#emailField"), "Gagal navigasi ke Login setelah logout");
        } else {
            fail("Tombol Logout tidak ditemukan di sidebar");
        }
    }
}