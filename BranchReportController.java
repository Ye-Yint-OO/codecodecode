package com.prj.LoneHPManagement.Controller;

import com.prj.LoneHPManagement.Service.impl.BranchReportService;
import com.prj.LoneHPManagement.model.repo.BranchRepository;
import com.prj.LoneHPManagement.model.repo.UserRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/branch/report")
public class BranchReportController {

    @Autowired
    private BranchReportService branchReportService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/generate")
    public ResponseEntity<InputStreamResource> generateBranchReport(
            @RequestParam("format") String format,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "branchName", required = false) String branchName) {
        try {
            System.out.println("Starting branch report generation for format: " + format +
                    ", page: " + page + ", size: " + size +
                    ", branchName: " + (branchName != null ? branchName : "all"));

            BranchReportService.ReportResponse reportResponse = branchReportService.generateBranchReport(format, page, size);
            byte[] reportBytes = reportResponse.getReportData();

            MediaType mediaType;
            String fileExtension;
            if ("pdf".equalsIgnoreCase(format)) {
                mediaType = MediaType.APPLICATION_PDF;
                fileExtension = "pdf";
            } else if ("xls".equalsIgnoreCase(format)) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
                fileExtension = "xlsx";
            } else {
                return ResponseEntity.badRequest()
                        .body(new InputStreamResource(new ByteArrayInputStream("Unsupported format.".getBytes())));
            }

            HttpHeaders headers = new HttpHeaders();
            String fileName = "Branch_Report" +
                    (branchName != null ? "_" + branchName : "") +
                    "_Page_" + (page + 1) + "." + fileExtension;
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Branch_Report_Page_" + (page + 1) + "." + fileExtension);
            headers.add("X-Total-Pages", String.valueOf(reportResponse.getTotalPages()));
            headers.add("X-Total-Elements", String.valueOf(reportResponse.getTotalElements()));
            headers.add("X-Current-Page", String.valueOf(reportResponse.getCurrentPage()));
            headers.add("X-Page-Size", String.valueOf(reportResponse.getPageSize()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(new InputStreamResource(new ByteArrayInputStream(reportBytes)));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream(("Error generating report: " + e.getMessage()).getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream("Unexpected error.".getBytes())));
        }
    }

https://drive.google.com/drive/folders/1LPLMhHGLBT5sjieKHF6yrk8Rlmkh6-yM

}
