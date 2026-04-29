package com.subsmanager.manager;

import com.subsmanager.auth.User;
import com.subsmanager.overlay.OverlayController;
import com.subsmanager.subscription.model.Subscription;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionManager {

    // ── Fields ───────────────────────────────────────────
    private List<Subscription> subscriptionList;
    private OverlayController overlayController;

    // ── Constructor ──────────────────────────────────────
    public SubscriptionManager(OverlayController overlayController) {
        this.subscriptionList = new ArrayList<>();
        this.overlayController = overlayController;
    }
    
    public SubscriptionManager() {
        this.subscriptionList = new ArrayList<>();
        this.overlayController = null;
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Memuat semua subscription milik user ke dalam list
     */
    public void loadSubscriptions(User user) {
        this.subscriptionList = new ArrayList<>(
            user.getSubscriptions()
        );
        System.out.println("Berhasil memuat " + 
            subscriptionList.size() + 
            " subscription milik " + user.getEmail());
    }

    /**
     * Menambahkan subscription baru ke list
     */
    public void addSubscription(Subscription subscription) {
        subscriptionList.add(subscription);
        System.out.println("Subscription " + 
            subscription.getServiceName() + 
            " berhasil ditambahkan.");
    }

    /**
     * Menghapus subscription dari list berdasarkan id
     */
    public void removeSubscription(Long id) {
        subscriptionList.removeIf(s -> s.getId().equals(id));
        System.out.println("Subscription dengan id " + 
            id + " berhasil dihapus.");
    }

    /**
     * Mencari subscription berdasarkan id
     */
    public Subscription findById(Long id) {
        for (Subscription s : subscriptionList) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        System.out.println("Subscription dengan id " + 
            id + " tidak ditemukan.");
        return null;
    }

    /**
     * Memicu proses pembatalan subscription.
     * Dependency: memanggil OverlayController untuk
     * menampilkan URL pembatalan.
     */
    public void triggerCancelProcess(Subscription subscription) {
        System.out.println("Memulai proses pembatalan untuk: " +
            subscription.getServiceName());
        overlayController.displayUrl(subscription);
    }

    /**
     * Menampilkan semua subscription dalam list
     */
    public void printAllSubscriptions(double usdToIdrRate) {
        if (subscriptionList.isEmpty()) {
            System.out.println("Tidak ada subscription.");
            return;
        }
        System.out.println("===== DAFTAR SUBSCRIPTION =====");
        for (Subscription s : subscriptionList) {
            s.printSummary(usdToIdrRate);
        }
    }

    /**
     * Mengembalikan jumlah subscription dalam list
     */
    public int getTotalSubscriptions() {
        return subscriptionList.size();
    }

    // ── Getters ──────────────────────────────────────────
    public List<Subscription> getSubscriptionList() {
        return subscriptionList;
    }

    public OverlayController getOverlayController() {
        return overlayController;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "SubscriptionManager{" +
               "jumlah subscription=" + 
               subscriptionList.size() + "}";
    }
}
