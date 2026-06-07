package com.subsmanager;

import com.subsmanager.auth.User;
import com.subsmanager.db.DatabaseConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * SessionManager - State global aplikasi
 *
 * Menyimpan Stage utama dan User yang sedang login.
 * Semua Controller mengakses class ini untuk navigasi antar scene.
 *
 * Dependency (semua Controller) ..> SessionManager
 */
public class SessionManager {

    private static Stage primaryStage;
    private static User  currentUser;

    private SessionManager() {}

    // ===================== Stage =====================

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // ===================== Navigasi =====================

    /**
     * Pindah ke scene dari file FXML.
     *
     * @param fxmlPath path relatif dari src, contoh:
     *                 "/com/subsmanager/gui/fxml/dashboard.fxml"
     */
    public static void navigateTo(String fxmlPath) {
        try {
        	
        	boolean wasMaximized = primaryStage.isMaximized();

            Parent root = FXMLLoader.load(
                SessionManager.class.getResource(fxmlPath)
            );
            Scene scene = new Scene(root,
                MainApp.WIN_WIDTH, MainApp.WIN_HEIGHT);
            primaryStage.setScene(scene);
            
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }
            
            System.out.println("[SessionManager] Navigasi ke: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("[SessionManager] Gagal navigasi ke: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ===================== User Session =====================

    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("[SessionManager] Login: "
            + (user != null ? user.getEmail() : "(cleared)"));
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        System.out.println("[SessionManager] Logout: "
            + (currentUser != null ? currentUser.getEmail() : "-"));
        currentUser = null;

        // Tutup koneksi DB saat logout
        DatabaseConnection.closeConnection();

        navigateTo("/com/subsmanager/gui/fxml/login.fxml");
    }

    @Override
    public String toString() {
        return "SessionManager{user="
            + (currentUser != null ? currentUser.getEmail() : "null") + "}";
    }
}