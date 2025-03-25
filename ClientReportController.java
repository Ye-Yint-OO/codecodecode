package com.prj.LoneHPManagement.Controller;

import com.prj.LoneHPManagement.Service.impl.ClientReportService;
import com.prj.LoneHPManagement.Service.impl.ClientReportService.ReportResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cif/report")
public class ClientReportController {

    private static final String PDF_CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String TEXT_CONTENT_TYPE = MediaType.TEXT_PLAIN_VALUE;
    private static final String CONTENT_DISPOSITION = HttpHeaders.CONTENT_DISPOSITION;

    private final ClientReportService clientReportService;

    public ClientReportController(ClientReportService clientReportService) {
        this.clientReportService = clientReportService;
    }

    @GetMapping("/generateCifReport")
    public ResponseEntity<byte[]> generateCifReport(
            @RequestParam("format") String format,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "branchName", required = false) String branchName) {
        try {
            logRequest(format, page, size, branchName);
            ReportResponse reportResponse = clientReportService.generateCifReport(format, page, size, branchName);
            return buildSuccessResponse(reportResponse, format, page, branchName);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "No data found: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating report: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
        }
    }

    private void logRequest(String format, int page, int size, String branchName) {
        System.out.printf("Starting report generation for format: %s, page: %d, size: %d, branchName: %s%n",
                format, page, size, branchName != null ? branchName : "all");
    }

    private ResponseEntity<byte[]> buildSuccessResponse(ReportResponse reportResponse, String format, int page, String branchName) {
        FormatDetails formatDetails = getFormatDetails(format);
        if (formatDetails == null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.parseMediaType(TEXT_CONTENT_TYPE))
                    .body(("Unsupported format: " + format).getBytes());
        }

        HttpHeaders headers = createHeaders(reportResponse, formatDetails, page, branchName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(formatDetails.contentType))
                .body(reportResponse.getReportData());
    }

    private FormatDetails getFormatDetails(String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return new FormatDetails(PDF_CONTENT_TYPE, "pdf");
        } else if ("xls".equalsIgnoreCase(format)) {
            return new FormatDetails(XLSX_CONTENT_TYPE, "xlsx");
        }
        return null;
    }

    private HttpHeaders createHeaders(ReportResponse reportResponse, FormatDetails formatDetails, int page, String branchName) {
        HttpHeaders headers = new HttpHeaders();
        String fileName = "CIF_Report" +
                (branchName != null ? "_" + branchName : "") +
                "_Page_" + (page + 1) + "." + formatDetails.fileExtension;
        headers.add(CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add("X-Total-Pages", String.valueOf(reportResponse.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(reportResponse.getTotalElements()));
        headers.add("X-Current-Page", String.valueOf(reportResponse.getCurrentPage()));
        headers.add("X-Page-Size", String.valueOf(reportResponse.getPageSize()));
        return headers;
    }

    private ResponseEntity<byte[]> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType(TEXT_CONTENT_TYPE))
                .body(message.getBytes());
    }

    private static class FormatDetails {
        String contentType;
        String fileExtension;

        FormatDetails(String contentType, String fileExtension) {
            this.contentType = contentType;
            this.fileExtension = fileExtension;
        }
    }
}