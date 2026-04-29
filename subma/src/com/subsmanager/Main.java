package com.subsmanager;

import com.subsmanager.auth.Admin;
import com.subsmanager.auth.User;
import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;
import com.subsmanager.coin.CoinService;
import com.subsmanager.coin.PaymentMethod;
import com.subsmanager.currency.CurrencyConverter;
import com.subsmanager.financial.FinancialSummary;
import com.subsmanager.manager.SubscriptionManager;
import com.subsmanager.overlay.OverlayController;
import com.subsmanager.subscription.model.BillingCycle;
import com.subsmanager.subscription.model.CustomSubscription;
import com.subsmanager.subscription.model.PredefinedSubscription;

import java.util.Date;

/**
 * Main - Entry point utama aplikasi Subscription Manager
 *
 * Pilihan mode:
 * - Mode GUI  : launch JavaFX via MainApp (default)
 * - Mode Demo : jalankan console demo OOP (untuk testing)
 *
 * Untuk menjalankan mode demo, ubah:
 *   LAUNCH_GUI = false
 */
public class Main {

    /**
     * Ganti ke false untuk menjalankan console demo OOP
     * Ganti ke true untuk menjalankan aplikasi JavaFX GUI
     */
    private static final boolean LAUNCH_GUI = true;

    /**
     * Entry point JVM.
     * Memilih antara mode GUI atau mode demo console.
     *
     * @param args argumen command line
     */
    public static void main(String[] args) {
        if (LAUNCH_GUI) {
            // ── Mode GUI: launch JavaFX ──────────────────
            System.out.println(
                "[Main] Menjalankan mode GUI (JavaFX)...");
            MainApp.main(args);
        } else {
            // ── Mode Demo: console OOP demo ──────────────
            System.out.println(
                "[Main] Menjalankan mode Demo (Console)...");
            jalankanDemo();
        }
    }

