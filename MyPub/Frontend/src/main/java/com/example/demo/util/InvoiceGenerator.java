package com.example.demo.util;

import com.example.demo.model.OrderDto;
import com.example.demo.model.OrderItemDto;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceGenerator {

    public static void generate(OrderDto order, List<OrderItemDto> items) {
        if (order == null || items == null || items.isEmpty()) {
            AlertUtils.warn("No order or items to print.");
            return;
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            float y = 750;
            float margin = 60;

            // === Header ===
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 22);
            content.newLineAtOffset(margin, y);
            content.showText("The Golden Pub");
            content.endText();

            y -= 25;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(margin, y);
            content.showText("Invoice for Order #" + order.getOrderId());
            content.endText();

            y -= 20;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(margin, y);
            content.showText("Date: " + order.getDatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            content.endText();

            y -= 20;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(margin, y);
            content.showText("Customer: " + order.getName());
            content.endText();

            y -= 20;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(margin, y);
            content.showText("Table: " + order.getTableId());
            content.endText();

            // === Items Table ===
            y -= 40;
            content.setStrokingColor(Color.DARK_GRAY);
            content.moveTo(margin, y);
            content.lineTo(550, y);
            content.stroke();

            y -= 15;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(margin, y);
            content.showText("Product");
            content.newLineAtOffset(250, 0);
            content.showText("Qty");
            content.newLineAtOffset(70, 0);
            content.showText("Subtotal");
            content.endText();

            y -= 10;
            content.moveTo(margin, y);
            content.lineTo(550, y);
            content.stroke();

            y -= 20;
            content.setFont(PDType1Font.HELVETICA, 12);

            BigDecimal total = BigDecimal.ZERO;

            for (OrderItemDto item : items) {
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText(item.getProductName());
                content.newLineAtOffset(250, 0);
                content.showText(String.valueOf(item.getQuantity()));
                content.newLineAtOffset(70, 0);
                content.showText("$" + item.getSubtotal());
                content.endText();

                total = total.add(item.getSubtotal());
                y -= 20;
            }

            y -= 10;
            content.moveTo(margin, y);
            content.lineTo(550, y);
            content.stroke();

            // === Total ===
            y -= 30;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(margin, y);
            content.showText("Total: $" + total);
            content.endText();

            // === Footer ===
            y -= 40;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            content.newLineAtOffset(margin, y);
            content.showText("Thank you for visiting The Golden Pub!");
            content.endText();

            content.close();

            // Save file
            String fileName = "Invoice_Order_" + order.getOrderId() + ".pdf";
            File output = new File(System.getProperty("user.home"), fileName);
            doc.save(output);

            // Open the file automatically (Windows)
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(output);
            }

            AlertUtils.info("Invoice saved to: " + output.getAbsolutePath());

        } catch (IOException e) {
            AlertUtils.error("Error creating invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
