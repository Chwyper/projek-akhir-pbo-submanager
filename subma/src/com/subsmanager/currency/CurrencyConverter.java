package com.subsmanager.currency;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.net.URI;

public class CurrencyConverter {

    // ── Fields ───────────────────────────────────────────
    private double usdToIdrRate;
    private LocalDateTime lastUpdated;
    private static final String API_URL =
        "https://api.frankfurter.app/latest?from=USD&to=IDR";

    // ── Constructor ──────────────────────────────────────
    public CurrencyConverter() {
        this.usdToIdrRate = 1600.0; // nilai default jika API gagal
        this.lastUpdated = null;
    }

    // ── Core Methods ─────────────────────────────────────

    /**
     * Mengecek apakah kurs perlu diperbarui.
     * Rate diperbarui setiap 24 jam sekali.
     */
    public boolean needsUpdate() {
        if (lastUpdated == null) return true;
        return lastUpdated.isBefore(
            LocalDateTime.now().minusHours(24)
        );
    }

    /**
     * Mengambil kurs terbaru dari Frankfurter API.
     * Jika gagal, tetap menggunakan nilai default.
     */
    public void fetchLatestRate() {
        try {
        	URL url = URI.create(API_URL).toURL();
            HttpURLConnection conn = 
                (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response secara manual
            // Response: {"rates":{"IDR":16000.0}}
            String json = response.toString();
            int idx = json.indexOf("\"IDR\":");
            if (idx != -1) {
                String rateStr = json.substring(idx + 6)
                    .replaceAll("[^0-9.]", "")
                    .trim();
                // Ambil angka pertama saja
                int dotCount = 0;
                StringBuilder numBuilder = new StringBuilder();
                for (char c : rateStr.toCharArray()) {
                    if (Character.isDigit(c)) {
                        numBuilder.append(c);
                    } else if (c == '.' && dotCount == 0) {
                        numBuilder.append(c);
                        dotCount++;
                    } else {
                        break;
                    }
                }
                this.usdToIdrRate = Double.parseDouble(
                    numBuilder.toString()
                );
                this.lastUpdated = LocalDateTime.now();
                System.out.println("Kurs berhasil diperbarui: " +
                    "1 USD = Rp " + 
                    String.format("%,.2f", usdToIdrRate));
            }

        } catch (Exception e) {
            System.out.println("Gagal mengambil kurs, " +
                "menggunakan nilai default: Rp " + 
                String.format("%,.2f", usdToIdrRate));
        }
    }

    /**
     * Mengembalikan kurs terkini.
     * Otomatis refresh jika sudah lebih dari 24 jam.
     */
    public double getRate() {
        if (needsUpdate()) {
            fetchLatestRate();
        }
        return usdToIdrRate;
    }

    /**
     * Mengkonversi jumlah dari mata uang tertentu ke IDR.
     * Jika sudah IDR langsung dikembalikan tanpa konversi.
     */
    public double convert(double amount, String fromCurrency) {
        if (fromCurrency.equalsIgnoreCase("USD")) {
            return amount * getRate();
        }
        return amount;
    }

    /**
     * Memformat angka ke format Rupiah
     * Contoh: 150000.0 → "Rp 150.000"
     */
    public String formatIDR(double amount) {
        return "Rp " + String.format("%,.0f", amount);
    }

    // ── Getters ──────────────────────────────────────────
    public double getUsdToIdrRate() { return usdToIdrRate; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "CurrencyConverter{" +
               "usdToIdrRate=" + usdToIdrRate +
               ", lastUpdated=" + lastUpdated + "}";
    }
}
