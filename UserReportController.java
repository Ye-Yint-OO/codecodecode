package com.prj.LoneHPManagement.Controller;

import com.prj.LoneHPManagement.Service.UserReportService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/reports")
public class UserReportController {

    @Autowired
    private UserReportService userReportService;

    @GetMapping("/users/pdf")
    public ResponseEntity<Resource> getUserPdfReport(
            @RequestParam(value = "branchId", required = false) Long branchId) throws JRException {
        ByteArrayOutputStream reportStream = userReportService.generateUserPdfReport(branchId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_report.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);
        ByteArrayResource resource = new ByteArrayResource(reportStream.toByteArray());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(reportStream.size())
                .body(resource);
    }

    @GetMapping("/users/excel")
    public ResponseEntity<Resource> getUserExcelReport(
            @RequestParam(value = "branchId", required = false) Long branchId) throws JRException {
        ByteArrayOutputStream reportStream = userReportService.generateUserExcelReport(branchId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_report.xls");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ByteArrayResource resource = new ByteArrayResource(reportStream.toByteArray());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(reportStream.size())
                .body(resource);
    }
}