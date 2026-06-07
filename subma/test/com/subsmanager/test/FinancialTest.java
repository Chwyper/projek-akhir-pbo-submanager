package com.subsmanager.test;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Keuangan (financial.fxml).
 *
 * <p>Mencakup TC-FN-01 hingga TC-FN-05.
 * Pengujian Ekspor File difokuskan pada ketersediaan UI untuk menghindari
 * blocking dari native OS Save Dialog.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Keuangan & Export")
class FinancialTest extends TestBase {

    // ══════════════════════════════════════════════════════
    // SETUP: login + navigasi ke halaman Keuangan
    // ══════════════════════════════════════════════════════

    private void loginDanBukaKeuangan() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);
        
        TestHelper.klikSidebar(this, "\uD83D\uDCB0  Keuangan"); // 💰  Keuangan
        TestHelper.tungguNode(this, "#monthlyTotalLabel", AppContext.TIMEOUT_DB);
        
        // Tunggu proses async kurs dan kalkulasi selesai merender UI
        TestHelper.tungguMs(1500); 
    }

    // ══════════════════════════════════════════════════════
    // TEST CASES
    // ══════════════════════════════════════════════════════

    /**
     * TC-FN-01: Halaman keuangan tampil ringkasan biaya bulanan dan tahunan.
     */
    @Test
    @Order(1)
    @DisplayName("TC-FN-01: Keuangan — Halaman keuangan tampil ringkasan")
    void keuangan_halamanTampilRingkasan() {
        loginDanBukaKeuangan();

        String bulanan = TestHelper.getTeksLabel(this, "monthlyTotalLabel");
        String tahunan = TestHelper.getTeksLabel(this, "yearlyTotalLabel");

        assertFalse(
            bulanan.isBlank() || bulanan.equals("Rp 0") || bulanan.equals("0"),
            "Total bulanan harus terisi dengan kalkulasi (bukan 0 atau kosong)"
        );
        assertFalse(
            tahunan.isBlank() || tahunan.equals("Rp 0") || tahunan.equals("0"),
            "Total tahunan harus terisi dengan kalkulasi (bukan 0 atau kosong)"
        );
    }

    /**
     * Memastikan tabel rincian / breakdown langganan terisi (Adaptasi tambahan).
     */
    @Test
    @Order(2)
    @DisplayName("TC-FN-02: Keuangan — Tabel rincian (breakdown) terisi data")
    void keuangan_tabelBreakdownTerisi() {
        loginDanBukaKeuangan();

        @SuppressWarnings("unchecked")
        TableView<?> tabel = lookup("#breakdownTable").queryAs(TableView.class);
        
        assertFalse(
            tabel.getItems().isEmpty(),
            "breakdownTable seharusnya memuat daftar langganan user"
        );
    }

    /**
     * TC-FN-05: Konversi mata uang tampil (menguji info kurs IDR berjalan).
     */
    @Test
    @Order(3)
    @DisplayName("TC-FN-03: Keuangan — Konversi kurs mata uang berjalan")
    void keuangan_konversiKursTampil() {
        loginDanBukaKeuangan();

        // Mencari label mana saja di layar yang memuat teks "USD" atau "Rp" (indikator API berjalan)
        boolean isKursInfoAda = lookup(".label").queryAllAs(Label.class).stream()
            .anyMatch(l -> l.getText() != null && 
                          (l.getText().contains("USD") || l.getText().contains("Kurs")));

        assertTrue(
            isKursInfoAda, 
            "Informasi konversi kurs (USD/IDR) seharusnya ditampilkan di UI"
        );
    }

    /**
     * TC-FN-04: Tombol "Export PDF" ada di halaman Keuangan.
     */
    @Test
    @Order(4)
    @DisplayName("TC-FN-04: Keuangan — Tombol Export PDF tersedia di UI")
    void keuangan_tombolExportPDFAda() {
        loginDanBukaKeuangan();

        // Menggunakan filter stream untuk mencari class .button agar aman dari ambiguitas emoji
        boolean btnPdfAda = lookup(".button").queryAllAs(javafx.scene.control.Button.class)
            .stream()
            .anyMatch(b -> b.getText() != null && b.getText().contains("Export PDF"));

        assertTrue(
            btnPdfAda,
            "Tombol 'Export PDF' seharusnya di-render di halaman Keuangan"
        );
    }

    /**
     * TC-FN-05: Tombol "Export Excel" ada di halaman Keuangan.
     */
    @Test
    @Order(5)
    @DisplayName("TC-FN-05: Keuangan — Tombol Export Excel tersedia di UI")
    void keuangan_tombolExportExcelAda() {
        loginDanBukaKeuangan();

        boolean btnExcelAda = lookup(".button").queryAllAs(javafx.scene.control.Button.class)
            .stream()
            .anyMatch(b -> b.getText() != null && b.getText().contains("Export Excel"));

        assertTrue(
            btnExcelAda,
            "Tombol 'Export Excel' seharusnya di-render di halaman Keuangan"
        );
    }
}