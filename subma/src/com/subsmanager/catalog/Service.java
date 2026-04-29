package com.subsmanager.catalog;

import java.util.ArrayList;
import java.util.List;

public class Service {

    // ── Fields ───────────────────────────────────────────
    private Long id;
    private String name;
    private String domain;
    private String cancellationUrl;
    private String category;
    private String defaultCurrency;
    private List<ServiceTier> availableTiers;

    // ── Constructor ──────────────────────────────────────
    public Service(Long id, String name, String domain,
                   String cancellationUrl, String category,
                   String defaultCurrency) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.cancellationUrl = cancellationUrl;
        this.category = category;
        this.defaultCurrency = defaultCurrency;
        this.availableTiers = new ArrayList<>();
    }

    public Service() {
        this.availableTiers = new ArrayList<>();
    }

    // ── Icon & Cancel Methods ────────────────────────────

    /**
     * Mengembalikan URL ikon layanan menggunakan Clearbit
     * dengan fallback ke Google Favicon
     */
    public String getIconUrl() {
        return "https://logo.clearbit.com/" + domain;
    }

    /**
     * Mengembalikan URL fallback ikon menggunakan Google
     */
    public String getFallbackIconUrl() {
        return "https://www.google.com/s2/favicons?domain=" 
               + domain + "&sz=64";
    }

    /**
     * Mengembalikan URL halaman pembatalan layanan
     */
    public String getCancellationUrl() {
        return cancellationUrl;
    }

    // ── Tier Methods ─────────────────────────────────────

    /**
     * Menambahkan tier baru ke layanan ini
     * Komposisi: tier adalah bagian dari service
     */
    public void addTier(ServiceTier tier) {
        availableTiers.add(tier);
        System.out.println("Tier " + tier.getTierName() + 
            " ditambahkan ke layanan " + name);
    }

    /**
     * Menghapus tier dari layanan berdasarkan id
     */
    public void removeTier(Long tierId) {
        availableTiers.removeIf(tier -> tier.getId().equals(tierId));
        System.out.println("Tier dengan id " + tierId + 
            " dihapus dari layanan " + name);
    }

    /**
     * Mengembalikan semua tier yang tersedia
     */
    public List<ServiceTier> getAvailableTiers() {
        return availableTiers;
    }

    /**
     * Mengecek apakah layanan ini punya tier
     */
    public boolean hasTiers() {
        return !availableTiers.isEmpty();
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDomain() { return domain; }
    public String getCategory() { return category; }
    public String getDefaultCurrency() { return defaultCurrency; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDomain(String domain) { this.domain = domain; }
    public void setCancellationUrl(String url) { 
        this.cancellationUrl = url; 
    }
    public void setCategory(String category) { 
        this.category = category; 
    }
    public void setDefaultCurrency(String defaultCurrency) { 
        this.defaultCurrency = defaultCurrency; 
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "Service{id=" + id +
               ", name='" + name + "'" +
               ", domain='" + domain + "'" +
               ", category='" + category + "'" +
               ", jumlah tier=" + availableTiers.size() + "}";
    }
}
