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

    // Session Pooler — aws-1-ap-southeast-1
    private static final String HOST =
        "aws-1-ap-southeast-1.pooler.supabase.com";
    private static final String PORT  = "5432";
    private static final String DB    = "postgres";
    private static final String USER  = "postgres.gcjjtyexaastjijfckyh"; 
    private static final String PASS  = "K8%gF?EgPZRsExv";

    private static final String JDBC_URL =
        "jdbc:postgresql://" + HOST + ":" + PORT
        + "/" + DB
        + "?sslmode=require"
        + "&connectTimeout=10"
        + "&socketTimeout=30"
        + "&ApplicationName=SubsManager";

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