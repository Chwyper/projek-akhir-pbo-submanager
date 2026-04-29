package com.subsmanager.db;

import com.subsmanager.auth.User;
import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;
import com.subsmanager.subscription.model.BillingCycle;
import com.subsmanager.subscription.model.CustomSubscription;
import com.subsmanager.subscription.model.PredefinedSubscription;
import com.subsmanager.subscription.model.Subscription;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SubscriptionDAO - Data Access Object untuk tabel subscriptions.
 *
 * Operasi yang tersedia:
 * - loadByUser(user)       → ambil semua langganan milik user dari DB
 * - save(sub, userId)      → simpan langganan baru ke DB, return id-nya
 * - delete(id)             → hapus langganan berdasarkan id
 *
 * Menggunakan Single Table Inheritance:
 * kolom "type" membedakan PREDEFINED vs CUSTOM.
 */
public class SubscriptionDAO {

    // ── Konstanta tipe subscription ───────────────────────
    private static final String TYPE_PREDEFINED = "PREDEFINED";
    private static final String TYPE_CUSTOM     = "CUSTOM";

    /**
     * Ambil semua langganan milik user dari database.
     * Rekonstruksi object PredefinedSubscription atau CustomSubscription
     * berdasarkan kolom "type".
     *
     * @param user user yang sedang login
     * @return list subscription milik user (kosong jika tidak ada)
     */
    public static List<Subscription> loadByUser(User user) {
        List<Subscription> result = new ArrayList<>();

        String sql =
            "SELECT s.id, s.type, s.service_name, s.cost, s.currency, " +
            "       s.billing_date, s.billing_cycle, s.tier, " +
            "       s.service_id, s.tier_id, " +
            "       s.custom_cancel_url, s.custom_domain, s.category, " +
            "       sv.name AS svc_name, sv.domain AS svc_domain, " +
            "       sv.cancellation_url, sv.category AS svc_category, " +
            "       sv.default_currency, " +
            "       st.tier_name, st.description AS tier_desc " +
            "FROM subscriptions s " +
            "LEFT JOIN services sv ON sv.id = s.service_id " +
            "LEFT JOIN service_tiers st ON st.id = s.tier_id " +
            "WHERE s.user_id = ? " +
            "ORDER BY s.billing_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                Subscription sub;

                if (TYPE_PREDEFINED.equalsIgnoreCase(type)) {
                    sub = buildPredefined(rs);
                } else {
                    sub = buildCustom(rs);
                }

                result.add(sub);
            }

