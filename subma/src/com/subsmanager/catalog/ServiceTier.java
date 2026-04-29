package com.subsmanager.catalog;

public class ServiceTier {

    // ── Fields ───────────────────────────────────────────
    private Long id;
    private String tierName;
    private String description;

    // ── Constructor ──────────────────────────────────────
    public ServiceTier(Long id, String tierName, String description) {
        this.id = id;
        this.tierName = tierName;
        this.description = description;
    }

    public ServiceTier() {}
    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public String getTierName() { return tierName; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setTierName(String tierName) { 
        this.tierName = tierName; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "ServiceTier{id=" + id +
               ", tierName='" + tierName + "'" +
               ", description='" + description + "'}";
    }
}
