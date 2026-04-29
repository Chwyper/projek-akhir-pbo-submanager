package com.subsmanager.subscription.model;

import java.util.Date;

public class CustomSubscription extends Subscription {

    // ── Fields ───────────────────────────────────────────
    private String customCancelUrl;
    private String customDomain;
    private String category;
    private String customTier;

    // ── Constructor ──────────────────────────────────────
    public CustomSubscription(Long id, String serviceName,
                               double cost, String currency,
                               Date billingDate, BillingCycle billingCycle,
                               String tier, String customCancelUrl,
                               String customDomain, String category) {
        super(id, serviceName, cost, currency,
              billingDate, billingCycle, tier);
        this.customCancelUrl = customCancelUrl;
        this.customDomain = customDomain;
        this.category = category;
        this.customTier = tier;
    }
    public CustomSubscription() { super(); }
    // ── Implementasi Abstract Methods ────────────────────

    /**
     * Mengembalikan URL pembatalan yang diisi user.
     * Jika tidak diisi maka fallback ke Google search.
     * Polimorfisme: implementasi berbeda dari PredefinedSubscription
     */
    @Override
    public String getCancelPageURL() {
        if (customCancelUrl != null && !customCancelUrl.isEmpty()) {
            return customCancelUrl;
        }
        return "https://www.google.com/search?q=how+to+cancel+"
               + serviceName.replace(" ", "+") + "+subscription";
    }

    /**
     * Mengembalikan URL ikon menggunakan Google Favicon.
     * Polimorfisme: implementasi berbeda dari PredefinedSubscription
     */
    @Override
    public String getIconUrl() {
        if (customDomain != null && !customDomain.isEmpty()) {
            return "https://www.google.com/s2/favicons?domain="
                   + customDomain + "&sz=64";
        }
        return "https://www.google.com/s2/favicons?domain=example.com&sz=64";
    }

    // ── Getters & Setters ────────────────────────────────
    public String getCustomCancelUrl() { return customCancelUrl; }
    public String getCustomDomain() { return customDomain; }
    public String getCategory() { return category; }
    public String getCustomTier() { return customTier; }

    public void setCustomCancelUrl(String customCancelUrl) {
        this.customCancelUrl = customCancelUrl;
    }
    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setCustomTier(String customTier) {
        this.customTier = customTier;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "CustomSubscription{" +
               "serviceName='" + serviceName + "'" +
               ", tier='" + (customTier != null ? customTier : "-") + "'" +
               ", cost=" + cost +
               ", currency='" + currency + "'" +
               ", billingCycle=" + billingCycle.getLabel() +
               ", category='" + category + "'" +
               ", cancelUrl='" + getCancelPageURL() + "'}";
    }
}
