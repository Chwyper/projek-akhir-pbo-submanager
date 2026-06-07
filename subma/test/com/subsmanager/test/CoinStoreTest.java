package com.subsmanager.test;

import javafx.scene.control.Button;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk modul Toko Koin (coinstore.fxml).
 *
 * <p>Mencakup TC-CK-01 hingga TC-CK-07 sesuai Test Plan.
 * Setiap test login sebagai user biasa dan navigasi ke halaman Toko Koin.
 * Proses pembayaran tidak dieksekusi penuh untuk menghindari 
 * perubahan data koin permanen di DB secara berulang.
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Toko Koin")
class CoinStoreTest extends TestBase {

    // ══════════════════════════════════════════════════════
    // SETUP: login + navigasi ke halaman Toko Koin
    // ══════════════════════════════════════════════════════

    private void loginDanBukaTokoCoin() {
        TestHelper.loginSebagaiUser(this);
        TestHelper.tungguNode(this, "#greetingLabel", AppContext.TIMEOUT_DB);
        
        // Navigasi via sidebar
        TestHelper.klikSidebar(this, "\uD83E\uDE99  Toko Koin"); // 🪙  Toko Koin
        TestHelper.tungguNode(this, "#coinBalanceLabel", AppContext.TIMEOUT_DB);
    }

    // ══════════════════════════════════════════════════════
    // TEST CASES
    // ══════════════════════════════════════════════════════

    /**
     * TC-CK-01: Paket koin tampil dari DB.
     * Ekspektasi: Kartu paket (misalnya #cardStarter) berhasil di-render di UI.
     */
    @Test
    @Order(1)
    @DisplayName("TC-CK-01: Toko Koin — Paket koin dimuat dan tampil")
    void tokoCoin_paketKoinTampil() {
        loginDanBukaTokoCoin();

        assertTrue(
            TestHelper.isHalamanAktif(this, "#cardStarter"),
            "Paket koin (cardStarter) seharusnya dimuat dari DB dan ter-render di UI"
        );
    }

    /**
     * TC-CK-02: Saldo koin tampil.
     * Ekspektasi: coinBalanceLabel tidak kosong dan mengindikasikan informasi koin.
     */
    @Test
    @Order(2)
    @DisplayName("TC-CK-02: Toko Koin — Saldo koin tampil")
    void tokoCoin_coinBalanceLabelTampilSaldo() {
        loginDanBukaTokoCoin();

        String teksSaldo = TestHelper.getTeksLabel(this, "coinBalanceLabel");
        
        assertFalse(
            teksSaldo.isBlank(),
            "coinBalanceLabel tidak boleh kosong"
        );
        assertTrue(
            teksSaldo.toLowerCase().contains("koin") || teksSaldo.matches(".*\\d+.*"),
            "coinBalanceLabel seharusnya menyebut angka saldo atau kata 'koin', dapat: " + teksSaldo
        );
    }

    /**
     * TC-CK-03 & 04: Validasi UI ketersediaan tombol pembayaran.
     * Ekspektasi: Tombol Bayar (atau tombol konfirmasi pembayaran) tersedia.
     */
    @Test
    @Order(3)
    @DisplayName("TC-CK-03 & 04: Toko Koin — Tombol aksi pembayaran tersedia")
    void tokoCoin_tombolBayarTersedia() {
        loginDanBukaTokoCoin();

        // Mencari tombol yang mengandung teks "Bayar" menggunakan class .button 
        boolean btnBayarAda = lookup(".button").queryAllAs(Button.class).stream()
            .anyMatch(b -> b.getText() != null && b.getText().toLowerCase().contains("bayar"));

        assertTrue(
            btnBayarAda,
            "Tombol untuk mengeksekusi pembayaran (Bayar) seharusnya ada di antarmuka"
        );
    }

    /**
     * TC-CK-06/07: Tombol "Lihat Riwayat" navigasi ke halaman Riwayat Koin.
     * Ekspektasi: setelah klik, historyTable (dari CoinHistory) ada di scene.
     */
    @Test
    @Order(4)
    @DisplayName("TC-CK-06/07: Toko Koin — 'Lihat Riwayat' navigasi ke CoinHistory")
    void tokoCoin_lihatRiwayatNavigasiKeCoinHistory() {
        loginDanBukaTokoCoin();

        // Mencari tombol Lihat Riwayat secara aman dan klik menggunakan API JavaFX
        Button btnRiwayat = lookup(".button").queryAllAs(Button.class).stream()
            .filter(b -> b.getText() != null && b.getText().contains("Lihat Riwayat"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Tombol 'Lihat Riwayat' tidak ditemukan"));

        interact(btnRiwayat::fire);
        
        // Tunggu halaman baru dimuat
        TestHelper.tungguNode(this, "#historyTable", AppContext.TIMEOUT_DEFAULT);

        assertTrue(
            TestHelper.isHalamanAktif(this, "#historyTable"),
            "Seharusnya otomatis pindah ke halaman Riwayat Koin setelah klik 'Lihat Riwayat'"
        );
        assertFalse(
            TestHelper.isHalamanAktif(this, "#cardStarter"),
            "Seharusnya sudah meninggalkan halaman Toko Koin"
        );
    }
}