package com.subsmanager.coin;

/**
 * Class PaymentProcessor menangani proses pembayaran dummy.
 * Semua transaksi auto-approve karena ini simulasi.
 *
 * Relasi: CoinService depends on PaymentProcessor (Dependency)
 */
public class PaymentProcessor {

    // ── Fields ───────────────────────────────────────────
    private String processorName;
    private boolean isOnline;

    // ── Constructor ──────────────────────────────────────
    public PaymentProcessor() {
        this.processorName = "Dummy Payment Gateway";
        this.isOnline = true;
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Memproses pembayaran transaksi secara dummy.
     * Selalu mengembalikan SUCCESS karena simulasi.
     */
    public TransactionStatus processPayment(
            CoinTransaction transaction) {
        System.out.println("=================================");
        System.out.println("Memproses pembayaran...");
        System.out.println("Kode     : " +
            transaction.getTransactionCode());
        System.out.println("Metode   : " +
            transaction.getPaymentMethod().getLabel());
        System.out.println("Nominal  : Rp " +
            String.format("%,d", transaction.getPrice()));

        // Simulasi delay proses pembayaran
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Auto-approve karena dummy
        transaction.updateStatus(TransactionStatus.SUCCESS);
        System.out.println("Pembayaran berhasil diproses!");
        System.out.println("=================================");

        return TransactionStatus.SUCCESS;
    }

    /**
     * Menampilkan instruksi pembayaran berdasarkan
     * metode yang dipilih user.
     */
    public void displayPaymentInstructions(
            CoinTransaction transaction) {
        PaymentMethod method = transaction.getPaymentMethod();
        System.out.println("=================================");
        System.out.println("INSTRUKSI PEMBAYARAN");
        System.out.println("Paket    : " +
            transaction.getCoinPackage().getName());
        System.out.println("Nominal  : Rp " +
            String.format("%,d", transaction.getPrice()));
        System.out.println("---------------------------------");

        switch (method.getType()) {
            case "BANK_TRANSFER":
                System.out.println("Transfer ke rekening:");
                System.out.println("Bank     : " +
                    method.getLabel());
                System.out.println("No. Rek  : " +
                    method.getPaymentNumber());
                System.out.println("A/N      : Subscription Manager");
                System.out.println("Kode Unik: " +
                    transaction.getTransactionCode());
                break;

            case "E_WALLET":
                System.out.println("Transfer ke e-wallet:");
                System.out.println("Platform : " +
                    method.getLabel());
                System.out.println("No. Tujuan: " +
                    method.getPaymentNumber());
                System.out.println("Nominal  : Rp " +
                    String.format("%,d", transaction.getPrice()));
                break;

            case "QRIS":
                System.out.println("Scan QRIS berikut:");
                System.out.println("QRIS ID  : " +
                    method.getPaymentNumber());
                System.out.println("(Di frontend akan " +
                    "ditampilkan gambar QR statis)");
                break;
        }
        System.out.println("=================================");
    }

    /**
     * Mengecek apakah payment gateway sedang online.
     * Selalu true karena dummy.
     */
    public boolean checkGatewayStatus() {
        System.out.println(processorName +
            " status: " + (isOnline ? "Online" : "Offline"));
        return isOnline;
    }

    // ── Getters ──────────────────────────────────────────
    public String getProcessorName() { return processorName; }
    public boolean isOnline() { return isOnline; }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "PaymentProcessor{" +
               "processorName='" + processorName + "'" +
               ", isOnline=" + isOnline + "}";
    }
}