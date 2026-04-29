package com.subsmanager.db;

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
 * - savePurchase(trx)   → simpan transaksi pembelian koin ke DB
 * - saveUsage(trx)      → simpan transaksi penggunaan koin ke DB
 * - loadPackages()      → ambil semua paket koin dari tabel coin_packages
 */
public class CoinDAO {

    /**
     * Simpan transaksi pembelian koin (PURCHASE) ke database.
     * Dipanggil oleh CoinService setelah pembayaran berhasil.
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

            // coin_package_id
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
        }

        return -1L;
    }

    /**
     * Simpan transaksi penggunaan koin (USAGE) ke database.
     * Dipanggil oleh FinancialController setelah export PDF/Excel.
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
        }

        return -1L;
    }

    /**
     * Ambil semua paket koin dari tabel coin_packages.
     * Digunakan oleh CoinService agar paket konsisten dengan DB
     * (bukan hardcoded di Java).
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
}