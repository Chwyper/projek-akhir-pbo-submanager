package com.subsmanager.gui.controller;

import com.subsmanager.auth.User;
import com.subsmanager.currency.CurrencyConverter;
import com.subsmanager.subscription.model.Subscription;
import com.subsmanager.coin.CoinTransaction;
import com.subsmanager.db.CoinDAO.TransactionRecord;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ExportService menangani export data langganan
 * ke format PDF dan Excel.
 *
 * Dependency: FinancialController ..> ExportService
 */
public class ExportService {

    private User user;

    private double rate;

    // ── Constructor ──────────────────────────────────
    public ExportService(User user, CurrencyConverter converter) {
        this.user = user;
      
        this.rate = converter.getRate();
    }

    // ── PDF Export ───────────────────────────────────

    /**
     * Export daftar langganan ke file PDF.
     * @param outputPath path file output PDF
     */
    public void exportPDF(String outputPath) throws Exception {
        List<Subscription> subs = user.getSubscriptions();

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float margin = 50;
        float yStart = 780;
        float y = yStart;
        float lineHeight = 20;
        float pageWidth = PDRectangle.A4.getWidth();

        // ── Header Dokumen ───────────────────────────
        cs.setFont(
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        cs.beginText();
        cs.newLineAtOffset(margin, y);
        cs.showText("Subscription Manager — Laporan Langganan");
        cs.endText();
        y -= lineHeight * 1.5f;

        // Tanggal generate
        cs.setFont(
            new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        cs.beginText();
        cs.newLineAtOffset(margin, y);
        cs.showText("Dibuat: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        cs.endText();
        y -= lineHeight;

        cs.beginText();
        cs.newLineAtOffset(margin, y);
        cs.showText("Pengguna: " + user.getEmail());
        cs.endText();
        y -= lineHeight * 1.5f;

        // ── Garis Pemisah ────────────────────────────
        cs.moveTo(margin, y);
        cs.lineTo(pageWidth - margin, y);
        cs.stroke();
        y -= lineHeight;

        // ── Header Tabel ─────────────────────────────
        cs.setFont(
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        float col1 = margin;
        float col2 = margin + 180;
        float col3 = margin + 310;
        float col4 = margin + 400;
        float col5 = margin + 470;

        cs.beginText();
        cs.newLineAtOffset(col1, y);
        cs.showText("Nama Layanan");
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col2, y);
        cs.showText("Biaya Asli");
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col3, y);
        cs.showText("Per Bulan (IDR)");
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col4, y);
        cs.showText("Siklus");
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col5, y);
        cs.showText("Tier");
        cs.endText();

        y -= lineHeight * 0.5f;
        cs.moveTo(margin, y);
        cs.lineTo(pageWidth - margin, y);
        cs.stroke();
        y -= lineHeight;

        // ── Isi Tabel ────────────────────────────────
        cs.setFont(
            new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);

        double totalBulanan = 0;
        for (Subscription s : subs) {
            if (y < 80) {
                // Tambah halaman baru jika sudah habis
                cs.close();
                PDPage newPage = new PDPage(PDRectangle.A4);
                doc.addPage(newPage);
                cs = new PDPageContentStream(doc, newPage);
                y = yStart;
            }

            double monthlyCost = s.getMonthlyCostInIDR(rate);
            totalBulanan += monthlyCost;

            String nama = truncate(s.getServiceName(), 28);
            String biaya = s.getCurrency() + " " +
                String.format("%,.0f", s.getCost());
            String monthly = "Rp " +
                String.format("%,.0f", monthlyCost);
            String siklus = s.getBillingCycle().getLabel();
            String tier = s.getTier() != null ? s.getTier() : "-";

            cs.beginText();
            cs.newLineAtOffset(col1, y);
            cs.showText(nama);
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(col2, y);
            cs.showText(biaya);
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(col3, y);
            cs.showText(monthly);
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(col4, y);
            cs.showText(siklus);
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(col5, y);
            cs.showText(truncate(tier, 10));
            cs.endText();

            y -= lineHeight;
        }

        // ── Total ────────────────────────────────────
        y -= lineHeight * 0.5f;
        cs.moveTo(margin, y);
        cs.lineTo(pageWidth - margin, y);
        cs.stroke();
        y -= lineHeight;

        cs.setFont(
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        cs.beginText();
        cs.newLineAtOffset(col1, y);
        cs.showText("TOTAL BULANAN");
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col3, y);
        cs.showText("Rp " + String.format("%,.0f", totalBulanan));
        cs.endText();

        cs.close();
        doc.save(outputPath);
        doc.close();

        System.out.println("[ExportService] PDF berhasil: " + outputPath);
    }

