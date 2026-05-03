package com.subsmanager.db;

import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceDAO - Data Access Object untuk tabel services dan service_tiers.
 * Digunakan khusus oleh AdminPanelController.
 *
 * Operasi:
 * - loadAllServices()           → ambil semua layanan dari DB
 * - saveService(service)        → INSERT layanan baru
 * - deleteService(serviceId)    → DELETE layanan + tier-tiernya
 * - loadTiersByService(svcId)   → ambil tier milik satu layanan
 * - saveTier(tier, serviceId)   → INSERT tier baru
 * - deleteTier(tierId)          → DELETE tier
 *
 * Admin queries:
 * - loadAllUsers()              → ambil semua user untuk tab User
 * - deleteUser(userId)          → hapus user + data terkait
 * - loadAllPurchases()          → ambil semua transaksi PURCHASE
 * - getTotalRevenue()           → total pemasukan
 */
public class ServiceDAO {

    // ═══════════════════════════════════════════════
    // SERVICES
    // ═══════════════════════════════════════════════

    /**
     * Ambil semua layanan dari tabel services.
     *
     * @return list Service dari DB
     */
    public static List<Service> loadAllServices() {
        List<Service> result = new ArrayList<>();

        String sql =
            "SELECT id, name, domain, cancellation_url, " +
            "       category, default_currency " +
            "FROM services ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Service svc = new Service();
                svc.setId(rs.getLong("id"));
                svc.setName(rs.getString("name"));
                svc.setDomain(rs.getString("domain"));
                svc.setCancellationUrl(rs.getString("cancellation_url"));
                svc.setCategory(rs.getString("category"));
                svc.setDefaultCurrency(rs.getString("default_currency"));
                result.add(svc);
            }