            System.out.println("[SubscriptionDAO] Berhasil load " +
                result.size() + " langganan untuk user id=" + user.getId());

        } catch (SQLException e) {
            System.err.println("[SubscriptionDAO] Error loadByUser: " +
                e.getMessage());
        }

        return result;
    }

    /**
     * Simpan langganan baru ke database.
     * Otomatis menentukan kolom yang diisi berdasarkan tipe subscription.
     *
     * @param sub    objek subscription yang akan disimpan
     * @param userId id user pemilik subscription
     * @return id yang di-generate DB, atau -1 jika gagal
     */
    public static long save(Subscription sub, long userId) {
        String sql =
            "INSERT INTO subscriptions " +
            "(user_id, type, service_name, cost, currency, " +
            " billing_date, billing_cycle, tier, " +
            " service_id, tier_id, " +
            " custom_cancel_url, custom_domain, category) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, userId);
            ps.setString(3, sub.getServiceName());
            ps.setDouble(4, sub.getCost());
            ps.setString(5, sub.getCurrency());
            ps.setDate  (6, sub.getBillingDate() != null
                ? new java.sql.Date(sub.getBillingDate().getTime())
                : null);
            ps.setString(7, sub.getBillingCycle().name());
            ps.setString(8, sub.getTier());

            if (sub instanceof PredefinedSubscription) {
                PredefinedSubscription p = (PredefinedSubscription) sub;
                ps.setString(2, TYPE_PREDEFINED);

                // service_id
                if (p.getService() != null && p.getService().getId() != null) {
                    ps.setLong(9, p.getService().getId());
                } else {
                    ps.setNull(9, Types.BIGINT);
                }

                // tier_id
                if (p.getSelectedTier() != null && p.getSelectedTier().getId() != null) {
                    ps.setLong(10, p.getSelectedTier().getId());
                } else {
                    ps.setNull(10, Types.BIGINT);
                }

                ps.setNull(11, Types.VARCHAR); // custom_cancel_url
                ps.setNull(12, Types.VARCHAR); // custom_domain
                ps.setNull(13, Types.VARCHAR); // category

            } else {
                CustomSubscription c = (CustomSubscription) sub;
                ps.setString(2, TYPE_CUSTOM);
                ps.setNull  (9,  Types.BIGINT); // service_id
                ps.setNull  (10, Types.BIGINT); // tier_id
                ps.setString(11, c.getCustomCancelUrl());
                ps.setString(12, c.getCustomDomain());
                ps.setString(13, c.getCategory());
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long generatedId = rs.getLong("id");
                System.out.println("[SubscriptionDAO] Langganan disimpan, id=" +
                    generatedId);
                return generatedId;
            }

        } catch (SQLException e) {
            System.err.println("[SubscriptionDAO] Error save: " +
                e.getMessage());
        }

        return -1L;
    }

    /**
     * Hapus langganan dari database berdasarkan id.
     *
     * @param subscriptionId id langganan yang akan dihapus
     * @return true jika berhasil dihapus
     */
    public static boolean delete(long subscriptionId) {
        String sql = "DELETE FROM subscriptions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, subscriptionId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("[SubscriptionDAO] Langganan id=" +
                    subscriptionId + " berhasil dihapus.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[SubscriptionDAO] Error delete: " +
                e.getMessage());
        }

        return false;
    }

    // ── Private Helpers ───────────────────────────────────

    /**
     * Bangun objek PredefinedSubscription dari ResultSet.
     *
     * @param rs ResultSet yang sudah diarahkan ke baris yang sesuai
     * @return PredefinedSubscription
     */
    private static PredefinedSubscription buildPredefined(ResultSet rs)
            throws SQLException {

        // Bangun Service (bisa null jika service dihapus dari katalog)
        Service service = null;
        long serviceId = rs.getLong("service_id");
        if (!rs.wasNull()) {
            service = new Service();
            service.setId(serviceId);
            service.setName(rs.getString("svc_name"));
            service.setDomain(rs.getString("svc_domain"));
            service.setCancellationUrl(rs.getString("cancellation_url"));
            service.setCategory(rs.getString("svc_category"));
            service.setDefaultCurrency(rs.getString("default_currency"));
        }

        // Bangun ServiceTier (bisa null)
        ServiceTier tier = null;
        long tierId = rs.getLong("tier_id");
        if (!rs.wasNull()) {
            tier = new ServiceTier();
            tier.setId(tierId);
            tier.setTierName(rs.getString("tier_name"));
            tier.setDescription(rs.getString("tier_desc"));
        }

        PredefinedSubscription sub = new PredefinedSubscription();
        sub.setId(rs.getLong("id"));
        sub.setServiceName(rs.getString("service_name"));
        sub.setCost(rs.getDouble("cost"));
        sub.setCurrency(rs.getString("currency"));
        sub.setBillingDate(rs.getDate("billing_date"));
        sub.setBillingCycle(parseCycle(rs.getString("billing_cycle")));
        sub.setTier(rs.getString("tier"));
        sub.setService(service);
        sub.setSelectedTier(tier);

        return sub;
    }

    /**
     * Bangun objek CustomSubscription dari ResultSet.
     *
     * @param rs ResultSet yang sudah diarahkan ke baris yang sesuai
     * @return CustomSubscription
     */
    private static CustomSubscription buildCustom(ResultSet rs)
            throws SQLException {

        CustomSubscription sub = new CustomSubscription();
        sub.setId(rs.getLong("id"));
        sub.setServiceName(rs.getString("service_name"));
        sub.setCost(rs.getDouble("cost"));
        sub.setCurrency(rs.getString("currency"));
        sub.setBillingDate(rs.getDate("billing_date"));
        sub.setBillingCycle(parseCycle(rs.getString("billing_cycle")));
        sub.setTier(rs.getString("tier"));
        sub.setCustomCancelUrl(rs.getString("custom_cancel_url"));
        sub.setCustomDomain(rs.getString("custom_domain"));
        sub.setCategory(rs.getString("category"));

        return sub;
    }

    /**
     * Parse string billing cycle dari DB ke enum BillingCycle.
     * Default ke MONTHLY jika tidak dikenali.
     *
     * @param value string dari kolom billing_cycle di DB
     * @return BillingCycle enum
     */
    private static BillingCycle parseCycle(String value) {
        if (value == null) return BillingCycle.MONTHLY;
        try {
            return BillingCycle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[SubscriptionDAO] Billing cycle tidak dikenal: "
                + value + ", default ke MONTHLY");
            return BillingCycle.MONTHLY;
        }
    }
}