package com.billpoint.backend.controller;

import com.billpoint.backend.model.Bill;
import com.billpoint.backend.model.Shop;
import com.billpoint.backend.repository.BillRepository;
import com.billpoint.backend.repository.ShopRepository;
import com.billpoint.backend.security.UserDetailsImpl;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('SHOP_OWNER')")
public class ReportController {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private BillRepository billRepository;

    private Long getAuthShopId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Shop shop = shopRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Shop not found"));
        return shop.getId();
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        Iterable<Bill> bills = billRepository.findByShopId(shopId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Bill bill : bills) {
            totalRevenue = totalRevenue.add(bill.getFinalAmount());
        }

        return ResponseEntity.ok(new com.billpoint.backend.dto.MessageResponse("Total Revenue: " + totalRevenue.toString()));
    }

    @GetMapping(value = "/download/bills", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadBillsReport(Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        Iterable<Bill> bills = billRepository.findByShopId(shopId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Sales Report").setFontSize(20).setBold());
            document.add(new Paragraph("Shop ID: " + shopId).setMarginBottom(10));

            Table table = new Table(new float[]{3, 3, 3, 3});
            table.addHeaderCell("Bill ID");
            table.addHeaderCell("Date");
            table.addHeaderCell("Payment Mode");
            table.addHeaderCell("Final Amount");

            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (Bill bill : bills) {
                table.addCell(String.valueOf(bill.getId()));
                table.addCell(bill.getCreatedAt().toString());
                table.addCell(bill.getPaymentMode());
                table.addCell(bill.getFinalAmount().toString());
                totalRevenue = totalRevenue.add(bill.getFinalAmount());
            }

            document.add(table);
            document.add(new Paragraph("Total Revenue: Rs. " + totalRevenue).setBold().setMarginTop(10));

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
