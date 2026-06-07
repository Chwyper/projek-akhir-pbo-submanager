<div align="center">
  <h1 align="center">SubManager — Subscription Manager</h1>
  <p align="center">
    Aplikasi desktop manajemen langganan (subscription) berbasis Java 21 dan JavaFX.
    <br />
    <a href="#-fitur-utama"><strong>Jelajahi Fitur »</strong></a>
    <br />
    <br />
    <a href="#-cara-menjalankan-getting-started">Mulai Menjalankan</a>
    ·
    <a href="#-struktur-proyek">Lihat Struktur</a>
  </p>
</div>

<!-- BADGES -->
<div align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-21-FF8000?style=for-the-badge&logo=java&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-Supabase-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
</div>

<br />

## 📖 Tentang Proyek

**SubManager** adalah aplikasi manajemen langganan (*subscription*) berbasis desktop yang dirancang untuk memudahkan pengguna dalam melacak, mengelola, dan memproyeksikan biaya langganan bulanan maupun tahunan. 

Proyek ini dikembangkan sebagai **Tugas Akhir mata kuliah Pemrograman Berorientasi Objek (PBO)** di ITENAS Bandung. Fokus utama dari proyek ini adalah mendemonstrasikan penerapan konsep-konsep *Object-Oriented Programming* (OOP) secara mendalam dalam arsitektur perangkat lunak yang utuh.

---

## ✨ Fitur Utama

- 🔐 **Sistem Autentikasi**: Login dan Register dengan pembagian peran (User & Admin).
- 📊 **Dashboard Terpusat**: Visualisasi ringkasan langganan, biaya bulanan/tahunan, dan saldo koin secara intuitif.
- 📦 **Manajemen Langganan**: Kemudahan menambah langganan dari katalog (*Predefined*) atau membuat entri kustom (*Custom*).
- 💰 **Ringkasan Keuangan**: Kalkulasi matematis untuk pengeluaran bulanan dan proyeksi biaya tahunan.
- 💱 **Konversi Mata Uang Real-time**: Integrasi Frankfurter API untuk konversi kurs USD → IDR secara *real-time*.
- 🪙 **Sistem Koin & Pembayaran**: Simulasi *top-up* koin untuk fitur premium. (Menyediakan paket Starter, Regular, dan Pro via GoPay, DANA, OVO, dll).
- 📄 **Export PDF & Excel**: Pembuatan laporan finansial dan data langganan menggunakan *Apache PDFBox* & *Apache POI*.
- 🛠️ **Admin Panel**: Dasbor khusus admin untuk manajemen katalog layanan, pemantauan transaksi, dan kelola pengguna.

---

## 🛠️ Teknologi yang Digunakan

Proyek ini dibangun menggunakan berbagai teknologi dan *library* modern:

| Komponen | Teknologi / Versi |
|---|---|
| **Core Language** | [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) |
| **GUI Framework** | [JavaFX 21.0.10](https://gluonhq.com/products/javafx/) |
| **Database** | PostgreSQL (di-host via [Supabase](https://supabase.com/)) |
| **Connection Pooling**| [HikariCP 5.1.0](https://github.com/brettwooldridge/HikariCP) |
| **JDBC Driver** | PostgreSQL JDBC 42.7.10 |
| **Testing** | JUnit 5 & TestFX 4.0.18 |
| **PDF Generation** | Apache PDFBox 3.0.7 |
| **Excel Generation**| Apache POI 5.5.1 |
| **Logging** | Log4j 2.24.3 & SLF4J |

---

## 🧠 Penerapan Konsep OOP

SubManager dirancang secara hati-hati untuk memenuhi kaidah *Object-Oriented Programming*:

1. **Inheritance** — Kelas `Admin` mewarisi dari kelas `User`.
2. **Polymorphism** — Metode *override* seperti `getCancelPageURL()` untuk berbagai tipe langganan.
3. **Encapsulation** — Perlindungan akses data melalui *modifier* `private` dengan metode *getter* dan *setter*.
4. **Abstraction** — Penggunaan kelas abstrak `Subscription` sebagai fondasi dasar model.
5. **Composition & Aggregation** — Relasi *Has-A* (contoh: `User` memiliki daftar `Subscription` dan `CoinBalance`).
6. **Design Patterns**:
   - **Singleton**: Digunakan pada `DatabaseConnection` untuk memastikan hanya ada satu *connection pool*.
   - **DAO (Data Access Object)**: Pemisahan logika akses basis data (`UserDAO`, `SubscriptionDAO`, dll).

---

## 🚀 Cara Menjalankan (Getting Started)

### Prasyarat Instalasi
- **Java 21 (JDK)**
- **JavaFX SDK 21.0.10**
- IDE Java (direkomendasikan: **Eclipse IDE**)

### Langkah Instalasi (Eclipse)
1. Kloning repositori ini ke komputer lokal Anda:
   ```sh
   git clone https://github.com/Chwyper/projek-akhir-pbo-submanager.git
   ```
2. Buka Eclipse dan lakukan *Import* -> **Existing Projects into Workspace**. Arahkan ke folder `subma/`.
3. Buat file bernama `.env` di dalam root project (sejajar dengan `src/`) menggunakan format dari `.env.example`, dan isi kredensial *database* Anda.
4. Pastikan *build path* (`.classpath`) sudah sesuai atau arahkan kembali ke lokasi file `.jar` JavaFX SDK di komputer Anda. Seluruh dependensi pihak ketiga (*library*) telah dilampirkan secara *offline* di folder `Depedensi/`.
5. Tambahkan Argumen VM (VM Arguments) pada **Run Configuration** di Eclipse:
   ```sh
   --module-path /path/to/javafx-sdk-21.0.10/lib --add-modules javafx.controls,javafx.fxml
   ```
6. Jalankan kelas `Main.java` sebagai *Java Application*.

---

## 📂 Struktur Proyek

Berikut adalah gambaran arsitektur tingkat tinggi dari direktori *source code* (kode sumber):

```text
subma/src/com/subsmanager/
├── auth/           # Model autentikasi (User, Admin)
├── catalog/        # Katalog layanan (Service, Tier)
├── coin/           # Sistem mata uang & Payment Gateway
├── currency/       # API Konversi kurs
├── db/             # Data Access Object (DAO) & Koneksi DB (HikariCP)
├── financial/      # Pemroses kalkulasi ringkasan
├── gui/            # JavaFX Controller & File FXML
├── manager/        # Logika aplikasi dan state management
├── overlay/        # Transisi antar-antarmuka
└── subscription/   # Model langganan abstrak dan konkret
```

---

## 🧪 Pengujian (Testing)

Aplikasi ini dilengkapi dengan **60 Test Cases** yang komprehensif, dibangun menggunakan **JUnit 5** dan **TestFX** untuk pengujian otomatis antarmuka pengguna (UI).
Cakupan pengujian meliputi proses Autentikasi, Manajemen Langganan, Simulasi Transaksi Koin, Fungsi Ekspor, dan Panel Admin.

Untuk menjalankan pengujian di Eclipse:
- Klik kanan folder `test/` di *Project Explorer* -> Pilih `Run As` -> `JUnit Test`.

---


