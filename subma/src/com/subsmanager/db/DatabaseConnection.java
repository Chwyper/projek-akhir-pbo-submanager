package com.subsmanager.db;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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

    /** HikariCP DataSource instance */
    private static HikariDataSource dataSource;

    /** Private constructor — tidak boleh di-instantiate */
    private DatabaseConnection() {}

    /**
     * Inisialisasi HikariCP pool
     */
    private static void initPool() {
        if (dataSource == null) {
            System.out.println("[DB] Inisialisasi HikariCP Connection Pool...");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            config.setUsername(USER);
            config.setPassword(PASS);
            
            // Optimasi Pool untuk aplikasi desktop
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000); // 10 detik timeout
            config.setIdleTimeout(600000); // 10 menit idle timeout
            config.setMaxLifetime(1800000); // 30 menit usia maksimal
            
            dataSource = new HikariDataSource(config);
            System.out.println("[DB] HikariCP Pool berhasil dibuat.");
        }
    }

    /**
     * Ambil koneksi dari pool.
     *
     * @return Connection ke Supabase PostgreSQL
     * @throws SQLException jika koneksi gagal
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initPool();
        }
        return dataSource.getConnection();
    }

    /**
     * Tutup seluruh kolam koneksi saat logout atau aplikasi ditutup.
     */
    public static void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DB] HikariCP Connection Pool ditutup.");
        }
    }

    /**
     * Cek apakah koneksi pool aktif — untuk debugging.
     *
     * @return true jika terhubung
     */
    public static boolean isConnected() {
        return dataSource != null && !dataSource.isClosed() && dataSource.isRunning();
    }
}