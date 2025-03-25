package com.prj.LoneHPManagement.Service.impl;

import com.prj.LoneHPManagement.model.dto.BranchDTO;
import com.prj.LoneHPManagement.model.entity.Branch;
import com.prj.LoneHPManagement.model.repo.BranchRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BranchReportService {

    @Autowired
    private BranchRepository branchRepository;

    public static class ReportResponse {
        private byte[] reportData;
        private int totalPages;
        private long totalElements;
        private int currentPage;
        private int pageSize;

        public ReportResponse(byte[] reportData, int totalPages, long totalElements, int currentPage, int pageSize) {
            this.reportData = reportData;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
        }

        public byte[] getReportData() { return reportData; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
    }

    public ReportResponse generateBranchReport(String format, int page, int size) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Branch> branchPage = branchRepository.findAll(pageable);
        List<BranchDTO> branches = branchPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        if (branches.isEmpty()) {
            throw new IllegalArgumentException("Branch data is empty or not found for page " + page);
        }

        try (InputStream jrxmlInputStream = getClass().getResourceAsStream("/reports/BranchReport.jrxml")) {
            if (jrxmlInputStream == null) {
                throw new IOException("JasperReport JRXML file not found");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(branches);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Branch Report - Page " + (page + 1));

            try {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                if (jasperPrint.getPages().isEmpty()) {
                    throw new IllegalStateException("No data in report for page " + page);
                }

                byte[] reportData;
                if ("pdf".equalsIgnoreCase(format)) {
                    reportData = exportToPDF(jasperPrint);
                } else if ("xls".equalsIgnoreCase(format)) {
                    reportData = exportToExcel(jasperPrint);
                } else {
                    throw new IllegalArgumentException("Unsupported format: " + format);
                }

                return new ReportResponse(reportData, branchPage.getTotalPages(), branchPage.getTotalElements(), page, size);

            } catch (JRException e) {
                System.err.println("Error filling report: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("Error generating report", e);
            }
        } catch (JRException | IllegalArgumentException | IllegalStateException e) {
            throw new IOException("Report generation error: " + e.getMessage(), e);
        }
    }

    private BranchDTO convertToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setBranchCode(branch.getBranchCode());
        dto.setBranchName(branch.getBranchName());
        dto.setCreatedDate(branch.getCreatedDate());
        dto.setUpdatedDate(branch.getUpdatedDate());

        // Extract createdUserId from the User entity
        dto.setUserCode(branch.getCreatedUser() != null ? branch.getCreatedUser().getUserCode() : "N/A");
        // Set status directly from Branch entity
        dto.setStatus(branch.getStatus());

        // Extract address fields if address is not null
        if (branch.getAddress() != null) {
            dto.setTownship(branch.getAddress().getTownship());
            dto.setCity(branch.getAddress().getCity());
            dto.setState(branch.getAddress().getState());
            dto.setAdditionalAddress(branch.getAddress().getAdditionalAddress());
        }

        return dto;
    }

    private byte[] exportToPDF(JasperPrint jasperPrint) throws JRException, IOException {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();
            return out.toByteArray();
        }
    }

    private byte[] exportToExcel(JasperPrint jasperPrint) throws JRException, IOException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            SimpleXlsxExporterConfiguration configuration = new SimpleXlsxExporterConfiguration();
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            return out.toByteArray();
        }
    }
}