            System.out.println("[ServiceDAO] Load " +
                result.size() + " layanan dari DB.");

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error loadAllServices: " +
                e.getMessage());
        }

        return result;
    }

    /**
     * Simpan layanan baru ke tabel services.
     *
     * @param svc layanan yang akan disimpan
     * @return id yang di-generate DB, atau -1 jika gagal
     */
    public static long saveService(Service svc) {
        String sql =
            "INSERT INTO services " +
            "(name, domain, cancellation_url, category, default_currency) " +
            "VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, svc.getName());
            ps.setString(2, svc.getDomain());
            ps.setString(3, svc.getCancellationUrl());
            ps.setString(4, svc.getCategory());
            ps.setString(5, svc.getDefaultCurrency());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                System.out.println("[ServiceDAO] Layanan disimpan, id=" + id);
                return id;
            }

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error saveService: " +
                e.getMessage());
        }

        return -1L;
    }

    /**
     * Hapus layanan + semua tier-nya dari DB.
     *
     * @param serviceId id layanan yang dihapus
     * @return true jika berhasil
     */
    public static boolean deleteService(long serviceId) {
        // Hapus tier dulu (FK constraint)
        String sqlTier = "DELETE FROM service_tiers WHERE service_id = ?";
        String sqlSvc  = "DELETE FROM services WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlTier)) {
                ps.setLong(1, serviceId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlSvc)) {
                ps.setLong(1, serviceId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("[ServiceDAO] Layanan id=" +
                        serviceId + " dihapus.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error deleteService: " +
                e.getMessage());
        }

        return false;
    }

    // ═══════════════════════════════════════════════
    // SERVICE TIERS
    // ═══════════════════════════════════════════════

    /**
     * Ambil semua tier milik satu layanan.
     *
     * @param serviceId id layanan
     * @return list ServiceTier
     */
    public static List<ServiceTier> loadTiersByService(long serviceId) {
        List<ServiceTier> result = new ArrayList<>();

        String sql =
            "SELECT id, tier_name, description " +
            "FROM service_tiers WHERE service_id = ? " +
            "ORDER BY tier_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, serviceId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ServiceTier tier = new ServiceTier();
                tier.setId(rs.getLong("id"));
                tier.setTierName(rs.getString("tier_name"));
                tier.setDescription(rs.getString("description"));
                result.add(tier);
            }

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error loadTiersByService: " +
                e.getMessage());
        }

        return result;
    }

    /**
     * Simpan tier baru ke tabel service_tiers.
     *
     * @param tier      tier yang akan disimpan
     * @param serviceId id layanan pemilik tier
     * @return id yang di-generate DB, atau -1 jika gagal
     */
    public static long saveTier(ServiceTier tier, long serviceId) {
        String sql =
            "INSERT INTO service_tiers (service_id, tier_name, description) " +
            "VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, serviceId);
            ps.setString(2, tier.getTierName());
            ps.setString(3, tier.getDescription() != null
                ? tier.getDescription() : "");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                System.out.println("[ServiceDAO] Tier disimpan, id=" + id);
                return id;
            }

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error saveTier: " +
                e.getMessage());
        }

        return -1L;
    }

    /**
     * Hapus tier dari DB.
     *
     * @param tierId id tier yang dihapus
     * @return true jika berhasil
     */
    public static boolean deleteTier(long tierId) {
        String sql = "DELETE FROM service_tiers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, tierId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[ServiceDAO] Tier id=" +
                    tierId + " dihapus.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error deleteTier: " +
                e.getMessage());
        }

        return false;
    }

    // ═══════════════════════════════════════════════
    // ADMIN — USER MANAGEMENT
    // ═══════════════════════════════════════════════

    /**
     * DTO untuk tampilan data user di tab Admin.
     */
    public static class UserRecord {
        private final long   id;
        private final String email;
        private final String tanggalDaftar;
        private final int    saldoKoin;
        private final boolean isAdmin;

        public UserRecord(long id, String email,
                           String tanggalDaftar,
                           int saldoKoin, boolean isAdmin) {
            this.id           = id;
            this.email        = email;
            this.tanggalDaftar = tanggalDaftar;
            this.saldoKoin    = saldoKoin;
            this.isAdmin      = isAdmin;
        }

        public long    getId()            { return id; }
        public String  getEmail()         { return email; }
        public String  getTanggalDaftar() { return tanggalDaftar; }
        public int     getSaldoKoin()     { return saldoKoin; }
        public boolean isAdmin()          { return isAdmin; }
        public String  getRoleLabel()     { return isAdmin ? "Admin" : "User"; }
    }

    /**
     * Ambil semua user dari DB untuk Tab User.
     *
     * @return list UserRecord
     */
    public static List<UserRecord> loadAllUsers() {
        List<UserRecord> result = new ArrayList<>();

        String sql =
            "SELECT u.id, u.email, u.is_admin, " +
            "       COALESCE(cb.balance, 0) AS saldo, " +
            "       TO_CHAR(u.created_at, 'DD/MM/YYYY') AS tgl_daftar " +
            "FROM users u " +
            "LEFT JOIN coin_balances cb ON cb.user_id = u.id " +
            "ORDER BY u.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new UserRecord(
                    rs.getLong   ("id"),
                    rs.getString ("email"),
                    rs.getString ("tgl_daftar"),
                    rs.getInt    ("saldo"),
                    rs.getBoolean("is_admin")
                ));
            }

            System.out.println("[ServiceDAO] Load " +
                result.size() + " user dari DB.");

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error loadAllUsers: " +
                e.getMessage());
        }

        return result;
    }

    /**
     * Hapus user + semua data terkait (subscription, coin, transaksi).
     *
     * @param userId id user yang dihapus
     * @return true jika berhasil
     */
    public static boolean deleteUser(long userId) {
        // Urutan hapus: transaksi → subscription → coin_balances → user
        String[] sqls = {
            "DELETE FROM coin_transactions WHERE user_id = ?",
            "DELETE FROM subscriptions WHERE user_id = ?",
            "DELETE FROM coin_balances WHERE user_id = ?",
            "DELETE FROM users WHERE id = ?"
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (String sql : sqls) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }
            }
            System.out.println("[ServiceDAO] User id=" +
                userId + " berhasil dihapus.");
            return true;

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error deleteUser: " +
                e.getMessage());
        }

        return false;
    }

    // ═══════════════════════════════════════════════
    // ADMIN — PEMASUKAN
    // ═══════════════════════════════════════════════

    /**
     * DTO untuk tampilan transaksi pembelian di tab Pemasukan.
     */
    public static class PurchaseRecord {
        private final long   id;
        private final String email;
        private final String tanggal;
        private final String kode;
        private final int    coinAmount;
        private final String harga;
        private final String metodeBayar;
        private final String status;

        public PurchaseRecord(long id, String email, String tanggal,
                               String kode, int coinAmount,
                               String harga, String metodeBayar,
                               String status) {
            this.id          = id;
            this.email       = email;
            this.tanggal     = tanggal;
            this.kode        = kode;
            this.coinAmount  = coinAmount;
            this.harga       = harga;
            this.metodeBayar = metodeBayar;
            this.status      = status;
        }

        public long   getId()          { return id; }
        public String getEmail()       { return email; }
        public String getTanggal()     { return tanggal; }
        public String getKode()        { return kode; }
        public int    getCoinAmount()  { return coinAmount; }
        public String getHarga()       { return harga; }
        public String getMetodeBayar() { return metodeBayar; }
        public String getStatus()      { return status; }
    }

    /**
     * Ambil semua transaksi PURCHASE dari semua user.
     *
     * @return list PurchaseRecord
     */
    public static List<PurchaseRecord> loadAllPurchases() {
        List<PurchaseRecord> result = new ArrayList<>();

        String sql =
            "SELECT ct.id, u.email, " +
            "       TO_CHAR(ct.created_at, 'DD/MM/YYYY HH24:MI') AS tanggal, " +
            "       ct.transaction_code, ct.coin_amount, " +
            "       ct.price, ct.payment_method, ct.status " +
            "FROM coin_transactions ct " +
            "JOIN users u ON u.id = ct.user_id " +
            "WHERE ct.type = 'PURCHASE' " +
            "ORDER BY ct.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int hargaRaw = rs.getInt("price");
                result.add(new PurchaseRecord(
                    rs.getLong  ("id"),
                    rs.getString("email"),
                    rs.getString("tanggal"),
                    rs.getString("transaction_code"),
                    rs.getInt   ("coin_amount"),
                    "Rp " + String.format("%,d", hargaRaw),
                    rs.getString("payment_method") != null
                        ? rs.getString("payment_method") : "-",
                    rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error loadAllPurchases: " +
                e.getMessage());
        }

        return result;
    }

    /**
     * Hitung total pemasukan dari semua transaksi PURCHASE SUCCESS.
     *
     * @return total dalam rupiah
     */
    public static long getTotalRevenue() {
        String sql =
            "SELECT COALESCE(SUM(price), 0) AS total " +
            "FROM coin_transactions " +
            "WHERE type = 'PURCHASE' AND status = 'SUCCESS'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getLong("total");

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error getTotalRevenue: " +
                e.getMessage());
        }

        return 0L;
    }
}