package com.subsmanager.test;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility statis berisi helper method yang dipakai bersama di semua test case.
 *
 * <p>Semua method menerima {@link FxRobot} sebagai parameter pertama,
 * yang tersedia dari {@code this} di dalam class yang extends {@link TestBase}.
 *
 * <p>Contoh penggunaan di dalam test:
 * <pre>
 *   TestHelper.loginSebagaiUser(this);
 *   TestHelper.tungguNode(this, "#greetingLabel");
 * </pre>
 */
public final class TestHelper {

    private TestHelper() {}

    // ══════════════════════════════════════════════════════
    // LOGIN HELPERS
    // ══════════════════════════════════════════════════════

    /**
     * Login sebagai user biasa menggunakan kredensial dari {@link AppContext}.
     * Mengasumsikan aplikasi sedang di halaman login.fxml.
     *
     * @param robot FxRobot dari test yang memanggil
     */
    public static void loginSebagaiUser(FxRobot robot) {
        login(robot, AppContext.USER_EMAIL, AppContext.USER_PASSWORD);
    }

    /**
     * Login sebagai admin menggunakan kredensial dari {@link AppContext}.
     * Mengasumsikan aplikasi sedang di halaman login.fxml.
     *
     * @param robot FxRobot dari test yang memanggil
     */
    public static void loginSebagaiAdmin(FxRobot robot) {
        login(robot, AppContext.ADMIN_EMAIL, AppContext.ADMIN_PASSWORD);
    }

    /**
     * Login dengan email dan password yang ditentukan manual.
     * Mengasumsikan aplikasi sedang di halaman login.fxml.
     *
     * @param robot    FxRobot dari test yang memanggil
     * @param email    email akun
     * @param password password akun
     */
    public static void login(FxRobot robot, String email, String password) {
        robot.clickOn("#emailField").write(email);
        robot.clickOn("#passwordField").write(password);
        robot.clickOn("Login");
        // Tunggu UI selesai diupdate setelah DB query
        tungguFX();
    }

    // ══════════════════════════════════════════════════════
    // NAVIGASI HELPERS
    // ══════════════════════════════════════════════════════

    /**
     * Klik tombol sidebar berdasarkan teks label-nya.
     *
     * @param robot     FxRobot dari test
     * @param labelText teks tombol sidebar, misal "Langganan Saya"
     */
    public static void klikSidebar(FxRobot robot, String labelText) {
        robot.clickOn(labelText);
        tungguFX();
    }

    // ══════════════════════════════════════════════════════
    // WAIT HELPERS
    // ══════════════════════════════════════════════════════

    /**
     * Tunggu sampai JavaFX Application Thread selesai memproses semua event.
     * Setara dengan {@code WaitForAsyncUtils.waitForFxEvents()}.
     */
    public static void tungguFX() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Tunggu sampai node dengan query tertentu muncul di scene,
     * dengan timeout {@link AppContext#TIMEOUT_DEFAULT}.
     *
     * @param robot FxRobot dari test
     * @param query selector node, misal "#greetingLabel" atau ".button"
     * @throws RuntimeException jika node tidak muncul dalam batas waktu
     */
    public static void tungguNode(FxRobot robot, String query) {
        tungguNode(robot, query, AppContext.TIMEOUT_DEFAULT);
    }

    /**
     * Tunggu sampai node dengan query tertentu muncul di scene,
     * dengan timeout yang ditentukan.
     *
     * @param robot      FxRobot dari test
     * @param query      selector node
     * @param timeoutMs  batas waktu dalam milidetik
     * @throws RuntimeException jika node tidak muncul dalam batas waktu
     */
    public static void tungguNode(FxRobot robot, String query, int timeoutMs) {
        try {
            WaitForAsyncUtils.waitFor(
                timeoutMs, TimeUnit.MILLISECONDS,
                () -> robot.lookup(query).tryQuery().isPresent()
            );
        } catch (TimeoutException e) {
            throw new RuntimeException(
                "Node tidak ditemukan dalam " + timeoutMs + "ms: " + query, e);
        }
    }

    /**
     * Tunggu dengan jeda waktu tetap (untuk proses async seperti PaymentProcessor).
     * Gunakan hanya jika {@code tungguNode} tidak cukup.
     *
     * @param ms waktu tunggu dalam milidetik
     */
    public static void tungguMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        tungguFX();
    }

    // ══════════════════════════════════════════════════════
    // ASSERTION HELPERS
    // ══════════════════════════════════════════════════════

    /**
     * Ambil teks dari Label berdasarkan fx:id-nya.
     * Berguna untuk verifikasi nilai label di assert.
     *
     * @param robot FxRobot dari test
     * @param fxId  fx:id node Label (tanpa '#'), misal "greetingLabel"
     * @return teks label, atau string kosong jika node tidak ditemukan
     */
    public static String getTeksLabel(FxRobot robot, String fxId) {
        return robot.lookup("#" + fxId)
            .queryAs(Label.class)
            .getText();
    }

    /**
     * Ambil teks dari TextField berdasarkan fx:id-nya.
     *
     * @param robot FxRobot dari test
     * @param fxId  fx:id node TextField (tanpa '#')
     * @return teks field, atau string kosong jika node tidak ditemukan
     */
    public static String getTeksField(FxRobot robot, String fxId) {
        return robot.lookup("#" + fxId)
            .queryAs(TextField.class)
            .getText();
    }

    /**
     * Cek apakah node dengan query tertentu saat ini visible di scene.
     *
     * @param robot FxRobot dari test
     * @param query selector node
     * @return true jika node ada dan visible
     */
    public static boolean isVisible(FxRobot robot, String query) {
        return robot.lookup(query)
            .tryQuery()
            .map(node -> node.isVisible() && node.getParent() != null)
            .orElse(false);
    }

    /**
     * Cek apakah halaman yang aktif sekarang adalah halaman yang diharapkan,
     * dengan cara mencari node unik milik halaman tersebut.
     *
     * <p>Contoh node unik per halaman:
     * <ul>
     *   <li>Dashboard   → {@code #greetingLabel}</li>
     *   <li>Admin Panel → {@code #adminEmailLabel}</li>
     *   <li>Coin History→ {@code #historyTable}</li>
     * </ul>
     *
     * @param robot         FxRobot dari test
     * @param uniqueNodeId  fx:id node yang unik milik halaman tersebut (dengan '#')
     * @return true jika node ditemukan di scene aktif
     */
    public static boolean isHalamanAktif(FxRobot robot, String uniqueNodeId) {
        return robot.lookup(uniqueNodeId).tryQuery().isPresent();
    }
}