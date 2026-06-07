package com.subsmanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Singleton koneksi JDBC ke Supabase PostgreSQL.
 * Menggunakan Session Pooler (IPv4) karena Direct Connection port 5432 diblokir.
 *
 * Panggil getConnection() dari DAO mana pun.
 * Panggil closeConnection() dari SessionManager.logout().
 */
public class DatabaseConnection {

    private static final String HOST;
    private static final String PORT;
    private static final String DB;
    private static final String USER;
    private static final String PASS;
    private static final String JDBC_URL;

    static {
        java.util.Properties props = new java.util.Properties();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(".env"))) {
            props.load(reader);
        } catch (java.io.IOException e) {
            System.err.println("[DB] Warning: File .env tidak ditemukan. Mencoba System Environment Variable.");
        }

        HOST = getEnvOrProp("DB_HOST", props, "localhost");
        PORT = getEnvOrProp("DB_PORT", props, "5432");
        DB   = getEnvOrProp("DB_NAME", props, "postgres");
        USER = getEnvOrProp("DB_USER", props, "postgres");
        PASS = getEnvOrProp("DB_PASS", props, "");

        JDBC_URL = "jdbc:postgresql://" + HOST + ":" + PORT
            + "/" + DB
            + "?sslmode=require"
            + "&connectTimeout=10"
            + "&socketTimeout=30"
            + "&ApplicationName=SubsManager";
    }

    private static String getEnvOrProp(String key, java.util.Properties props, String defaultValue) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return props.getProperty(key, defaultValue);
    }

    /** Singleton instance koneksi */
    private static Connection instance;

    /** Private constructor — tidak boleh di-instantiate */
    private DatabaseConnection() {}

    /**
     * Ambil koneksi singleton.
     * Buat koneksi baru jika belum ada, sudah tertutup, atau tidak valid.
     *
     * @return Connection ke Supabase PostgreSQL
     * @throws SQLException jika koneksi gagal
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()
                || !instance.isValid(3)) {
            System.out.println("[DB] Membuka koneksi ke Supabase (Session Pooler)...");
            instance = DriverManager.getConnection(JDBC_URL, USER, PASS);
            System.out.println("[DB] Koneksi berhasil.");
        }
        return instance;
    }

    /**
     * Tutup koneksi saat logout atau aplikasi ditutup.
     */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("[DB] Koneksi ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Gagal menutup koneksi: "
                + e.getMessage());
        }
    }

    /**
     * Cek apakah koneksi aktif — untuk debugging.
     *
     * @return true jika terhubung
     */
    public static boolean isConnected() {
        try {
            return instance != null
                && !instance.isClosed()
                && instance.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}