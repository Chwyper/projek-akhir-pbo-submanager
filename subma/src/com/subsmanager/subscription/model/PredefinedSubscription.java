package com.subsmanager.subscription.model;

import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;
import java.util.Date;


public class PredefinedSubscription extends Subscription {

    // ── Fields ───────────────────────────────────────────
    private Service service;
    private ServiceTier selectedTier;
    

    // ── Constructor ──────────────────────────────────────
    public PredefinedSubscription() {}
    public PredefinedSubscription(Long id, double cost,
                                  String currency, Date billingDate,
                                  BillingCycle billingCycle, String tier,
                                  Service service, ServiceTier selectedTier) {
        super(id, service.getName(), cost, currency, 
              billingDate, billingCycle, tier);
        this.service = service;
        this.selectedTier = selectedTier;
    }

    // ── Implementasi Abstract Methods ────────────────────

    /**
     * Mengembalikan URL pembatalan dari object Service
     * Polimorfisme: implementasi berbeda dari CustomSubscription
     */
    @Override
    public String getCancelPageURL() {
        return service.getCancellationUrl();
    }

    /**
     * Mengembalikan URL ikon dari Clearbit menggunakan domain Service
     * Polimorfisme: implementasi berbeda dari CustomSubscription
     */
    @Override
    public String getIconUrl() {
        return "https://www.google.com/s2/favicons?domain=" + service.getDomain() + "&sz=64";
    }

    // ── Getters & Setters ────────────────────────────────
    public Service getService() { return service; }
    public ServiceTier getSelectedTier() { return selectedTier; }

    public void setService(Service service) { 
        this.service = service; 
    }
    public void setSelectedTier(ServiceTier selectedTier) { 
        this.selectedTier = selectedTier; 
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "PredefinedSubscription{" +
               "service='" + service.getName() + "'" +
               ", tier='" + (selectedTier != null ? 
                 selectedTier.getTierName() : "-") + "'" +
               ", cost=" + cost +
               ", currency='" + currency + "'" +
               ", billingCycle=" + billingCycle.getLabel() + "}";
    }
}