    // ── Excel Export ─────────────────────────────────

    /**
     * Export daftar langganan ke file Excel (.xlsx).
     * @param outputPath path file output Excel
     */
    public void exportExcel(String outputPath) throws Exception {
        List<Subscription> subs = user.getSubscriptions();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Langganan Saya");

        // ── Style Header ─────────────────────────────
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(
            IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // ── Style Total ───────────────────────────────
        CellStyle totalStyle = workbook.createCellStyle();
        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalFont);
        totalStyle.setFillForegroundColor(
            IndexedColors.LIGHT_YELLOW.getIndex());
        totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ── Style Data Biasa ──────────────────────────
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // ── Judul ─────────────────────────────────────
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Subscription Manager — Laporan Langganan");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        Row infoRow = sheet.createRow(1);
        infoRow.createCell(0).setCellValue(
            "Pengguna: " + user.getEmail());
        Row dateRow = sheet.createRow(2);
        dateRow.createCell(0).setCellValue(
            "Dibuat: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        dateRow.createCell(3).setCellValue(
            "Kurs: 1 USD = Rp " +
                String.format("%,.0f", rate));

        // ── Header Tabel ──────────────────────────────
        String[] headers = {
            "No", "Nama Layanan", "Tier", "Biaya Asli",
            "Mata Uang", "Per Bulan (IDR)",
            "Per Tahun (IDR)", "Siklus", "Tgl Tagihan"
        };
        Row headerRow = sheet.createRow(4);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ── Isi Data ──────────────────────────────────
        double totalBulanan = 0;
        double totalTahunan = 0;
        int rowNum = 5;

        for (int i = 0; i < subs.size(); i++) {
            Subscription s = subs.get(i);
            double monthly = s.getMonthlyCostInIDR(rate);
            double yearly = monthly * 12;
            totalBulanan += monthly;
            totalTahunan += yearly;

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(s.getServiceName());
            row.createCell(2).setCellValue(
                s.getTier() != null ? s.getTier() : "-");
            row.createCell(3).setCellValue(s.getCost());
            row.createCell(4).setCellValue(s.getCurrency());
            row.createCell(5).setCellValue(monthly);
            row.createCell(6).setCellValue(yearly);
            row.createCell(7).setCellValue(
                s.getBillingCycle().getLabel());
            row.createCell(8).setCellValue(
                s.getBillingDate() != null
                    ? s.getBillingDate().toString() : "-");
        }

        // ── Baris Total ───────────────────────────────
        Row totalRow = sheet.createRow(rowNum + 1);
        Cell totalLabel = totalRow.createCell(0);
        totalLabel.setCellValue("TOTAL");
        totalLabel.setCellStyle(totalStyle);

        Cell totalBln = totalRow.createCell(5);
        totalBln.setCellValue(totalBulanan);
        totalBln.setCellStyle(totalStyle);

        Cell totalThn = totalRow.createCell(6);
        totalThn.setCellValue(totalTahunan);
        totalThn.setCellStyle(totalStyle);

        // ── Auto-size kolom ───────────────────────────
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // ── Simpan file ───────────────────────────────
        FileOutputStream fos = new FileOutputStream(outputPath);
        workbook.write(fos);
        fos.close();
        workbook.close();

        System.out.println("[ExportService] Excel berhasil: "
            + outputPath);
    }
 // ── Receipt Export ────────────────────────────────

