package com.subsmanager.auth;

import com.subsmanager.coin.CoinBalance;
import com.subsmanager.subscription.model.Subscription;
import java.util.ArrayList;
import java.util.List;
/**
 * Class User merepresentasikan pengguna website.
 * Menyimpan data akun dan daftar subscription milik pengguna.
 */
public class User {

    // ── Fields ───────────────────────────────────────────
    private Long id;
    private String email;
    private String password;
    private List<Subscription> subscriptionList;
    private CoinBalance coinBalance;
    

    // ── Constructor ──────────────────────────────────────
    public User() {
        this.subscriptionList = new ArrayList<>();
        this.coinBalance = new CoinBalance(0L, this);
        this.id = 0L;
    }
    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.subscriptionList = new ArrayList<>();
        this.coinBalance = new CoinBalance(id, this);
    }
    

    	public boolean login(String inputPassword) {
    		return this.password.equals(inputPassword);
    	}

    /**
     * Mengakhiri sesi pengguna
     */
    public void logout() {
        System.out.println("User " + email + " telah logout.");
    }

    /**
     * Memperbarui email pengguna
     */
    public void updateEmail(String newEmail) {
        this.email = newEmail;
        System.out.println("Email berhasil diperbarui menjadi: " + newEmail);
    }

    /**
     * Memperbarui password pengguna
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        System.out.println("Password berhasil diperbarui.");
    }

    // ── Subscription Methods ─────────────────────────────

    /**
     * Menambahkan subscription ke daftar milik user
     */
    public void addSubscription(Subscription subscription) {
        subscriptionList.add(subscription);
        System.out.println("Subscription " + 
            subscription.getServiceName() + " berhasil ditambahkan.");
    }

    
    public void removeSubscription(Subscription subscription) {
        subscriptionList.remove(subscription);
        System.out.println("Subscription " + 
            subscription.getServiceName() + " berhasil dihapus.");
    }
    
    public void removeSubscription(Long id) {
        subscriptionList.removeIf(s -> s.getId().equals(id));
    }

    
    public List<Subscription> getSubscriptions() {
        return subscriptionList;
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public CoinBalance getCoinBalance() { return coinBalance; }
    public int getCoinAmount() {
        return coinBalance != null ? coinBalance.getBalance() : 0;
    }
    public void deductCoins(int amount) {
        if (coinBalance != null) coinBalance.deductCoins(amount);
    }
    public void setCoinBalance(CoinBalance coinBalance) {
        this.coinBalance = coinBalance;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "User{id=" + id +
               ", email='" + email + "'" +
               ", jumlah subscription=" + subscriptionList.size() +
               ", saldo coin=" + coinBalance.getBalance() + "}";
    }
}
