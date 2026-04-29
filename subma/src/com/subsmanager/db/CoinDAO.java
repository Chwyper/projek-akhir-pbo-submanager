package com.subsmanager.db;

import com.subsmanager.auth.User;
import com.subsmanager.coin.CoinPackage;
import com.subsmanager.coin.CoinTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CoinDAO - Data Access Object untuk tabel coin_transactions
 * dan coin_packages.
 *
 * Operasi yang tersedia:
 * - savePurchase(trx)        → simpan transaksi pembelian koin ke DB
 * - saveUsage(trx)           → simpan transaksi penggunaan koin ke DB
 * - loadPackages()           → ambil semua paket koin dari tabel coin_packages
 * - loadTransactions(user)   → ambil riwayat transaksi milik user
 */
public class CoinDAO {

    // ── DTO untuk riwayat transaksi ───────────────────────

    /**
     * TransactionRecord - DTO (Data Transfer Object) sederhana
     * untuk menampilkan riwayat transaksi di tabel UI.
     * Tidak perlu instansiasi CoinTransaction yang kompleks.
     */
    public static class TransactionRecord {
        private final long   id;
        private final String tanggal;
        private final String kode;
        private final String tipe;        // PURCHASE / USAGE
        private final String deskripsi;
        private final int    jumlahKoin;
        private final String harga;       // hanya untuk PURCHASE
        private final String status;
        private final String metodeBayar; // hanya untuk PURCHASE

        public TransactionRecord(long id, String tanggal, String kode,
                                  String tipe, String deskripsi,
                                  int jumlahKoin, String harga,
                                  String status, String metodeBayar) {
            this.id          = id;
            this.tanggal     = tanggal;
            this.kode        = kode;
            this.tipe        = tipe;
            this.deskripsi   = deskripsi;
            this.jumlahKoin  = jumlahKoin;
            this.harga       = harga;
            this.status      = status;
            this.metodeBayar = metodeBayar;
        }

        public long   getId()          { return id; }
        public String getTanggal()     { return tanggal; }
        public String getKode()        { return kode; }
        public String getTipe()        { return tipe; }
        public String getDeskripsi()   { return deskripsi; }
        public int    getJumlahKoin()  { return jumlahKoin; }
        public String getHarga()       { return harga; }
        public String getStatus()      { return status; }
        public String getMetodeBayar() { return metodeBayar; }

        /** Label ramah untuk kolom Tipe di tabel */
        public String getTipeLabel() {
            return "PURCHASE".equals(tipe) ? "Top Up" : "Penggunaan";
        }
    }

    // ── Methods ───────────────────────────────────────────

    /**
     * Simpan transaksi pembelian koin (PURCHASE) ke database.
     *
     * @param trx transaksi yang akan disimpan
     * @return id yang di-generate DB, atau -1 jika gagal
     */
    public static long savePurchase(CoinTransaction trx) {
        String sql =
            "INSERT INTO coin_transactions " +
            "(user_id, type, status, payment_method, " +
            " coin_package_id, coin_amount, price, " +
            " description, transaction_code) " +
            "VALUES (?, 'PURCHASE', ?, ?, ?, ?, ?, ?, ?) " +
            "RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, trx.getUser().getId());
            ps.setString(2, trx.getStatus().name());
            ps.setString(3, trx.getPaymentMethod() != null
                ? trx.getPaymentMethod().name() : null);

            if (trx.getCoinPackage() != null) {
                ps.setLong(4, trx.getCoinPackage().getId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            ps.setInt   (5, trx.getCoinAmount());
            ps.setInt   (6, trx.getPrice());
            ps.setString(7, trx.getDescription());
            ps.setString(8, trx.getTransactionCode());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong("id");
                System.out.println("[CoinDAO] Transaksi PURCHASE disimpan, id=" +
                    generatedId + ", kode=" + trx.getTransactionCode());
                return generatedId;
            }

        } catch (SQLException e) {
            System.err.println("[CoinDAO] Error savePurchase: " +
                e.getMessage());
            e.printStackTrace();
        }

        return -1L;
    }

