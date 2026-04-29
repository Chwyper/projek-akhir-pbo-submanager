package com.subsmanager.overlay;

import com.subsmanager.subscription.model.Subscription;

public class OverlayController {

    // ── Fields ───────────────────────────────────────────
    private int windowWidth;
    private int windowHeight;
    private boolean isOpen;

    // ── Constructor ──────────────────────────────────────
    public OverlayController(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.isOpen = false;
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Menampilkan URL pembatalan ke user.
     * Di frontend nanti akan membuka URL di overlay/tab baru.
     */
    public void displayUrl(String url) {
        this.isOpen = true;
        System.out.println("=================================");
        System.out.println("Membuka halaman pembatalan...");
        System.out.println("URL    : " + url);
        System.out.println("Ukuran : " + windowWidth + 
            " x " + windowHeight);
        System.out.println("=================================");
    }

    /**
     * Menutup overlay pembatalan
     */
    public void closeOverlay() {
        this.isOpen = false;
        System.out.println("Overlay ditutup.");
    }

    /**
     * Menampilkan URL pembatalan dari object Subscription
     * Overloading: menerima Subscription langsung
     */
    public void displayUrl(Subscription subscription) {
        String url = subscription.getCancelPageURL();
        displayUrl(url);
    }

    /**
     * Menampilkan instruksi manual jika URL tidak tersedia
     */
    public void displayManualInstructions(String serviceName) {
        System.out.println("=================================");
        System.out.println("URL tidak tersedia untuk: " + serviceName);
        System.out.println("Silakan cari secara manual:");
        System.out.println("https://www.google.com/search?q=" +
            "cara+cancel+" + serviceName.replace(" ", "+"));
        System.out.println("=================================");
    }

    // ── Getters & Setters ────────────────────────────────
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public boolean isOpen() { return isOpen; }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }
    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "OverlayController{" +
               "windowWidth=" + windowWidth +
               ", windowHeight=" + windowHeight +
               ", isOpen=" + isOpen + "}";
    }
}