package com.subsmanager.test;

import com.subsmanager.MainApp;
import com.subsmanager.SessionManager;

import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Base class untuk semua TestFX test di SubsManager.
 *
 * <p>Berjalan dengan display asli Windows (non-headless).
 * Semua test class wajib extends TestBase.
 *
 * <p>Contoh penggunaan:
 * <pre>
 *   class LoginTest extends TestBase {
 *       {@literal @}Test
 *       void loginBerhasil() {
 *           clickOn("#emailField").write("warga@email.com");
 *           ...
 *       }
 *   }
 * </pre>
 */
public abstract class TestBase extends ApplicationTest {

    /**
     * Launch MainApp — titik masuk yang sama dengan aplikasi sebenarnya.
     * ApplicationTest memanggil method ini otomatis sebelum setiap test class.
     *
     * @param stage primary stage yang disiapkan oleh TestFX
     */
    @Override
    public void start(Stage stage) throws Exception {
        new MainApp().start(stage);
    }

    /**
     * Cleanup setelah setiap test.
     * Hanya reset currentUser tanpa memanggil navigateTo(),
     * agar tidak ada crash akibat setScene() di luar siklus normal.
     * TestFX akan restart aplikasi sendiri sebelum test berikutnya.
     */
    @AfterEach
    void tearDown() {
        WaitForAsyncUtils.waitForFxEvents();
        // Reset user tanpa navigasi — cukup untuk isolasi state antar test
        javafx.application.Platform.runLater(
            () -> SessionManager.setCurrentUser(null)
        );
        WaitForAsyncUtils.waitForFxEvents();
    }
}