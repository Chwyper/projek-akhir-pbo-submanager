package com.subsmanager.coin;

import com.subsmanager.auth.User;
import com.subsmanager.db.CoinDAO;
import com.subsmanager.db.UserDAO;
import java.util.ArrayList;
import java.util.List;

/**
 * Class CoinService sebagai koordinator utama
 * sistem coin. Mengelola pembelian coin, penggunaan
 * fitur premium, dan riwayat transaksi.
 *
 * Relasi: CoinService depends on PaymentProcessor (Dependency)
 *         CoinService depends on CoinBalance (Dependency)
 *         User aggregates CoinTransaction (Agregasi)
 */
public class CoinService {

    // ── Fields ───────────────────────────────────────────
    private PaymentProcessor paymentProcessor;
    private List<CoinTransaction> transactionHistory;
    private List<CoinPackage> availablePackages;
    private long transactionCounter;
    private CoinTransaction lastTransaction; // ← tambahkan ini

    // ── Constructor ──────────────────────────────────────
    public CoinService() {
        this.paymentProcessor = new PaymentProcessor();
        this.transactionHistory = new ArrayList<>();
        this.availablePackages = new ArrayList<>();
        this.transactionCounter = 1;
        initializePackages();
    }

    // ── Package Initialization ────────────────────────────

    /**
     * Menginisialisasi paket coin yang tersedia
     */
    private void initializePackages() {
        availablePackages.add(new CoinPackage(
            1L, "Starter", 50, 10000, "IDR"
        ));
        availablePackages.add(new CoinPackage(
            2L, "Regular", 150, 25000, "IDR"
        ));
        availablePackages.add(new CoinPackage(
            3L, "Pro", 350, 50000, "IDR"
        ));
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Menampilkan semua paket coin yang tersedia
     */
    public void showAvailablePackages() {
        System.out.println("===== PAKET COIN TERSEDIA =====");
        for (CoinPackage pkg : availablePackages) {
            System.out.println(pkg.getDescription() +
                " (Rp " + String.format("%.0f",
                pkg.getPricePerCoin()) + "/coin)");
        }
        System.out.println("================================");
    }

    /**
     * Proses pembelian coin oleh user.
     * Dependency: menggunakan PaymentProcessor
     * untuk approval pembayaran.
     */
    public boolean purchaseCoin(User user,
                                 Long packageId,
                                 PaymentMethod method) {
        // Cari paket yang dipilih
        CoinPackage selectedPackage = findPackageById(packageId);
        if (selectedPackage == null) {
            System.out.println("Paket tidak ditemukan.");
            return false;
        }

        // Buat transaksi baru
        CoinTransaction transaction = new CoinTransaction(
            transactionCounter++,
            user,
            selectedPackage,
            method
        );

        // Tampilkan instruksi pembayaran
        paymentProcessor.displayPaymentInstructions(transaction);

        // Proses pembayaran (auto-approve karena dummy)
        TransactionStatus status =
            paymentProcessor.processPayment(transaction);

        // Jika berhasil tambahkan coin ke saldo user
        if (status == TransactionStatus.SUCCESS) {
            user.getCoinBalance().addCoins(
                selectedPackage.getCoinAmount()
            );
            transactionHistory.add(transaction);
            this.lastTransaction = transaction;

            // Simpan transaksi dan saldo ke DB
            CoinDAO.savePurchase(transaction);
            UserDAO.updateCoinBalance(user);

            System.out.println("Pembelian berhasil! " +
                selectedPackage.getCoinAmount() +
                " coin telah ditambahkan ke akun " +
                user.getEmail());
            return true;
        }

        System.out.println("Pembelian gagal.");
        return false;
    }
	    public CoinTransaction getLastTransaction() {
	        return lastTransaction;
	    }
    /**
     * Menggunakan coin untuk fitur premium.
     * Mengecek saldo sebelum memproses.
     */
    public boolean useCoins(User user,
                             int amount,
                             String featureName) {
        CoinBalance balance = user.getCoinBalance();

        // Cek apakah saldo mencukupi
        if (!balance.hasSufficientBalance(amount)) {
            System.out.println("Saldo coin tidak cukup" +
                " untuk fitur " + featureName + ".");
            System.out.println("Saldo saat ini : " +
                balance.getBalance() + " coin");
            System.out.println("Dibutuhkan     : " +
                amount + " coin");
            System.out.println("Silakan beli coin terlebih dahulu.");
            return false;
        }

        // Kurangi saldo
        balance.deductCoins(amount);

        // Catat transaksi penggunaan
        // Konstruktor ini otomatis generate transactionCode
        CoinTransaction usageTransaction =
            new CoinTransaction(
                transactionCounter++,
                user,
                amount,
                "Penggunaan fitur: " + featureName
            );
        transactionHistory.add(usageTransaction);

        // Simpan transaksi dan saldo ke DB
        CoinDAO.saveUsage(usageTransaction);
        UserDAO.updateCoinBalance(user);

        System.out.println("Fitur " + featureName +
            " berhasil diakses!");
        return true;
    }

    /**
     * Menampilkan riwayat transaksi milik user
     */
    public void printTransactionHistory(User user) {
        System.out.println("===== RIWAYAT TRANSAKSI =====");
        System.out.println("User: " + user.getEmail());
        System.out.println("-----------------------------");

        boolean found = false;
        for (CoinTransaction t : transactionHistory) {
            if (t.getUser().getId().equals(user.getId())) {
                t.printDetail();
                found = true;
            }
        }

        if (!found) {
            System.out.println("Belum ada transaksi.");
        }
        System.out.println("=============================");
    }

    /**
     * Mencari paket coin berdasarkan id
     */
    public CoinPackage findPackageById(Long id) {
        for (CoinPackage pkg : availablePackages) {
            if (pkg.getId().equals(id)) {
                return pkg;
            }
        }
        return null;
    }

    /**
     * Menampilkan saldo coin user
     */
    public void printBalance(User user) {
        System.out.println("Saldo coin " +
            user.getEmail() + ": " +
            user.getCoinBalance().getBalance() + " coin");
    }

    // ── Getters ──────────────────────────────────────────
    public List<CoinPackage> getAvailablePackages() {
        return availablePackages;
    }

    public List<CoinTransaction> getTransactionHistory() {
        return transactionHistory;
    }
    
    public CoinPackage getPackageById(long id) {
        return findPackageById(id);
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "CoinService{" +
               "totalPackages=" + availablePackages.size() +
               ", totalTransactions=" +
               transactionHistory.size() + "}";
    }
}