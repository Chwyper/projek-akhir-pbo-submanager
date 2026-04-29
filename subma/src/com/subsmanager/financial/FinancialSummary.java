package com.subsmanager.financial;

import com.subsmanager.currency.CurrencyConverter;
import com.subsmanager.manager.SubscriptionManager;
import com.subsmanager.subscription.model.BillingCycle;
import com.subsmanager.subscription.model.Subscription;
import java.util.LinkedHashMap;

import java.util.Map;

public class FinancialSummary {

    // ── Fields ───────────────────────────────────────────
    private SubscriptionManager manager;
    private CurrencyConverter converter;

    // ── Constructor ──────────────────────────────────────
    public FinancialSummary(SubscriptionManager manager,
                             CurrencyConverter converter) {
        this.manager = manager;
        this.converter = converter;
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Menghitung total pengeluaran per bulan dalam IDR.
     * Subscription yearly dibagi 12 untuk dapat biaya per bulan.
     */
    public double getMonthlyOutcome() {
        double total = 0;
        double rate = converter.getRate();
        for (Subscription s : manager.getSubscriptionList()) {
            total += s.getMonthlyCostInIDR(rate);
        }
        return total;
    }

    /**
     * Menghitung proyeksi pengeluaran per tahun dalam IDR.
     * Subscription yearly dihitung sekali penuh, bukan dikali 12.
     */
    public double getYearlyProjection() {
        double total = 0;
        double rate = converter.getRate();
        for (Subscription s : manager.getSubscriptionList()) {
            if (s.getBillingCycle() == BillingCycle.YEARLY) {
                total += s.getCostInIDR(rate);
            } else {
                total += s.getCostInIDR(rate) * 12;
            }
        }
        return total;
    }

    /**
     * Menghitung breakdown pengeluaran per bulan
     * selama 12 bulan ke depan.
     * Subscription yearly hanya muncul di bulan billing-nya.
     */
    public Map<String, Double> getMonthlyBreakdown() {
        String[] months = {
            "Januari", "Februari", "Maret", "April",
            "Mei", "Juni", "Juli", "Agustus",
            "September", "Oktober", "November", "Desember"
        };
        Map<String, Double> breakdown = new LinkedHashMap<>();
        double monthlyTotal = getMonthlyOutcome();

        for (String month : months) {
            breakdown.put(month, monthlyTotal);
        }
        return breakdown;
    }

    /**
     * Menghitung kontribusi setiap subscription
     * terhadap total pengeluaran bulanan dalam persen.
     */
    public Map<String, Double> getPerSubscriptionBreakdown() {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        double rate = converter.getRate();
        double monthlyTotal = getMonthlyOutcome();

        if (monthlyTotal == 0) return breakdown;

        for (Subscription s : manager.getSubscriptionList()) {
            double cost = s.getMonthlyCostInIDR(rate);
            double percentage = (cost / monthlyTotal) * 100;
            breakdown.put(s.getServiceName(), percentage);
        }
        return breakdown;
    }

    /**
     * Menampilkan ringkasan keuangan lengkap ke console
     */
    public void printSummary() {
        System.out.println("====== RINGKASAN KEUANGAN ======");
        System.out.println("Kurs saat ini  : " + 
            converter.formatIDR(converter.getRate()) + " / USD");
        System.out.println("Total Bulanan  : " + 
            converter.formatIDR(getMonthlyOutcome()));
        System.out.println("Proyeksi Tahun : " + 
            converter.formatIDR(getYearlyProjection()));
        System.out.println("");
        System.out.println("-- Breakdown per Subscription --");

        Map<String, Double> perSub = getPerSubscriptionBreakdown();
        for (Map.Entry<String, Double> entry : perSub.entrySet()) {
            System.out.printf("%-20s : %.1f%%%n",
                entry.getKey(), entry.getValue());
        }

        System.out.println("");
        System.out.println("-- Proyeksi per Bulan --");
        Map<String, Double> monthly = getMonthlyBreakdown();
        for (Map.Entry<String, Double> entry : monthly.entrySet()) {
            System.out.printf("%-10s : %s%n",
                entry.getKey(),
                converter.formatIDR(entry.getValue()));
        }
        System.out.println("================================");
    }

    // ── Getters ──────────────────────────────────────────
    public SubscriptionManager getManager() { return manager; }
    public CurrencyConverter getConverter() { return converter; }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "FinancialSummary{" +
               "monthlyOutcome=" + 
               converter.formatIDR(getMonthlyOutcome()) +
               ", yearlyProjection=" + 
               converter.formatIDR(getYearlyProjection()) + "}";
    }
}