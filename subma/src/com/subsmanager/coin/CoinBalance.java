package com.subsmanager.coin;

import com.subsmanager.auth.User;
import java.time.LocalDateTime;

/**
 * Class CoinBalance menyimpan saldo coin milik user.
 * Setiap user memiliki tepat satu CoinBalance.
 *
 * Relasi: User aggregates CoinBalance (Agregasi one-to-one)
 */
public class CoinBalance {

    // ── Fields ───────────────────────────────────────────
    private Long id;
    private User user;
    private int balance;
    private LocalDateTime updatedAt;

    // ── Constructor ──────────────────────────────────────
    public CoinBalance(Long id, User user) {
        this.id = id;
        this.user = user;
        this.balance = 0; // saldo awal selalu 0
        this.updatedAt = LocalDateTime.now();
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Menambahkan coin ke saldo.
     * Dipanggil saat user berhasil membeli coin.
     */
    public void addCoins(int amount) {
        if (amount <= 0) {
            System.out.println("Jumlah coin tidak valid.");
            return;
        }
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
        System.out.println(amount + " coin berhasil" +
            " ditambahkan. Saldo sekarang: " +
            balance + " coin");
    }

    /**
     * Mengurangi coin dari saldo.
     * Dipanggil saat user menggunakan fitur premium.
     */
    public void deductCoins(int amount) {
        if (amount <= 0) {
            System.out.println("Jumlah coin tidak valid.");
            return;
        }
        if (amount > balance) {
            System.out.println("Saldo coin tidak cukup." +
                " Saldo: " + balance +
                " coin, dibutuhkan: " + amount + " coin");
            return;
        }
        this.balance -= amount;
        this.updatedAt = LocalDateTime.now();
        System.out.println(amount + " coin berhasil" +
            " digunakan. Saldo sekarang: " +
            balance + " coin");
    }

    /**
     * Mengecek apakah saldo mencukupi
     * untuk jumlah coin tertentu.
     */
    public boolean hasSufficientBalance(int amount) {
        return this.balance >= amount;
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public User getUser() { return user; }
    public int getBalance() { return balance; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "CoinBalance{" +
               "user='" + user.getEmail() + "'" +
               ", balance=" + balance + " coin" +
               ", updatedAt=" + updatedAt + "}";
    }
}