    /**
     * Export bukti transaksi pembelian koin ke PDF.
     * @param transaction transaksi yang akan dicetak
     * @param user user pemilik transaksi
     * @param saldoSebelum saldo koin sebelum transaksi
     * @param outputPath path file output PDF
     */
    public void exportReceipt(CoinTransaction transaction,
                               User user,
                               int saldoSebelum,
                               String outputPath) throws Exception {

        PDDocument doc = new PDDocument();

        // Ukuran struk: lebar 226pt (~8cm), tinggi 400pt
        PDRectangle receiptSize = new PDRectangle(226, 400);
        PDPage page = new PDPage(receiptSize);
        doc.addPage(page);

        PDPageContentStream cs =
            new PDPageContentStream(doc, page);

        float margin = 14;
        float width  = receiptSize.getWidth();
        float y      = 380;
        float lh     = 16; // line height

        PDType1Font fontBold =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontReg  =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font fontMono =
            new PDType1Font(Standard14Fonts.FontName.COURIER);

        // ── Judul ────────────────────────────────────
        cs.setFont(fontBold, 11);
        drawCentered(cs, "SUBSCRIPTION MANAGER", width, y);
        y -= lh;
        cs.setFont(fontReg, 9);
        drawCentered(cs, "Bukti Pembelian Koin", width, y);
        y -= lh * 1.2f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Info Transaksi ───────────────────────────
        cs.setFont(fontReg, 8);
        drawRow(cs, "Kode", transaction.getTransactionCode(),
            margin, y, width); y -= lh;
        drawRow(cs, "Tanggal",
            transaction.getCreatedAt().format(
                java.time.format.DateTimeFormatter.ofPattern(
                    "dd MMM yyyy HH:mm")),
            margin, y, width); y -= lh;
        drawRow(cs, "Akun", truncate(user.getEmail(), 24),
            margin, y, width); y -= lh;
        y -= lh * 0.3f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Detail Pembelian ─────────────────────────
        cs.setFont(fontBold, 8);
        drawRow(cs, "Paket",
            transaction.getCoinPackage().getName(),
            margin, y, width); y -= lh;

        cs.setFont(fontReg, 8);
        drawRow(cs, "Jumlah Koin",
            transaction.getCoinAmount() + " koin",
            margin, y, width); y -= lh;
        drawRow(cs, "Metode",
            transaction.getPaymentMethod().getLabel(),
            margin, y, width); y -= lh;
        drawRow(cs, "Total",
            "Rp " + String.format("%,d",
                transaction.getPrice()),
            margin, y, width); y -= lh;

        // Status
        cs.setFont(fontBold, 8);
        drawRow(cs, "Status",
            transaction.getStatus().getLabel().toUpperCase(),
            margin, y, width); y -= lh;
        y -= lh * 0.3f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Info Saldo ───────────────────────────────
        cs.setFont(fontReg, 8);
        drawRow(cs, "Saldo Sebelum",
            saldoSebelum + " koin",
            margin, y, width); y -= lh;
        drawRow(cs, "Saldo Sesudah",
            user.getCoinBalance().getBalance() + " koin",
            margin, y, width); y -= lh;
        y -= lh * 0.3f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Footer ───────────────────────────────────
        cs.setFont(fontReg, 7);
        drawCentered(cs, "Terima kasih telah menggunakan", width, y);
        y -= lh * 0.9f;
        drawCentered(cs, "Subscription Manager", width, y);
        y -= lh * 0.9f;
        cs.setFont(fontMono, 7);
        drawCentered(cs,
            transaction.getTransactionCode(), width, y);

        cs.close();
        doc.save(outputPath);
        doc.close();

        System.out.println("[ExportService] Struk berhasil: "
            + outputPath);
    }

