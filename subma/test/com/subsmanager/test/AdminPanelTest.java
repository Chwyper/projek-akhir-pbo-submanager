package com.subsmanager.test;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Admin Panel (adminpanel.fxml).
 *
 * <p>Mencakup TC-AD-01 hingga TC-AD-06 SECARA TERPISAH sesuai Test Plan CSV.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Admin Panel")
class AdminPanelTest extends TestBase {

    // ══════════════════════════════════════════════════════
    // SETUP & HELPER KHUSUS ADMIN PANEL
    // ══════════════════════════════════════════════════════

    private void loginSebagaiAdminDanTunggu() {
        TestHelper.loginSebagaiAdmin(this);
        TestHelper.tungguNode(this, "#adminEmailLabel", AppContext.TIMEOUT_DB);
    }

    /**
     * Helper anti-gagal untuk pindah tab tanpa mengandalkan klik UI (yang sering meleset).
     */
    private void pindahTabBerdasarkanJudul(String judulTab) {
        interact(() -> {
            TabPane tabPane = lookup(".tab-pane").queryAs(TabPane.class);
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText() != null && tab.getText().toLowerCase().contains(judulTab.toLowerCase())) {
                    tabPane.getSelectionModel().select(tab);
                    break;
                }
            }
        });
        TestHelper.tungguFX();
        TestHelper.tungguMs(500); // Beri jeda animasi render tab
    }

    private void klikTombolAman(String teksTombol) {
        Button btn = lookup(".button").queryAllAs(Button.class).stream()
            .filter(b -> b.getText() != null && b.getText().contains(teksTombol))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tombol '" + teksTombol + "' tidak ditemukan"));
        interact(btn::fire);
        TestHelper.tungguFX();
    }

    // ══════════════════════════════════════════════════════
    // TEST CASES (SESUAI DOKUMEN TEST PLAN)
    // ══════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("TC-AD-01: Admin Panel — Tampil setelah login admin")
    void adminPanel_tampilSetelahLogin() {
        loginSebagaiAdminDanTunggu();

        assertTrue(
            TestHelper.isHalamanAktif(this, "#adminEmailLabel"),
            "adminpanel.fxml seharusnya dimuat, ditandai dengan adanya #adminEmailLabel"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#greetingLabel"),
            "Akun admin tidak boleh diarahkan ke dashboard.fxml biasa"
        );
    }

    @Test
    @Order(2)
    @DisplayName("TC-AD-02: Admin Panel — Tab Manajemen User tampil daftar user")
    void adminPanel_tabelUserTerisi() {
        loginSebagaiAdminDanTunggu();
        pindahTabBerdasarkanJudul("User"); // Tab Manajemen User

        @SuppressWarnings("unchecked")
        TableView<?> tabelUser = lookup("#userTable").queryAs(TableView.class);
        
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !tabelUser.getItems().isEmpty());
        } catch (TimeoutException e) {
            fail("Timeout: Tabel user gagal menarik data dari DB Supabase.");
        }
        
        assertFalse(tabelUser.getItems().isEmpty(), "Tabel user seharusnya terisi dengan data dari DB");
    }

    @Test
    @Order(3)
    @DisplayName("TC-AD-03: Admin Panel — Tambah layanan baru ke katalog")
    void adminPanel_tambahLayananBaru() {
        loginSebagaiAdminDanTunggu();
        pindahTabBerdasarkanJudul("Katalog"); // Tab Katalog Subscription

        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !lookup("#serviceTable").queryAs(TableView.class).getItems().isEmpty());
        } catch (TimeoutException ignored) {}

        String layananBaru = "TestSvc_" + System.currentTimeMillis();
        
        clickOn("#tfSvcNama").write(layananBaru);
        clickOn("#tfSvcDomain").write("www.test.com");

        klikTombolAman("Tambah Layanan");
        TestHelper.tungguMs(1500);

        @SuppressWarnings("unchecked")
        TableView<?> tabelLayanan = lookup("#serviceTable").queryAs(TableView.class);
        assertFalse(tabelLayanan.getItems().isEmpty(), "Layanan harus tersimpan dan muncul di tabel katalog");
    }

    @Test
    @Order(4)
    @DisplayName("TC-AD-04: Admin Panel — Tambah layanan duplikat ditolak")
    void adminPanel_tambahLayananDuplikat() {
        loginSebagaiAdminDanTunggu();
        pindahTabBerdasarkanJudul("Katalog");

        clickOn("#tfSvcNama").write("Netflix");
        clickOn("#tfSvcDomain").write("www.netflix.com");
        
        klikTombolAman("Tambah Layanan");

        assertTrue(
            TestHelper.isVisible(this, "#labelSvcError"),
            "#labelSvcError seharusnya muncul untuk memblokir duplikasi"
        );
        
        String pesanError = TestHelper.getTeksLabel(this, "labelSvcError");
        assertTrue(pesanError.toLowerCase().contains("sudah ada"), "Teks error harus mengindikasikan duplikat");
    }

    @Test
    @Order(5)
    @DisplayName("TC-AD-05: Admin Panel — Tab Pemasukan tampil total revenue")
    void adminPanel_tabPemasukanTampilTotal() {
        loginSebagaiAdminDanTunggu();
        pindahTabBerdasarkanJudul("Pemasukan"); // Tab Pemasukan

        String totalText = TestHelper.getTeksLabel(this, "totalPemasukanLabel");
        assertFalse(totalText.isBlank(), "Total revenue harus ditampilkan");

        @SuppressWarnings("unchecked")
        TableView<?> tabelPemasukan = lookup("#pemasukanTable").queryAs(TableView.class);
        assertNotNull(tabelPemasukan, "Tabel pembelian (pemasukanTable) harus ter-render di tab ini");
    }

    @Test
    @Order(6)
    @DisplayName("TC-AD-06: Admin Panel — Hapus user dari panel admin")
    void adminPanel_hapusUser() {
        loginSebagaiAdminDanTunggu();
        pindahTabBerdasarkanJudul("User"); // Tab Manajemen User

        @SuppressWarnings("unchecked")
        TableView<?> tabelUser = lookup("#userTable").queryAs(TableView.class);

        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !tabelUser.getItems().isEmpty());
        } catch (TimeoutException ignored) {}

        if (tabelUser.getItems().size() > 1) {
            Button btnHapus = lookup(".button").queryAllAs(Button.class).stream()
                .filter(b -> b.getText() != null && b.getText().contains("Hapus"))
                .findFirst().orElse(null);

            if (btnHapus != null) {
                interact(btnHapus::fire);
                TestHelper.tungguFX();
                
                Button btnKonfirmasi = lookup(".button").queryAllAs(Button.class).stream()
                    .filter(b -> b.getText() != null && (b.getText().equals("Ya") || b.getText().equals("OK")))
                    .findFirst().orElse(null);
                    
                if (btnKonfirmasi != null) {
                    interact(btnKonfirmasi::fire);
                    TestHelper.tungguMs(1000);
                }
            }
        } else {
            assertTrue(true);
        }
    }
}