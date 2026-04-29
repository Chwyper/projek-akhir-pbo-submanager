package com.subsmanager.subscription.model;


import java.util.Date;



public abstract class Subscription {

    // ── Fields ───────────────────────────────────────────
    protected Long id;
    protected String serviceName;
    protected double cost;
    protected String currency;
    protected Date billingDate;
    protected BillingCycle billingCycle;
    protected String tier;

    // ── Constructor ──────────────────────────────────────
    public Subscription(Long id, String serviceName, double cost,
                        String currency, Date billingDate,
                        BillingCycle billingCycle, String tier) {
        this.id = id;
        this.serviceName = serviceName;
        this.cost = cost;
        this.currency = currency;
        this.billingDate = billingDate;
        this.billingCycle = billingCycle;
        this.tier = tier;
    }
    public Subscription() {}

    // ── Abstract Methods ─────────────────────────────────
    // Wajib diimplementasikan oleh setiap subclass

    /**
     * Mengembalikan URL halaman pembatalan layanan
     * Setiap jenis subscription punya cara berbeda mendapatkan URL ini
     */
    public abstract String getCancelPageURL();

    /**
     * Mengembalikan URL ikon layanan
     * Setiap jenis subscription punya sumber ikon yang berbeda
     */
    public abstract String getIconUrl();

    // ── Concrete Methods ─────────────────────────────────
    // Sudah ada implementasinya, tidak perlu di-override subclass

    /**
     * Menghitung biaya dalam IDR
     * Jika currency USD maka dikalikan rate, jika IDR langsung dikembalikan
     */
    public double getCostInIDR(double usdToIdrRate) {
        if (currency.equalsIgnoreCase("USD")) {
            return cost * usdToIdrRate;
        }
        return cost;
    }

    /**
     * Mengembalikan biaya per bulan dalam IDR
     * Berguna untuk kalkulasi di FinancialSummary
     */
    public double getMonthlyCostInIDR(double usdToIdrRate) {
        double costInIDR = getCostInIDR(usdToIdrRate);
        if (billingCycle == BillingCycle.YEARLY) {
            return costInIDR / 12;
        }
        return costInIDR;
    }

    /**
     * Menampilkan ringkasan subscription
     */
    public void printSummary(double usdToIdrRate) {
        System.out.println("=================================");
        System.out.println("Layanan    : " + serviceName);
        System.out.println("Tier       : " + (tier != null ? tier : "-"));
        System.out.println("Biaya      : " + cost + " " + currency);
        System.out.println("Biaya IDR  : Rp " + 
            String.format("%,.0f", getCostInIDR(usdToIdrRate)));
        System.out.println("Siklus     : " + billingCycle.getLabel());
        System.out.println("Cancel URL : " + getCancelPageURL());
        System.out.println("=================================");
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
    public String getCurrency() { return currency; }
    public Date getBillingDate() { return billingDate; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public String getTier() { return tier; }

    public void setId(Long id) { this.id = id; }
    public void setServiceName(String serviceName) { 
        this.serviceName = serviceName; 
    }
    public void setCost(double cost) { this.cost = cost; }
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }
    public void setBillingDate(Date billingDate) { 
        this.billingDate = billingDate; 
    }
    public void setBillingCycle(BillingCycle billingCycle) { 
        this.billingCycle = billingCycle; 
    }
    public void setTier(String tier) { this.tier = tier; }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "Subscription{id=" + id +
               ", serviceName='" + serviceName + "'" +
               ", cost=" + cost +
               ", currency='" + currency + "'" +
               ", billingCycle=" + billingCycle.getLabel() +
               ", tier='" + tier + "'}";
    }
}