    /**
     * Simpan transaksi penggunaan koin (USAGE) ke database.
     *
     * @param trx transaksi yang akan disimpan
     * @return id yang di-generate DB, atau -1 jika gagal
     */
    public static long saveUsage(CoinTransaction trx) {
        String sql =
            "INSERT INTO coin_transactions " +
            "(user_id, type, status, payment_method, " +
            " coin_package_id, coin_amount, price, " +
            " description, transaction_code) " +
            "VALUES (?, 'USAGE', 'SUCCESS', NULL, NULL, ?, 0, ?, ?) " +
            "RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, trx.getUser().getId());
            ps.setInt   (2, trx.getCoinAmount());
            ps.setString(3, trx.getDescription());
            ps.setString(4, trx.getTransactionCode());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong("id");
                System.out.println("[CoinDAO] Transaksi USAGE disimpan, id=" +
                    generatedId + ", deskripsi=" + trx.getDescription());
                return generatedId;
            }

        } catch (SQLException e) {
            System.err.println("[CoinDAO] Error saveUsage: " +
                e.getMessage());
            System.err.println("[CoinDAO] SQL State: " + e.getSQLState());
            e.printStackTrace();
        }

        return -1L;
    }

    /**
     * Ambil semua paket koin dari tabel coin_packages.
     *
     * @return list CoinPackage dari DB, kosong jika gagal
     */
    public static List<CoinPackage> loadPackages() {
        List<CoinPackage> packages = new ArrayList<>();

        String sql =
            "SELECT id, name, coin_amount, price, currency " +
            "FROM coin_packages " +
            "ORDER BY price ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CoinPackage pkg = new CoinPackage(
                    rs.getLong  ("id"),
                    rs.getString("name"),
                    rs.getInt   ("coin_amount"),
                    rs.getInt   ("price"),
                    rs.getString("currency")
                );
                packages.add(pkg);
            }

            System.out.println("[CoinDAO] Berhasil load " +
                packages.size() + " paket koin dari DB.");

        } catch (SQLException e) {
            System.err.println("[CoinDAO] Error loadPackages: " +
                e.getMessage());
        }

        return packages;
    }

    /**
     * Ambil semua riwayat transaksi koin milik user dari DB.
     * Diurutkan dari transaksi terbaru (created_at DESC).
     *
     * @param user user yang sedang login
     * @return list TransactionRecord siap ditampilkan di TableView
     */
    public static List<TransactionRecord> loadTransactions(User user) {
        List<TransactionRecord> result = new ArrayList<>();

        String sql =
            "SELECT id, type, status, payment_method, " +
            "       coin_amount, price, description, " +
            "       transaction_code, " +
            "       TO_CHAR(created_at, 'DD/MM/YYYY HH24:MI') AS tanggal " +
            "FROM coin_transactions " +
            "WHERE user_id = ? " +
            "ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int    hargaRaw = rs.getInt("price");
                String tipe     = rs.getString("type");
                String metode   = rs.getString("payment_method");

                // Format harga hanya untuk PURCHASE
                String hargaStr = "PURCHASE".equals(tipe)
                    ? "Rp " + String.format("%,d", hargaRaw)
                    : "-";

                String metodeStr = (metode != null) ? metode : "-";

                TransactionRecord rec = new TransactionRecord(
                    rs.getLong  ("id"),
                    rs.getString("tanggal"),
                    rs.getString("transaction_code"),
                    tipe,
                    rs.getString("description"),
                    rs.getInt   ("coin_amount"),
                    hargaStr,
                    rs.getString("status"),
                    metodeStr
                );
                result.add(rec);
            }

            System.out.println("[CoinDAO] Berhasil load " +
                result.size() + " transaksi untuk user id=" + user.getId());

        } catch (SQLException e) {
            System.err.println("[CoinDAO] Error loadTransactions: " +
                e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}