    /**
     * Demo console lengkap untuk membuktikan konsep OOP.
     * Dipanggil saat LAUNCH_GUI = false.
     *
     * Demonstrasi:
     * 1. Inheritance   : Admin extends User
     * 2. Komposisi     : Service punya ServiceTier
     * 3. Polymorphism  : getCancelPageURL(), getIconUrl()
     * 4. Agregasi      : User punya Subscription
     * 5. Dependency    : SubscriptionManager, OverlayController
     * 6. Currency      : CurrencyConverter (Frankfurter API)
     * 7. Financial     : FinancialSummary
     * 8. Detail Sub    : printAllSubscriptions()
     * 9. Coin System   : CoinService, PaymentProcessor
     */
    private static void jalankanDemo() {

        System.out.println(
            "╔══════════════════════════════════╗");
        System.out.println(
            "║     SUBSCRIPTION MANAGER DEMO    ║");
        System.out.println(
            "╚══════════════════════════════════╝");
        System.out.println();

        // ── 1. Demo Inheritance ──────────────────────────
        System.out.println(">>> 1. DEMO INHERITANCE");
        System.out.println("--------------------------------");

        User user   = new User(1L, "budi@email.com", "pass123");
        Admin admin = new Admin(
            2L, "admin@email.com", "adminpass", "SUPER");

        System.out.println("User  : " + user);
        System.out.println("Admin : " + admin);
        System.out.println("User login  : " +
            user.login("pass123"));
        System.out.println("Admin login : " +
            admin.login("adminpass"));
        System.out.println();

        // ── 2. Demo Komposisi ────────────────────────────
        System.out.println(">>> 2. DEMO KOMPOSISI");
        System.out.println("--------------------------------");

        Service netflix = new Service(
            1L, "Netflix", "netflix.com",
            "https://www.netflix.com/cancelplan",
            "Streaming", "USD"
        );

        ServiceTier personalTier = new ServiceTier(
            1L, "Personal", "1 layar, Full HD");
        ServiceTier familyTier = new ServiceTier(
            2L, "Family", "4 layar, Ultra HD");

        admin.addServiceTier(netflix, personalTier);
        admin.addServiceTier(netflix, familyTier);

        System.out.println("Service : " + netflix);
        System.out.println("Tier tersedia : " +
            netflix.getAvailableTiers().size() + " tier");
        System.out.println();

        // ── 3. Demo Polymorphism ─────────────────────────
        System.out.println(">>> 3. DEMO POLYMORPHISM");
        System.out.println("--------------------------------");

        PredefinedSubscription netflixSub =
            new PredefinedSubscription(
                1L, 15.99, "USD", new Date(),
                BillingCycle.MONTHLY, "Personal",
                netflix, personalTier
            );

        CustomSubscription gymSub = new CustomSubscription(
            2L, "Gym Lokal", 150000, "IDR", new Date(),
            BillingCycle.MONTHLY, "Basic",
            null, "gymlokal.com", "Kesehatan"
        );

        System.out.println("Cancel URL Netflix : " +
            netflixSub.getCancelPageURL());
        System.out.println("Cancel URL Gym     : " +
            gymSub.getCancelPageURL());
        System.out.println("Icon URL Netflix   : " +
            netflixSub.getIconUrl());
        System.out.println("Icon URL Gym       : " +
            gymSub.getIconUrl());
        System.out.println();

        // ── 4. Demo Agregasi ─────────────────────────────
        System.out.println(">>> 4. DEMO AGREGASI");
        System.out.println("--------------------------------");

        user.addSubscription(netflixSub);
        user.addSubscription(gymSub);
        System.out.println("Jumlah subscription " +
            user.getEmail() + " : " +
            user.getSubscriptions().size());
        System.out.println();

        // ── 5. Demo Dependency ───────────────────────────
        System.out.println(">>> 5. DEMO DEPENDENCY");
        System.out.println("--------------------------------");

        OverlayController overlay =
            new OverlayController(1280, 720);
        SubscriptionManager manager =
            new SubscriptionManager(overlay);

        manager.loadSubscriptions(user);
        System.out.println();
        manager.triggerCancelProcess(netflixSub);
        System.out.println();

        // ── 6. Demo Currency Converter ───────────────────
        System.out.println(">>> 6. DEMO CURRENCY CONVERTER");
        System.out.println("--------------------------------");

        CurrencyConverter converter = new CurrencyConverter();
        System.out.println("Kurs USD ke IDR : " +
            converter.formatIDR(converter.getRate()));
        System.out.println("15.99 USD = " +
            converter.formatIDR(
                converter.convert(15.99, "USD")));
        System.out.println();

        // ── 7. Demo Financial Summary ────────────────────
        System.out.println(">>> 7. DEMO FINANCIAL SUMMARY");
        System.out.println("--------------------------------");

        FinancialSummary financial =
            new FinancialSummary(manager, converter);
        financial.printSummary();
        System.out.println();

        // ── 8. Demo Semua Subscription ───────────────────
        System.out.println(">>> 8. DETAIL SEMUA SUBSCRIPTION");
        System.out.println("--------------------------------");
        manager.printAllSubscriptions(converter.getRate());

        // ── 9. Demo Coin System ──────────────────────────
        System.out.println(">>> 9. DEMO COIN SYSTEM");
        System.out.println("--------------------------------");

        CoinService coinService = new CoinService();

        coinService.showAvailablePackages();
        System.out.println();

        System.out.println("Saldo awal:");
        coinService.printBalance(user);
        System.out.println();

        System.out.println("-- Membeli Paket Regular via GoPay --");
        coinService.purchaseCoin(user, 2L, PaymentMethod.GOPAY);
        System.out.println();

        System.out.println("Saldo setelah pembelian:");
        coinService.printBalance(user);
        System.out.println();

        System.out.println("-- Menggunakan Fitur Export PDF --");
        coinService.useCoins(user, 10, "Export PDF");
        System.out.println();

        System.out.println("-- Menggunakan Fitur Reminder 1 Bulan --");
        coinService.useCoins(user, 20, "Reminder 1 Bulan");
        System.out.println();

        System.out.println("Saldo setelah penggunaan fitur:");
        coinService.printBalance(user);
        System.out.println();

        System.out.println(
            "-- Mencoba Fitur saat Saldo Tidak Cukup --");
        coinService.useCoins(user, 350, "Export Excel");
        System.out.println();

        coinService.printTransactionHistory(user);

        System.out.println();
        System.out.println(
            "╔══════════════════════════════════╗");
        System.out.println(
            "║           DEMO SELESAI           ║");
        System.out.println(
            "╚══════════════════════════════════╝");
    }
}