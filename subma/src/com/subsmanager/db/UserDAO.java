package com.subsmanager.db;

import com.subsmanager.auth.User;
import com.subsmanager.coin.CoinBalance;

import java.sql.*;

/**
 * UserDAO - Data Access Object untuk tabel users dan coin_balances.
 *
 * Operasi yang tersedia:
 * - login(email, password)    → autentikasi user
 * - register(email, password) → daftarkan user baru
 * - updateCoinBalance(user)   → simpan perubahan saldo koin
 * - loadCoinBalance(user)     → ambil saldo koin dari DB
 */
public class UserDAO {

    /**
     * Autentikasi user berdasarkan email dan password.
     * TODO: ganti password plaintext dengan bcrypt di production.
     *
     * @param email    email yang diinput
     * @param password password yang diinput
     * @return User jika berhasil, null jika gagal
     */
    public static User login(String email, String password) {
        String sql =
            "SELECT u.id, u.email, u.password, " +
            "       COALESCE(cb.balance, 0) AS coin_balance " +
            "FROM users u " +
            "LEFT JOIN coin_balances cb ON cb.user_id = u.id " +
            "WHERE u.email = ? AND u.password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));

                // Set saldo koin
                CoinBalance cb = new CoinBalance(
                    user.getId(), user);
                int coinBalance = rs.getInt("coin_balance");
                if (coinBalance > 0) cb.addCoins(coinBalance);
                user.setCoinBalance(cb);

                System.out.println(
                    "[UserDAO] Login berhasil: " + email);
                return user;
            }

            System.out.println(
                "[UserDAO] Login gagal: email/password salah");
            return null;

        } catch (SQLException e) {
            System.err.println(
                "[UserDAO] Error login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Daftarkan user baru ke database.
     * Otomatis buat baris coin_balances dengan saldo 0.
     *
     * @param email    email user baru
     * @param password password user baru
     * @return User yang baru dibuat, null jika gagal
     */
    public static User register(String email, String password) {
        // Cek apakah email sudah terdaftar
        if (isEmailTaken(email)) {
            System.out.println(
                "[UserDAO] Email sudah terdaftar: " + email);
            return null;
        }

        String sqlUser =
            "INSERT INTO users (email, password) " +
            "VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sqlUser)) {

            conn.setAutoCommit(false); // mulai transaksi

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long newId = rs.getLong("id");

                // Buat coin_balance untuk user baru
                String sqlCoin =
                    "INSERT INTO coin_balances (user_id, balance) " +
                    "VALUES (?, 0)";
                try (PreparedStatement psCoin =
                         conn.prepareStatement(sqlCoin)) {
                    psCoin.setLong(1, newId);
                    psCoin.executeUpdate();
                }

                conn.commit();

                // Buat objek User untuk dikembalikan
                User user = new User();
                user.setId(newId);
                user.setEmail(email);
                user.setPassword(password);
                CoinBalance cb = new CoinBalance(newId, user);
                // saldo awal 0 — constructor CoinBalance sudah set ke 0
                user.setCoinBalance(cb);

                System.out.println(
                    "[UserDAO] Register berhasil: " + email
                    + " (id=" + newId + ")");
                return user;
            }

            conn.rollback();
            return null;

        } catch (SQLException e) {
            System.err.println(
                "[UserDAO] Error register: " + e.getMessage());
            return null;
        }
    }

    /**
     * Simpan perubahan saldo koin ke database.
     * Dipanggil setelah pembelian koin atau penggunaan fitur.
     *
     * @param user user yang saldonya berubah
     */
    public static void updateCoinBalance(User user) {
        String sql =
            "UPDATE coin_balances SET balance = ?, " +
            "updated_at = NOW() WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getCoinAmount());
            ps.setLong(2, user.getId());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(
                    "[UserDAO] Saldo koin diperbarui: "
                    + user.getCoinAmount() + " koin");
            }

        } catch (SQLException e) {
            System.err.println(
                "[UserDAO] Error update koin: " + e.getMessage());
        }
    }

    /**
     * Cek apakah email sudah terdaftar di database.
     *
     * @param email email yang dicek
     * @return true jika sudah ada
     */
    public static boolean isEmailTaken(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println(
                "[UserDAO] Error cek email: " + e.getMessage());
            return false;
        }
    }
}