package com.common_wealth_builders.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service for generating PDF reports
 */
@Service
@Slf4j
public class PdfReportService {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(236, 240, 241);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Generate Financial Summary Report PDF
     */
    public byte[] generateFinancialSummaryPdf(Map<String, Object> reportData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add Header
            addReportHeader(document, "Common Wealth Financial Summary Report");
            
            // Add Report Period
            document.add(new Paragraph("Report Period: " + reportData.get("period"))
                    .setFontSize(12)
                    .setMarginBottom(20));

            // Add Summary Statistics
            addSummarySection(document, reportData);

            // Add Payment Details Table
            if (reportData.containsKey("payments")) {
                addPaymentTable(document, (List<Map<String, Object>>) reportData.get("payments"));
            }

            // Add Expense Details Table
            if (reportData.containsKey("expenses")) {
                addExpenseTable(document, (List<Map<String, Object>>) reportData.get("expenses"));
            }

            // Add Footer
            addReportFooter(document);

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating financial summary PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate User Contribution Report PDF
     */
    public byte[] generateUserContributionPdf(Map<String, Object> reportData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add Header
            addReportHeader(document, "User Contribution Report");
            
            // Add User Info
            document.add(new Paragraph("User: " + reportData.get("userName"))
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(10));
            
            document.add(new Paragraph("Email: " + reportData.get("userEmail"))
                    .setFontSize(10)
                    .setMarginBottom(20));

            // Add Contribution Statistics
            addContributionStats(document, reportData);

            // Add Payment History Table
            if (reportData.containsKey("paymentHistory")) {
                addPaymentHistoryTable(document, (List<Map<String, Object>>) reportData.get("paymentHistory"));
            }

            addReportFooter(document);
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating user contribution PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate Expense Report PDF
     */
    public byte[] generateExpenseReportPdf(Map<String, Object> reportData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addReportHeader(document, "Expense Report");
            
            document.add(new Paragraph("Report Period: " + reportData.get("period"))
                    .setFontSize(12)
                    .setMarginBottom(20));

            // Add Expense Summary
            addExpenseSummary(document, reportData);

            // Add Expense Details
            if (reportData.containsKey("expenses")) {
                addDetailedExpenseTable(document, (List<Map<String, Object>>) reportData.get("expenses"));
            }

            addReportFooter(document);
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating expense PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate Payment Report PDF
     */
    public byte[] generatePaymentReportPdf(Map<String, Object> reportData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addReportHeader(document, "Payment Report");
            
            document.add(new Paragraph("Report Period: " + reportData.get("period"))
                    .setFontSize(12)
                    .setMarginBottom(20));

            // Add Payment Statistics
            addPaymentStats(document, reportData);

            // Add Payment Details
            if (reportData.containsKey("payments")) {
                addDetailedPaymentTable(document, (List<Map<String, Object>>) reportData.get("payments"));
            }

            addReportFooter(document);
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating payment PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    // Helper Methods

    private void addReportHeader(Document document, String title) {
        document.add(new Paragraph("COMMONWEALTH BUILDERS")
                .setFontSize(20)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));
        
        document.add(new Paragraph(title)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));
        
        document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30));
    }