    /**
     * Export bukti transaksi top up dari data riwayat (TransactionRecord).
     * Dipakai dari halaman Riwayat Koin — tidak butuh CoinTransaction penuh.
     *
     * @param rec        data transaksi dari tabel riwayat
     * @param user       user pemilik transaksi
     * @param outputPath path file output PDF
     */
    public void exportReceiptFromRecord(TransactionRecord rec,
                                        User user,
                                        String outputPath) throws Exception {

        PDDocument doc = new PDDocument();

        // Ukuran struk: lebar 226pt (~8cm), tinggi 400pt
        PDRectangle receiptSize = new PDRectangle(226, 400);
        PDPage page = new PDPage(receiptSize);
        doc.addPage(page);

        PDPageContentStream cs =
            new PDPageContentStream(doc, page);

        float margin = 14;
        float width  = receiptSize.getWidth();
        float y      = 380;
        float lh     = 16;

        PDType1Font fontBold =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontReg  =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font fontMono =
            new PDType1Font(Standard14Fonts.FontName.COURIER);

        // ── Judul ────────────────────────────────────
        cs.setFont(fontBold, 11);
        drawCentered(cs, "SUBSCRIPTION MANAGER", width, y);
        y -= lh;
        cs.setFont(fontReg, 9);
        drawCentered(cs, "Bukti Pembelian Koin", width, y);
        y -= lh * 1.2f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Info Transaksi ───────────────────────────
        cs.setFont(fontReg, 8);
        drawRow(cs, "Kode", rec.getKode(),
            margin, y, width); y -= lh;
        drawRow(cs, "Tanggal", rec.getTanggal(),
            margin, y, width); y -= lh;
        drawRow(cs, "Akun", truncate(user.getEmail(), 24),
            margin, y, width); y -= lh;
        y -= lh * 0.3f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Detail Pembelian ─────────────────────────
        cs.setFont(fontBold, 8);
        drawRow(cs, "Paket", rec.getDeskripsi(),
            margin, y, width); y -= lh;

        cs.setFont(fontReg, 8);
        drawRow(cs, "Jumlah Koin",
            rec.getJumlahKoin() + " koin",
            margin, y, width); y -= lh;
        drawRow(cs, "Metode", rec.getMetodeBayar(),
            margin, y, width); y -= lh;
        drawRow(cs, "Total", rec.getHarga(),
            margin, y, width); y -= lh;

        // Status
        cs.setFont(fontBold, 8);
        drawRow(cs, "Status", rec.getStatus().toUpperCase(),
            margin, y, width); y -= lh;
        y -= lh * 0.3f;

        // ── Garis ────────────────────────────────────
        drawLine(cs, margin, width - margin, y);
        y -= lh;

        // ── Footer ───────────────────────────────────
        cs.setFont(fontReg, 7);
        drawCentered(cs, "Terima kasih telah menggunakan", width, y);
        y -= lh * 0.9f;
        drawCentered(cs, "Subscription Manager", width, y);
        y -= lh * 0.9f;
        cs.setFont(fontMono, 7);
        drawCentered(cs, rec.getKode(), width, y);

        cs.close();
        doc.save(outputPath);
        doc.close();

        System.out.println("[ExportService] Struk riwayat berhasil: "
            + outputPath);
    }

    // ── Drawing Helpers ───────────────────────────────

    /**
     * Gambar teks di tengah halaman secara horizontal
     */
    private void drawCentered(PDPageContentStream cs,
                                String text,
                                float pageWidth,
                                float y) throws Exception {
        PDType1Font font =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float textWidth = font.getStringWidth(text) / 1000 * 9;
        float x = (pageWidth - textWidth) / 2;
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Gambar baris label: nilai (kiri: kanan)
     */
    private void drawRow(PDPageContentStream cs,
                          String label,
                          String value,
                          float marginLeft,
                          float y,
                          float pageWidth) throws Exception {
        cs.beginText();
        cs.newLineAtOffset(marginLeft, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(pageWidth - marginLeft
            - value.length() * 4.5f, y);
        cs.showText(value);
        cs.endText();
    }

    /**
     * Gambar garis horizontal
     */
    private void drawLine(PDPageContentStream cs,
                           float x1, float x2,
                           float y) throws Exception {
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }
    // ── Helper ───────────────────────────────────────

    /**
     * Memotong teks jika melebihi panjang maksimum
     * agar tidak overflow di PDF
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "-";
        return text.length() > maxLength
            ? text.substring(0, maxLength - 2) + ".."
            : text;
    }
}