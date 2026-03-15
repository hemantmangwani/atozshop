package com.atozshop.service;

import com.atozshop.dto.response.BillResponse;
import com.atozshop.entity.Bill;
import com.atozshop.entity.BillItem;
import com.atozshop.entity.Store;
import com.atozshop.repository.BillItemRepository;
import com.atozshop.repository.BillRepository;
import com.atozshop.repository.StoreRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public byte[] generateReceiptPDF(Long billId, Long tenantId) {
        // Get bill
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + billId));

        // Get bill items
        List<BillItem> items = billItemRepository.findByBillId(billId);

        // Get store details
        Store store = storeRepository.findByIdAndTenantId(bill.getStoreId(), tenantId)
                .orElse(null);

        // Generate PDF
        return createPDF(bill, items, store);
    }

    private byte[] createPDF(Bill bill, List<BillItem> items, Store store) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header - Store Info
            if (store != null) {
                document.add(new Paragraph(store.getName())
                        .setFontSize(20)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));

                if (store.getAddress() != null) {
                    document.add(new Paragraph(store.getAddress())
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER));
                }

                if (store.getPhone() != null) {
                    document.add(new Paragraph("Phone: " + store.getPhone())
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER));
                }

                if (store.getGstNumber() != null) {
                    document.add(new Paragraph("GSTIN: " + store.getGstNumber())
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER));
                }
            }

            document.add(new Paragraph("\n"));

            // Bill Info
            document.add(new Paragraph("TAX INVOICE")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Bill Details
            document.add(new Paragraph("Bill No: " + bill.getBillNumber())
                    .setFontSize(10));
            document.add(new Paragraph("Date: " +
                    bill.getBillDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")))
                    .setFontSize(10));
            document.add(new Paragraph("Bill Type: " + bill.getBillType())
                    .setFontSize(10));

            document.add(new Paragraph("\n"));

            // Items Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Headers
            table.addHeaderCell("S.No");
            table.addHeaderCell("Item");
            table.addHeaderCell("Qty");
            table.addHeaderCell("Price");
            table.addHeaderCell("Disc");
            table.addHeaderCell("Amount");

            // Table Rows
            int serialNo = 1;
            for (BillItem item : items) {
                table.addCell(String.valueOf(serialNo++));
                table.addCell(item.getProductName() +
                    (item.getVariantName() != null ? "\n" + item.getVariantName() : ""));
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("₹" + item.getUnitPrice());
                table.addCell("₹" + item.getDiscountAmount());
                table.addCell("₹" + item.getTotalAmount());
            }

            document.add(table);

            document.add(new Paragraph("\n"));

            // Totals
            document.add(new Paragraph("Subtotal: ₹" + bill.getSubtotal())
                    .setTextAlignment(TextAlignment.RIGHT));

            if (bill.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Discount: -₹" + bill.getDiscountAmount())
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            if (bill.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Tax: ₹" + bill.getTaxAmount())
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(new Paragraph("Total Amount: ₹" + bill.getTotalAmount())
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("\n"));

            // Payment Status
            document.add(new Paragraph("Payment Status: " + bill.getPaymentStatus())
                    .setFontSize(10));
            document.add(new Paragraph("Paid Amount: ₹" + bill.getPaidAmount())
                    .setFontSize(10));

            if (bill.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Balance: ₹" + bill.getBalanceAmount())
                        .setFontSize(10)
                        .setBold());
            }

            document.add(new Paragraph("\n\n"));

            // Footer
            document.add(new Paragraph("Thank you for your business!")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF receipt", e);
        }
    }

    @Transactional(readOnly = true)
    public String generateThermalReceipt(Long billId, Long tenantId) {
        // Get bill
        Bill bill = billRepository.findByIdAndTenantId(billId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + billId));

        // Get bill items
        List<BillItem> items = billItemRepository.findByBillId(billId);

        // Get store details
        Store store = storeRepository.findByIdAndTenantId(bill.getStoreId(), tenantId)
                .orElse(null);

        // Generate thermal receipt text (80mm width ~42 characters)
        return createThermalReceipt(bill, items, store);
    }

    private String createThermalReceipt(Bill bill, List<BillItem> items, Store store) {
        StringBuilder receipt = new StringBuilder();
        String line = "==========================================\n";

        // Header
        if (store != null) {
            receipt.append(centerText(store.getName(), 42)).append("\n");
            if (store.getAddress() != null) {
                receipt.append(centerText(store.getAddress(), 42)).append("\n");
            }
            if (store.getPhone() != null) {
                receipt.append(centerText("Ph: " + store.getPhone(), 42)).append("\n");
            }
            if (store.getGstNumber() != null) {
                receipt.append(centerText("GSTIN: " + store.getGstNumber(), 42)).append("\n");
            }
        }

        receipt.append(line);
        receipt.append(centerText("TAX INVOICE", 42)).append("\n");
        receipt.append(line);

        // Bill details
        receipt.append(String.format("Bill No: %-30s\n", bill.getBillNumber()));
        receipt.append(String.format("Date: %-33s\n",
            bill.getBillDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"))));
        receipt.append(line);

        // Items
        receipt.append(String.format("%-20s %4s %6s %8s\n", "Item", "Qty", "Price", "Amount"));
        receipt.append(line);

        for (BillItem item : items) {
            String itemName = item.getProductName();
            if (itemName.length() > 20) {
                itemName = itemName.substring(0, 17) + "...";
            }

            receipt.append(String.format("%-20s %4d %6.0f %8.0f\n",
                itemName,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalAmount()));

            if (item.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                receipt.append(String.format("  Discount: -₹%.2f\n", item.getDiscountAmount()));
            }
        }

        receipt.append(line);

        // Totals
        receipt.append(String.format("%-32s %8.2f\n", "Subtotal:", bill.getSubtotal()));

        if (bill.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-32s %8.2f\n", "Discount:", bill.getDiscountAmount()));
        }

        if (bill.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-32s %8.2f\n", "Tax:", bill.getTaxAmount()));
        }

        receipt.append(line);
        receipt.append(String.format("%-32s %8.2f\n", "TOTAL:", bill.getTotalAmount()));
        receipt.append(line);

        // Payment
        receipt.append(String.format("%-32s %8.2f\n", "Paid:", bill.getPaidAmount()));

        if (bill.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-32s %8.2f\n", "Balance:", bill.getBalanceAmount()));
        }

        receipt.append(line);
        receipt.append(centerText("Thank You! Visit Again!", 42)).append("\n");
        receipt.append(line);

        return receipt.toString();
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }

        int padding = (width - text.length()) / 2;
        StringBuilder centered = new StringBuilder();

        for (int i = 0; i < padding; i++) {
            centered.append(" ");
        }

        centered.append(text);
        return centered.toString();
    }
}
