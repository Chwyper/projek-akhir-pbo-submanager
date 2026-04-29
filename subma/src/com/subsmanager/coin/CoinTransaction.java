package com.subsmanager.coin;

import com.subsmanager.auth.User;
import java.time.LocalDateTime;

/**
 * Class CoinTransaction menyimpan riwayat setiap
 * transaksi coin yang dilakukan user.
*/

public class CoinTransaction {

    // ── Fields ───────────────────────────────────────────
    private Long id;
    private User user;
    private TransactionType type;
    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private CoinPackage coinPackage;
    private int coinAmount;
    private int price;
    private String description;
    private String transactionCode;
    private LocalDateTime createdAt;

    // ── Constructor untuk PURCHASE ───────────────────────
    public CoinTransaction(Long id, User user,
                           CoinPackage coinPackage,
                           PaymentMethod paymentMethod) {
        this.id = id;
        this.user = user;
        this.coinPackage = coinPackage;
        this.paymentMethod = paymentMethod;
        this.type = TransactionType.PURCHASE;
        this.status = TransactionStatus.PENDING;
        this.coinAmount = coinPackage.getCoinAmount();
        this.price = coinPackage.getPrice();
        this.description = "Pembelian paket " +
            coinPackage.getName();
        this.transactionCode = generateTransactionCode();
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructor untuk USAGE ──────────────────────────
    public CoinTransaction(Long id, User user,
                           int coinAmount,
                           String description) {
        this.id = id;
        this.user = user;
        this.type = TransactionType.USAGE;
        this.status = TransactionStatus.SUCCESS;
        this.coinAmount = coinAmount;
        this.price = 0;
        this.description = description;
        this.transactionCode = generateTransactionCode();
        this.createdAt = LocalDateTime.now();
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Membuat kode unik transaksi
     * Format: TRX-YYYYMMDD-HHMMSS
     */
    private String generateTransactionCode() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("TRX-%d%02d%02d-%02d%02d%02d",
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getHour(),
            now.getMinute(),
            now.getSecond()
        );
    }

    /**
     * Menampilkan detail transaksi
     */
    public void printDetail() {
        System.out.println("=================================");
        System.out.println("Kode Transaksi : " + transactionCode);
        System.out.println("Tipe           : " + type.getLabel());
        System.out.println("Status         : " + status.getLabel());
        System.out.println("Deskripsi      : " + description);
        System.out.println("Jumlah Coin    : " + coinAmount);
        if (type == TransactionType.PURCHASE) {
            System.out.println("Harga          : Rp " +
                String.format("%,d", price));
            System.out.println("Metode Bayar   : " +
                paymentMethod.getLabel());
            System.out.println("No. Tujuan     : " +
                paymentMethod.getPaymentNumber());
        }
        System.out.println("Waktu          : " + createdAt);
        System.out.println("=================================");
    }

    /**
     * Memperbarui status transaksi
     * Dipanggil oleh PaymentProcessor
     */
    public void updateStatus(TransactionStatus newStatus) {
        this.status = newStatus;
        System.out.println("Status transaksi " +
            transactionCode + " diperbarui menjadi: " +
            newStatus.getLabel());
    }

    // ── Getters ──────────────────────────────────────────
    public Long getId() { return id; }
    public User getUser() { return user; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public CoinPackage getCoinPackage() { return coinPackage; }
    public int getCoinAmount() { return coinAmount; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public String getTransactionCode() { return transactionCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "CoinTransaction{" +
               "code='" + transactionCode + "'" +
               ", type=" + type.getLabel() +
               ", status=" + status.getLabel() +
               ", coinAmount=" + coinAmount +
               ", description='" + description + "'}";
    }
}