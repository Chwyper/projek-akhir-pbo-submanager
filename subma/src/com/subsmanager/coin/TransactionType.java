package com.subsmanager.coin;

/**
 * Enum untuk tipe transaksi coin.
 * PURCHASE = user membeli coin
 * USAGE    = user menggunakan coin untuk fitur
 * REFUND   = pengembalian coin jika ada kesalahan
 */

public enum TransactionType {
    PURCHASE,
    USAGE,
    REFUND;

    /**
     * Mengembalikan label yang mudah dibaca
     */
    public String getLabel() {
        switch (this) {
            case PURCHASE: return "Pembelian Coin";
            case USAGE:    return "Penggunaan Coin";
            case REFUND:   return "Pengembalian Coin";
            default:       return "Tidak diketahui";
        }
    }

    /**
     * Mengembalikan apakah transaksi ini
     * menambah atau mengurangi saldo coin.
     * true  = menambah saldo
     * false = mengurangi saldo
     */
    public boolean isCredit() {
        switch (this) {
            case PURCHASE: return true;
            case REFUND:   return true;
            case USAGE:    return false;
            default:       return false;
        }
    }
}
