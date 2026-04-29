package com.subsmanager.coin;

/**
 * Enum untuk metode pembayaran yang tersedia.
 * Semua metode bersifat dummy untuk keperluan simulasi.
 */
public enum PaymentMethod {
    TRANSFER_BCA,
    TRANSFER_MANDIRI,
    TRANSFER_BNI,
    GOPAY,
    OVO,
    DANA,
    QRIS;

    /**
     * Mengembalikan label yang mudah dibaca
     */
    public String getLabel() {
        switch (this) {
            case TRANSFER_BCA:     return "Transfer Bank BCA";
            case TRANSFER_MANDIRI: return "Transfer Bank Mandiri";
            case TRANSFER_BNI:     return "Transfer Bank BNI";
            case GOPAY:            return "GoPay";
            case OVO:              return "OVO";
            case DANA:             return "DANA";
            case QRIS:             return "QRIS";
            default:               return "Tidak diketahui";
        }
    }

    /**
     * Mengembalikan tipe metode pembayaran
     * Berguna untuk menentukan instruksi yang ditampilkan
     */
    public String getType() {
        switch (this) {
            case TRANSFER_BCA:
            case TRANSFER_MANDIRI:
            case TRANSFER_BNI:  return "BANK_TRANSFER";
            case GOPAY:
            case OVO:
            case DANA:          return "E_WALLET";
            case QRIS:          return "QRIS";
            default:            return "UNKNOWN";
        }
    }

    /**
     * Mengembalikan nomor tujuan pembayaran (dummy)
     */
    public String getPaymentNumber() {
        switch (this) {
            case TRANSFER_BCA:     return "1234567890";
            case TRANSFER_MANDIRI: return "9876543210";
            case TRANSFER_BNI:     return "1122334455";
            case GOPAY:            return "0812-3456-7890";
            case OVO:              return "0812-3456-7890";
            case DANA:             return "0812-3456-7890";
            case QRIS:             return "QRIS-SUBMANAGER-2026";
            default:               return "-";
        }
    }
}