    private void addSummarySection(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        addSummaryRow(table, "Total Income:", formatCurrency(data.get("totalIncome")));
        addSummaryRow(table, "Total Expenses:", formatCurrency(data.get("totalExpenses")));
        addSummaryRow(table, "Net Balance:", formatCurrency(data.get("netBalance")));
        addSummaryRow(table, "Total Payments:", data.get("totalPayments").toString());

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addSummaryRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold())
                .setBackgroundColor(HEADER_BG));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    private void addPaymentTable(Document document, List<Map<String, Object>> payments) {
        document.add(new Paragraph("Recent Payments")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1.5f, 1.5f, 1}))
                .useAllAvailableWidth();

        // Header
        addTableHeader(table, new String[]{"ID", "User", "Amount", "Date", "Status"});

        // Data rows
        for (Map<String, Object> payment : payments) {
            table.addCell(new Cell().add(new Paragraph(payment.get("id").toString())));
            table.addCell(new Cell().add(new Paragraph(payment.get("userName").toString())));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(payment.get("amount")))));
            table.addCell(new Cell().add(new Paragraph(payment.get("date").toString())));
            table.addCell(new Cell().add(new Paragraph(payment.get("status").toString())));
        }

        document.add(table);
    }

    private void addExpenseTable(Document document, List<Map<String, Object>> expenses) {
        document.add(new Paragraph("Recent Expenses")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1.5f, 1.5f, 1}))
                .useAllAvailableWidth();

        // Header
        addTableHeader(table, new String[]{"ID", "Title", "Amount", "Date", "Category"});

        // Data rows
        for (Map<String, Object> expense : expenses) {
            table.addCell(new Cell().add(new Paragraph(expense.get("id").toString())));
            table.addCell(new Cell().add(new Paragraph(expense.get("title").toString())));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(expense.get("amount")))));
            table.addCell(new Cell().add(new Paragraph(expense.get("date").toString())));
            table.addCell(new Cell().add(new Paragraph(expense.get("category").toString())));
        }

        document.add(table);
    }

    private void addContributionStats(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        addSummaryRow(table, "Total Contributions:", formatCurrency(data.get("totalContributions")));
        addSummaryRow(table, "Number of Payments:", data.get("paymentCount").toString());
        addSummaryRow(table, "Average Contribution:", formatCurrency(data.get("averageContribution")));
        addSummaryRow(table, "Last Payment Date:", data.get("lastPaymentDate").toString());

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addPaymentHistoryTable(Document document, List<Map<String, Object>> payments) {
        document.add(new Paragraph("Payment History")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1.5f, 1.5f}))
                .useAllAvailableWidth();

        addTableHeader(table, new String[]{"ID", "Amount", "Date", "Status"});

        for (Map<String, Object> payment : payments) {
            table.addCell(new Cell().add(new Paragraph(payment.get("id").toString())));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(payment.get("amount")))));
            table.addCell(new Cell().add(new Paragraph(payment.get("date").toString())));
            table.addCell(new Cell().add(new Paragraph(payment.get("status").toString())));
        }

        document.add(table);
    }

    private void addExpenseSummary(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        addSummaryRow(table, "Total Expenses:", formatCurrency(data.get("totalExpenses")));
        addSummaryRow(table, "Number of Expenses:", data.get("expenseCount").toString());
        addSummaryRow(table, "Approved Expenses:", formatCurrency(data.get("approvedExpenses")));
        addSummaryRow(table, "Pending Approval:", formatCurrency(data.get("pendingExpenses")));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addDetailedExpenseTable(Document document, List<Map<String, Object>> expenses) {
        document.add(new Paragraph("Expense Details")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1.5f, 1.5f, 1, 1}))
                .useAllAvailableWidth();

        addTableHeader(table, new String[]{"ID", "Title", "Amount", "Date", "Category", "Status"});

        for (Map<String, Object> expense : expenses) {
            table.addCell(new Cell().add(new Paragraph(expense.get("id").toString())));
            table.addCell(new Cell().add(new Paragraph(expense.get("title").toString())));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(expense.get("amount")))));
            table.addCell(new Cell().add(new Paragraph(expense.get("date").toString())));
            table.addCell(new Cell().add(new Paragraph(expense.get("category").toString())));
            table.addCell(new Cell().add(new Paragraph(expense.get("status").toString())));
        }

        document.add(table);
    }

    private void addPaymentStats(Document document, Map<String, Object> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        addSummaryRow(table, "Total Payments:", formatCurrency(data.get("totalPayments")));
        addSummaryRow(table, "Number of Payments:", data.get("paymentCount").toString());
        addSummaryRow(table, "Verified Payments:", formatCurrency(data.get("verifiedPayments")));
        addSummaryRow(table, "Pending Verification:", formatCurrency(data.get("pendingPayments")));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addDetailedPaymentTable(Document document, List<Map<String, Object>> payments) {
        document.add(new Paragraph("Payment Details")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1.5f, 1.5f, 1, 1}))
                .useAllAvailableWidth();

        addTableHeader(table, new String[]{"ID", "User", "Amount", "Date", "Status", "Verified"});

        for (Map<String, Object> payment : payments) {
            table.addCell(new Cell().add(new Paragraph(payment.get("id").toString())));
            table.addCell(new Cell().add(new Paragraph(payment.get("userName").toString())));
            table.addCell(new Cell().add(new Paragraph(formatCurrency(payment.get("amount")))));
            table.addCell(new Cell().add(new Paragraph(payment.get("date").toString())));
            table.addCell(new Cell().add(new Paragraph(payment.get("status").toString())));
            table.addCell(new Cell().add(new Paragraph(payment.get("verified").toString())));
        }

        document.add(table);
    }

    private void addTableHeader(Table table, String[] headers) {
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
        }
    }

    private void addReportFooter(Document document) {
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("This is a system-generated report from Commonwealth Builders Management System")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
        
        document.add(new Paragraph("For inquiries, contact: support@commonwealthbuilders.com")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
    }

    private String formatCurrency(Object amount) {
        if (amount instanceof BigDecimal) {
            return CURRENCY_FORMAT.format((BigDecimal) amount);
        } else if (amount instanceof Number) {
            return CURRENCY_FORMAT.format(((Number) amount).doubleValue());
        }
        return amount != null ? amount.toString() : "₦0.00";
    }
}