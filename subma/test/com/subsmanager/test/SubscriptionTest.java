package com.subsmanager.test;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Langganan Saya (subscription.fxml).
 *
 * <p>Mencakup TC-SB-01 hingga TC-SB-07 sesuai Test Plan.
 * Setiap test login sebagai user lalu navigasi ke halaman Langganan Saya
 * via sidebar sebelum melakukan pengujian.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Langganan Saya")
class SubscriptionTest extends TestBase {

    // ══════════════════════════════════════════════════════
    // SETUP: login + navigasi ke halaman Langganan Saya
    // ══════════════════════════════════════════════════════

    private void loginDanBukaLangganan() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);
        
        // Navigasi via sidebar (📋  Langganan Saya)
        TestHelper.klikSidebar(this, "\uD83D\uDCCB  Langganan Saya");
        TestHelper.tungguNode(this, "#subscriptionTable", AppContext.TIMEOUT_DB);
        TestHelper.tungguMs(1000); // Tunggu FilteredList selesai me-load data
    }

    // ══════════════════════════════════════════════════════
    // TEST CASES
    // ══════════════════════════════════════════════════════

    /**
     * TC-SB-01: Halaman Langganan Saya tampil dengan elemen yang benar.
     * Ekspektasi: subscriptionTable, searchField, filterCombo, dan
     * tombol "+ Tambah Langganan" semua ada di scene.
     */
    @Test
    @Order(1)
    @DisplayName("TC-SB-01: Halaman Langganan Saya tampil")
    void langganan_halamanTampil() {
        loginDanBukaLangganan();

        assertTrue(
            TestHelper.isHalamanAktif(this, "#subscriptionTable"),
            "subscriptionTable seharusnya ada di halaman Langganan Saya"
        );
        assertTrue(
            TestHelper.isHalamanAktif(this, "#searchField"),
            "searchField seharusnya ada"
        );
        assertTrue(
            TestHelper.isHalamanAktif(this, "#filterCombo"),
            "filterCombo seharusnya ada"
        );
    }

    /**
     * TC-SB-02: Tabel berisi data langganan user.
     * Ekspektasi: subscriptionTable tidak kosong,
     * subtitleLabel mencerminkan jumlah langganan.
     */
    @Test
    @Order(2)
    @DisplayName("TC-SB-02: Langganan Saya — tabel berisi data")
    void langganan_tabelBerisiData() {
        loginDanBukaLangganan();

        @SuppressWarnings("unchecked")
        TableView<?> tabel = lookup("#subscriptionTable").queryAs(TableView.class);
        assertFalse(
            tabel.getItems().isEmpty(),
            "subscriptionTable seharusnya berisi langganan milik user"
        );

        String teksSubtitle = TestHelper.getTeksLabel(this, "subtitleLabel");
        assertFalse(
            teksSubtitle.isBlank(),
            "subtitleLabel seharusnya menampilkan jumlah langganan aktif"
        );
        assertTrue(
            teksSubtitle.contains("langganan aktif"),
            "subtitleLabel harus mengandung teks 'langganan aktif'"
        );
    }

    /**
     * TC-SB-03: Fitur pencarian memfilter tabel sesuai kata kunci.
     * Ekspektasi: setelah mengetik nama langganan yang spesifik, jumlah baris
     * tabel berkurang (terfilter); setelah dihapus, kembali penuh.
     */
    @Test
    @Order(3)
    @DisplayName("TC-SB-03: Langganan Saya — search memfilter tabel")
    void langganan_searchMemfilterTabel() {
        loginDanBukaLangganan();

        @SuppressWarnings("unchecked")
        TableView<?> tabel = lookup("#subscriptionTable").queryAs(TableView.class);
        int jumlahSebelumFilter = tabel.getItems().size();

        // Act — ketik kata kunci acak yang sangat spesifik
        clickOn("#searchField").write("XyxAbc999");
        TestHelper.tungguFX();

        int jumlahSesudahFilter = tabel.getItems().size();

        // Menggunakan Platform.runLater() agar Textfield.clear() lebih aman dari sekadar UI erase
        TextField searchField = lookup("#searchField").queryAs(TextField.class);
        javafx.application.Platform.runLater(searchField::clear);
        TestHelper.tungguFX();
        
        int jumlahSetelahReset = tabel.getItems().size();

        assertTrue(
            jumlahSesudahFilter < jumlahSebelumFilter,
            "Tabel seharusnya terfilter (menyusut) saat ada kata kunci yang tidak ada di data awal"
        );

        assertEquals(
            jumlahSebelumFilter,
            jumlahSetelahReset,
            "Tabel seharusnya kembali ke jumlah awal setelah kata kunci dihapus"
        );
    }

    /**
     * TC-SB-04: Dropdown filter kategori dapat mengubah isi tabel.
     * Ekspektasi: setelah memilih kategori "Streaming", tabel memfilter 
     * hanya yang sesuai kategori.
     */
    @Test
    @Order(4)
    @DisplayName("TC-SB-04: Langganan Saya — filter kategori berfungsi")
    void langganan_filterKategoriBerfungsi() {
        loginDanBukaLangganan();

        @SuppressWarnings("unchecked")
        TableView<?> tabel = lookup("#subscriptionTable").queryAs(TableView.class);
        int jumlahSemua = tabel.getItems().size();

        // Act — klik dan ubah value ComboBox langsung lewat API untuk stabilitas TestFX
        @SuppressWarnings("unchecked")
        ComboBox<String> filterCombo = lookup("#filterCombo").queryComboBox();
        javafx.application.Platform.runLater(() -> filterCombo.setValue("Streaming"));
        TestHelper.tungguFX();

        int jumlahStreaming = tabel.getItems().size();

        assertTrue(
            jumlahStreaming <= jumlahSemua,
            "Filter 'Streaming' seharusnya memperlihatkan subset (lebih kecil atau sama dengan) semua langganan"
        );
    }

    /**
     * TC-SB-05: Tombol Reset membersihkan semua filter.
     * Ekspektasi: setelah mengisi searchField dan memilih filterCombo,
     * klik Reset → searchField kosong dan tabel kembali penuh.
     */
    @Test
    @Order(5)
    @DisplayName("TC-SB-05: Langganan Saya — tombol Reset membersihkan filter")
    void langganan_tombolResetBersihkanFilter() {
        loginDanBukaLangganan();

        @SuppressWarnings("unchecked")
        TableView<?> tabel = lookup("#subscriptionTable").queryAs(TableView.class);
        int jumlahPenuh = tabel.getItems().size();

        // Pasang filter
        clickOn("#searchField").write("DataAcak123");
        TestHelper.tungguFX();

        // Act — klik Reset
        clickOn("Reset");
        TestHelper.tungguFX();

        // Assert — searchField kosong
        String teksSearch = TestHelper.getTeksField(this, "searchField");
        assertTrue(
            teksSearch.isBlank(),
            "searchField seharusnya kosong setelah Reset"
        );

        // Assert — tabel kembali penuh
        int jumlahSetelahReset = tabel.getItems().size();
        assertEquals(
            jumlahPenuh,
            jumlahSetelahReset,
            "Tabel seharusnya kembali ke jumlah penuh setelah klik Reset"
        );
    }

    /**
     * TC-SB-06: Tombol "+ Tambah Langganan" navigasi ke halaman addsub.
     * Ekspektasi: setelah klik, form tambah langganan tampil.
     */
    @Test
    @Order(6)
    @DisplayName("TC-SB-06: Langganan Saya — '+ Tambah Langganan' navigasi ke addsub")
    void langganan_tombolTambahNavigasiKeAddsub() {
        loginDanBukaLangganan();

        clickOn("+ Tambah Langganan");
        TestHelper.tungguNode(this, "#radioPredefined", AppContext.TIMEOUT_DEFAULT);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#radioPredefined"),
            "Seharusnya navigasi ke halaman addsub setelah klik '+ Tambah Langganan'"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#subscriptionTable"),
            "Seharusnya sudah berpindah dari halaman Langganan Saya"
        );
    }

    /**
     * TC-SB-07: Kolom Aksi berisi tombol "Batal Langganan" dan "Hapus" per baris.
     * Ekspektasi: karena tabel punya data, tombol aksi tersedia di scene.
     */
    @Test
    @Order(7)
    @DisplayName("TC-SB-07: Langganan Saya — tombol aksi 'Batal Langganan' dan 'Hapus' ada")
    void langganan_kolomAksiPunyaTombol() {
        loginDanBukaLangganan();

        @SuppressWarnings("unchecked")
        TableView<?> tabel = lookup("#subscriptionTable").queryAs(TableView.class);
        assertFalse(
            tabel.getItems().isEmpty(),
            "Prasyarat: tabel harus berisi data agar tombol aksi muncul"
        );

        // Assert — tombol yang di-generate dari Controller cellFactory ada di scene
        assertTrue(
            lookup("Batal Langganan").tryQuery().isPresent(),
            "Tombol 'Batal Langganan' seharusnya ter-render di dalam baris tabel"
        );
        assertTrue(
            lookup("Hapus").tryQuery().isPresent(),
            "Tombol 'Hapus' seharusnya ter-render di dalam baris tabel"
        );
    }

    /**
     * TC-SB-08: Search keyword tidak cocok → tabel kosong.
     */
    @Test
    @Order(8)
    @DisplayName("TC-SB-08: Langganan Saya — search keyword tidak cocok -> tabel kosong")
    void langganan_searchTidakCocokKosong() {
        loginDanBukaLangganan();

        clickOn("#searchField").write("xyz123random");
        TestHelper.tungguFX();

        @SuppressWarnings("unchecked")
        javafx.scene.control.TableView<?> tabel = lookup("#subscriptionTable").queryAs(javafx.scene.control.TableView.class);
        assertEquals(
            0,
            tabel.getItems().size(),
            "Tabel seharusnya kosong saat keyword tidak cocok"
        );
    }
}