package com.subsmanager.coin;

/**
 * Enum untuk status transaksi coin.
 * PENDING = transaksi sedang diproses
 * SUCCESS = transaksi berhasil
 * FAILED  = transaksi gagal
 */

public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED;

    /**
     * Mengembalikan label yang mudah dibaca
     */
    public String getLabel() {
        switch (this) {
            case PENDING: return "Menunggu Konfirmasi";
            case SUCCESS: return "Berhasil";
            case FAILED:  return "Gagal";
            default:      return "Tidak diketahui";
        }
    }

    /**
     * Mengecek apakah transaksi sudah selesai diproses
     * Berguna untuk PaymentProcessor saat auto-approve
     */
    public boolean isFinished() {
        switch (this) {
            case SUCCESS: return true;
            case FAILED:  return true;
            case PENDING: return false;
            default:      return false;
        }
    }
}

