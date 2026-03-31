package com.techrentalhub.order;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfExportService {

    public byte[] exportContract(Order order) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitle = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font fontContent = new Font(Font.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("TECHRENTALHUB - DEVICE RENTAL CONTRACT", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("ORDER ID: #" + order.getId(), fontContent));
            document.add(new Paragraph("Customer Email: " + order.getUser().getEmail(), fontContent));
            document.add(new Paragraph(
                    "Device: " + order.getDevice().getName() + " (" + order.getDevice().getBrand() + ")", fontContent));
            document.add(new Paragraph("Rental Period: " + order.getStartDate() + " to " + order.getEndDate() + " ("
                    + order.getRentalDays() + " days)", fontContent));
            document.add(new Paragraph("Total Rental Fee: " + order.getTotalAmount() + " VND", fontContent));
            document.add(new Paragraph("Deposit (50% Base Price): " + order.getDepositAmount() + " VND", fontContent));
            document.add(new Paragraph("Status: " + order.getStatus().name(), fontContent));

            Paragraph footer = new Paragraph("\n\nSignature\n\n___________________\nCustomer");
            footer.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF contract");
        }

        return out.toByteArray();
    }
}
