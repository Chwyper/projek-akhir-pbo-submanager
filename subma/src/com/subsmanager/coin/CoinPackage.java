package com.subsmanager.coin;

/**
 * Class CoinPackage merepresentasikan paket pembelian coin.
 * Menyimpan informasi nama paket, jumlah coin, dan harga.
 *
 * Relasi: CoinTransaction uses CoinPackage (Asosiasi)
 */

public class CoinPackage {

    // ── Fields ───────────────────────────────────────────
    private Long id;
    private String name;
    private int coinAmount;
    private int price;
    private String currency;

    // ── Constructor ──────────────────────────────────────
    public CoinPackage(Long id, String name,
                       int coinAmount, int price,
                       String currency) {
        this.id = id;
        this.name = name;
        this.coinAmount = coinAmount;
        this.price = price;
        this.currency = currency;
    }

    // ── Methods ──────────────────────────────────────────

    /**
     * Mengembalikan harga per coin dalam paket ini
     * Berguna untuk menampilkan value proposition
     */
    public double getPricePerCoin() {
        return (double) price / coinAmount;
    }

    /**
     * Mengembalikan deskripsi paket
     */
    public String getDescription() {
        return name + " - " + coinAmount +
               " coin seharga Rp " +
               String.format("%,d", price);
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getCoinAmount() { return coinAmount; }
    public int getPrice() { return price; }
    public String getCurrency() { return currency; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCoinAmount(int coinAmount) {
        this.coinAmount = coinAmount;
    }
    public void setPrice(int price) { this.price = price; }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "CoinPackage{" +
               "name='" + name + "'" +
               ", coinAmount=" + coinAmount +
               ", price=Rp " + String.format("%,d", price) +
               ", pricePerCoin=Rp " +
               String.format("%.0f", getPricePerCoin()) + "}";
    }
}