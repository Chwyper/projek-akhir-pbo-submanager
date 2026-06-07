package com.subsmanager.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Tambah Langganan (addsub.fxml).
 *
 * <p>Mencakup TC-AS-01 hingga TC-AS-06 sesuai Test Plan.
 * Navigasi ke halaman addsub dilakukan via halaman Langganan Saya.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Tambah Langganan")
class AddSubTest extends TestBase {

    // ══════════════════════════════════════════════════════
    // SETUP & HELPER KHUSUS
    // ══════════════════════════════════════════════════════

    private void loginDanBukaAddsub() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);
        
        TestHelper.klikSidebar(this, "\uD83D\uDCCB  Langganan Saya"); // 📋  Langganan Saya
        TestHelper.tungguNode(this, "#subscriptionTable", AppContext.TIMEOUT_DB);
        
        clickOn("+ Tambah Langganan");
        TestHelper.tungguNode(this, "#radioPredefined", AppContext.TIMEOUT_DEFAULT);
        TestHelper.tungguMs(AppContext.TIMEOUT_DB); // Tunggu katalog load dari DB
    }

    /**
     * Helper khusus untuk klik tombol simpan.
     * Menggunakan interact() untuk mem-bypass ScrollPane BoundsLocatorException
     * apabila tombol terdorong ke luar viewport layar (off-screen).
     */
    private void klikTombolSimpan() {
        javafx.scene.control.Button btn = lookup(".button")
            .queryAllAs(javafx.scene.control.Button.class)
            .stream()
            .filter(b -> b.getText() != null && b.getText().contains("Simpan Langganan"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tombol Simpan tidak ditemukan"));
        
        // Eksekusi aksi klik secara programmatis ke thread JavaFX
        interact(btn::fire);
    }

    // ══════════════════════════════════════════════════════
    // TEST CASES
    // ══════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("TC-AS-01: Tambah Langganan — halaman dan form tampil")
    void addsub_halamanDanFormTampil() {
        loginDanBukaAddsub();

        assertTrue(TestHelper.isHalamanAktif(this, "#radioPredefined"), "radioPredefined harus ada");
        assertTrue(TestHelper.isHalamanAktif(this, "#radioCustom"), "radioCustom harus ada");
        assertTrue(TestHelper.isHalamanAktif(this, "#serviceSearchField"), "serviceSearchField harus ada");
        assertTrue(TestHelper.isHalamanAktif(this, "#tierCombo"), "tierCombo harus ada");
        assertTrue(TestHelper.isHalamanAktif(this, "#biayaField"), "biayaField harus ada");
        assertTrue(TestHelper.isHalamanAktif(this, "#siklusCombo"), "siklusCombo harus ada");
        assertTrue(TestHelper.isHalamanAktif(this, "#tanggalPicker"), "tanggalPicker harus ada");
    }

    @Test
    @Order(2)
    @DisplayName("TC-AS-02: Tambah Langganan — search katalog menampilkan saran")
    void addsub_searchKatalogTampilkanSaran() {
        loginDanBukaAddsub();

        clickOn("#serviceSearchField").write("Netfl");
        TestHelper.tungguMs(500); 

        assertTrue(
            TestHelper.isVisible(this, "#serviceListView"),
            "serviceListView seharusnya muncul saat mengetik nama layanan"
        );

        clickOn("Netflix");
        TestHelper.tungguFX();

        assertTrue(
            TestHelper.isVisible(this, "#selectedServiceBox"),
            "selectedServiceBox seharusnya muncul setelah layanan dipilih"
        );

        String namaLayanan = TestHelper.getTeksLabel(this, "selectedServiceLabel");
        assertEquals("Netflix", namaLayanan, "Label chip tidak sesuai");

        @SuppressWarnings("unchecked")
        javafx.scene.control.ComboBox<String> tierCombo =
            lookup("#tierCombo").queryAs(javafx.scene.control.ComboBox.class);
        assertFalse(
            tierCombo.getItems().isEmpty(),
            "tierCombo seharusnya terisi setelah layanan dipilih"
        );
    }

    @Test
    @Order(3)
    @DisplayName("TC-AS-03: Tambah Langganan — mode Custom menampilkan field nama custom")
    void addsub_modeCustomTampilkanNamaField() {
        loginDanBukaAddsub();

        clickOn("#radioCustom");
        TestHelper.tungguFX();

        assertTrue(
            TestHelper.isVisible(this, "#customNamaField"),
            "customNamaField seharusnya muncul"
        );

        assertFalse(
            TestHelper.isVisible(this, "#katalogBox"),
            "katalogBox seharusnya disembunyikan saat mode Custom"
        );
    }

    @Test
    @Order(4)
    @DisplayName("TC-AS-04: Tambah Langganan — Simpan kosong memunculkan errorLabel")
    void addsub_simpanKosongTampilkanError() {
        loginDanBukaAddsub();

        // Klik langsung menggunakan helper tanpa mengisi form apapun
        klikTombolSimpan();
        TestHelper.tungguFX();

        assertTrue(
            TestHelper.isHalamanAktif(this, "#radioPredefined"),
            "Seharusnya tetap di halaman addsub karena validasi error"
        );

        String teksError = TestHelper.getTeksLabel(this, "errorLabel");
        assertFalse(
            teksError.isBlank(),
            "errorLabel seharusnya menampilkan pesan validasi ('Pilih layanan dari katalog.')"
        );
    }

    @Test
    @Order(5)
    @DisplayName("TC-AS-05: Tambah Langganan — Simpan custom berhasil, navigasi ke Langganan")
    void addsub_simpanCustomBerhasilNavigasi() {
        loginDanBukaAddsub();

        clickOn("#radioCustom");
        TestHelper.tungguFX();

        String uniqueCustomName = "TestCustom_" + System.currentTimeMillis();
        clickOn("#customNamaField").write(uniqueCustomName);
        clickOn("#biayaField").write("55000");

        // Bypass klik DatePicker (karena kalender popup sering flaky di TestFX)
        // Kita set value secara langsung ke model control-nya
        interact(() -> {
            lookup("#tanggalPicker").queryAs(javafx.scene.control.DatePicker.class)
                .setValue(java.time.LocalDate.now());
        });

        klikTombolSimpan(); 
        TestHelper.tungguNode(this, "#subscriptionTable", AppContext.TIMEOUT_DB);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#subscriptionTable"),
            "Seharusnya pindah ke halaman Langganan Saya setelah save"
        );
    }

    @Test
    @Order(6)
    @DisplayName("TC-AS-06: Tambah Langganan — nama custom mirip katalog tampilkan peringatan")
    void addsub_namaCustomMiripKatalogTampilkanWarning() {
        loginDanBukaAddsub();

        clickOn("#radioCustom");
        TestHelper.tungguFX();

        clickOn("#customNamaField").write("Netflix");
        TestHelper.tungguFX();

        assertTrue(
            TestHelper.isVisible(this, "#katalogWarningLabel"),
            "katalogWarningLabel seharusnya ter-trigger saat mengetik 'Netflix'"
        );

        String teksWarning = TestHelper.getTeksLabel(this, "katalogWarningLabel");
        assertFalse(
            teksWarning.isBlank(),
            "Peringatan tidak boleh kosong"
        );
    }
}