package com.subsmanager.auth;


import com.subsmanager.catalog.Service;
import com.subsmanager.catalog.ServiceTier;

/**
 * Class Admin mewarisi User dan menambahkan kemampuan
 * untuk mengelola katalog layanan.
 * Relasi: Admin extends User (Inheritance)
 */
public class Admin extends User {

    // ── Fields ───────────────────────────────────────────
    private String adminLevel;

    // ── Constructor ──────────────────────────────────────
    public Admin(Long id, String email, String password, String adminLevel) {
        super(id, email, password); // memanggil constructor User
        this.adminLevel = adminLevel;
    }

    // ── Service Catalog Methods ──────────────────────────

    /**
     * Menambahkan layanan baru ke katalog
     */
    public void addService(Service service) {
        System.out.println("Admin " + getEmail() + 
            " menambahkan layanan: " + service.getName());
    }

    /**
     * Memperbarui layanan yang sudah ada di katalog
     */
    public void updateService(Service service) {
        System.out.println("Admin " + getEmail() + 
            " memperbarui layanan: " + service.getName());
    }

    /**
     * Menghapus layanan dari katalog berdasarkan id
     */
    public void removeService(Long serviceId) {
        System.out.println("Admin " + getEmail() + 
            " menghapus layanan dengan id: " + serviceId);
    }

    /**
     * Menambahkan tier baru ke sebuah layanan
     */
    public void addServiceTier(Service service, ServiceTier tier) {
        service.addTier(tier);
        System.out.println("Admin " + getEmail() + 
            " menambahkan tier " + tier.getTierName() + 
            " ke layanan " + service.getName());
    }

    /**
     * Menghapus tier dari sebuah layanan
     */
    public void removeServiceTier(Service service, Long tierId) {
        System.out.println("Admin " + getEmail() + 
            " menghapus tier dengan id " + tierId + 
            " dari layanan " + service.getName());
    }

    // ── Getters & Setters ────────────────────────────────
    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { 
        this.adminLevel = adminLevel; 
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "Admin{email='" + getEmail() + 
               "', adminLevel='" + adminLevel + "'}";
    }
}