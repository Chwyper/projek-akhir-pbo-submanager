# SubManager — Subscription Manager

Aplikasi manajemen langganan (*subscription*) berbasis desktop yang dibangun dengan **Java 21** dan **JavaFX 21**. Proyek ini merupakan tugas akhir mata kuliah **Pemrograman Berorientasi Objek (PBO)** yang mendemonstrasikan penerapan konsep-konsep OOP secara menyeluruh.

---

## Fitur Utama

| Fitur | Keterangan |
|---|---|
| **Autentikasi** | Login & Register akun pengguna |
| **Dashboard** | Ringkasan total langganan, biaya bulanan/tahunan, dan saldo koin |
| **Manajemen Langganan** | Tambah, lihat, dan hapus langganan (*predefined* & *custom*) |
| **Ringkasan Keuangan** | Kalkulasi pengeluaran bulanan dan proyeksi tahunan |
| **Konversi Mata Uang** | Kurs USD → IDR real-time via Frankfurter API (refresh 24 jam) |
| **Sistem Koin** | Beli koin, gunakan untuk fitur premium (Export PDF, Reminder) |
| **Export PDF & Excel** | Ekspor data langganan menggunakan Apache PDFBox & Apache POI |

---

## Konsep OOP yang Diterapkan

1. **Inheritance** — `Admin extends User`
2. **Komposisi** — `Service` memiliki daftar `ServiceTier`
3. **Polymorphism** — `getCancelPageURL()` dan `getIconUrl()` di-override oleh `PredefinedSubscription` dan `CustomSubscription`
4. **Agregasi** — `User` memiliki daftar `Subscription` dan `CoinBalance`
5. **Dependency** — `SubscriptionManager` bergantung pada `OverlayController`; `CoinService` bergantung pada `PaymentProcessor`
6. **Abstract Class** — `Subscription` sebagai *abstract base class*
7. **Singleton** — `DatabaseConnection` (koneksi JDBC ke Supabase)
8. **DAO Pattern** — `UserDAO`, `SubscriptionDAO`, `CoinDAO`

---

## Tech Stack

| Komponen | Versi |
|---|---|
| Java | 21 |
| JavaFX | 21.0.10 |
| PostgreSQL JDBC | 42.7.10 |
| Database (cloud) | Supabase PostgreSQL |
| Apache PDFBox | 3.0.7 |
| Apache POI | 5.5.1 |
| Log4j | 2.24.3 |
| IDE | Eclipse |

---

## Struktur Paket

```
subma/src/com/subsmanager/
├── Main.java                  # Entry point (mode GUI / mode demo)
├── MainApp.java               # JavaFX Application entry point
├── SessionManager.java        # State global: stage & user aktif
│
├── auth/                      # Autentikasi pengguna
│   ├── User.java
│   └── Admin.java
│
├── catalog/                   # Katalog layanan
│   ├── Service.java
│   └── ServiceTier.java
│
├── coin/                      # Sistem koin & pembayaran
│   ├── CoinBalance.java
│   ├── CoinPackage.java
│   ├── CoinService.java
│   ├── CoinTransaction.java
│   ├── PaymentMethod.java     # Enum: GOPAY, OVO, DANA, dll.
│   ├── PaymentProcessor.java
│   ├── TransactionStatus.java
│   └── TransactionType.java
│
├── currency/                  # Konversi mata uang
│   └── CurrencyConverter.java # Frankfurter API
│
├── db/                        # Data Access Layer
│   ├── DatabaseConnection.java
│   ├── UserDAO.java
│   ├── SubscriptionDAO.java
│   └── CoinDAO.java
│
├── financial/                 # Ringkasan keuangan
│   └── FinancialSummary.java
│
├── gui/                       # Antarmuka pengguna (JavaFX)
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── RegisterController.java
│   │   ├── DashboardController.java
│   │   ├── SubscriptionController.java
│   │   ├── AddSubController.java
│   │   ├── FinancialController.java
│   │   ├── CoinStoreController.java
│   │   ├── CoinHistoryController.java
│   │   └── ExportService.java
│   └── fxml/
│       ├── login.fxml
│       ├── register.fxml
│       ├── dashboard.fxml
│       ├── subscription.fxml
│       ├── addsub.fxml
│       ├── financial.fxml
│       ├── coinstore.fxml
│       └── coinhistory.fxml
│
├── manager/                   # Logika pengelolaan langganan
│   └── SubscriptionManager.java
│
├── overlay/                   # Overlay navigasi
│   └── OverlayController.java
│
└── subscription/              # Model langganan
    └── model/
        ├── Subscription.java          # Abstract base class
        ├── PredefinedSubscription.java
        ├── CustomSubscription.java
        └── BillingCycle.java          # Enum: MONTHLY, YEARLY
```

---

## Cara Menjalankan

### Prasyarat
- **Java 21** (JDK)
- **JavaFX SDK 21.0.10** — unduh dari [gluonhq.com](https://gluonhq.com/products/javafx/)
- **Eclipse IDE** (disarankan) atau IDE lain yang mendukung JavaFX module

### Konfigurasi Eclipse
1. Clone repositori ini.
2. Import sebagai *Existing Java Project* dari folder `subma/`.
3. Perbarui path *classpath* di `.classpath` agar mengarah ke lokasi JavaFX SDK di komputer Anda.
4. Tambahkan VM arguments berikut pada *Run Configuration*:
   ```
   --module-path /path/to/javafx-sdk-21.0.10/lib --add-modules javafx.controls,javafx.fxml
   ```
5. Jalankan `Main.java` sebagai Java Application.

### Mode Aplikasi

`Main.java` memiliki flag yang dapat diubah:

```java
// GUI mode (default) — jalankan antarmuka JavaFX
private static final boolean LAUNCH_GUI = true;

// Demo mode — jalankan demo console OOP
private static final boolean LAUNCH_GUI = false;
```

---

## Paket Koin

| Paket | Jumlah Koin | Harga |
|---|---|---|
| Starter | 50 koin | Rp 10.000 |
| Regular | 150 koin | Rp 25.000 |
| Pro | 350 koin | Rp 50.000 |

Koin digunakan untuk mengakses fitur premium seperti **Export PDF** dan **Reminder Langganan**.

---

## Metode Pembayaran

- GoPay
- OVO
- DANA
- Transfer Bank
- Kartu Kredit

---

## Koneksi Database

Aplikasi terhubung ke **Supabase PostgreSQL** menggunakan JDBC melalui *Session Pooler* (region `aws-1-ap-southeast-1`). Koneksi dikelola sebagai **Singleton** oleh `DatabaseConnection.java` dan ditutup otomatis saat pengguna logout.

---

## Lisensi

Proyek ini dibuat untuk keperluan akademik — **Tugas Akhir PBO Semester 4**.
