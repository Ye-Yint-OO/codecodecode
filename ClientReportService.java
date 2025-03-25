package com.prj.LoneHPManagement.Service.impl;

import com.prj.LoneHPManagement.model.dto.CifDTO;
import com.prj.LoneHPManagement.model.entity.CIF;
import com.prj.LoneHPManagement.model.repo.CIFRepository;
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
public class ClientReportService {

    @Autowired
    private CIFRepository cifRepository;

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

    public ReportResponse generateCifReport(String format, int page, int size, String branchName) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        Page<CIF> cifPage = (branchName != null && !branchName.trim().isEmpty())
                ? cifRepository.findByBranchName(branchName, pageable)
                : cifRepository.findAll(pageable);



        if (cifPage.isEmpty()) {
            throw new IllegalArgumentException("No CIF data found" +
                    (branchName != null ? " for branch: " + branchName : "") +
                    " on page " + page);
        }

        List<CifDTO> cifs = cifPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        try (InputStream jrxmlInputStream = getClass().getResourceAsStream("/reports/ClientReport.jrxml")) {
            if (jrxmlInputStream == null) {
                throw new IOException("JasperReport JRXML file not found");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(cifs);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Client Report - " +
                    (branchName != null ? branchName : "All Branches") + " - Page " + (page + 1));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            if (jasperPrint.getPages().isEmpty()) {
                throw new IllegalStateException("No data in report for page " + page);
            }

            byte[] reportData = "pdf".equalsIgnoreCase(format) ? exportToPDF(jasperPrint) : exportToExcel(jasperPrint);
            return new ReportResponse(reportData, cifPage.getTotalPages(), cifPage.getTotalElements(), page, size);

        } catch (JRException e) {
            throw new IOException("Error generating report: " + e.getMessage(), e);
        }
    }

    private CifDTO convertToDTO(CIF cif) {
        CifDTO dto = new CifDTO();
        dto.setId(cif.getId());
        dto.setCifCode(cif.getCifCode());
        dto.setName(cif.getName());
        dto.setEmail(cif.getEmail());
        dto.setGender(cif.getGender() != null ? cif.getGender().name() : null);
        dto.setDateOfBirth(cif.getDateOfBirth());
        dto.setPhoneNumber(cif.getPhoneNumber());
        dto.setNRC(cif.getNRC());
        dto.setCifType(cif.getCifType() != null ? cif.getCifType().name() : null);

        String userCode = (cif.getCreatedUser() != null) ? cif.getCreatedUser().getUserCode() : null;
        dto.setUserCode(userCode);

        if (cif.getCreatedUser() != null && cif.getCreatedUser().getBranch() != null) {
            dto.setBranchName(cif.getCreatedUser().getBranch().getBranchName());
        }

        if (cif.getAddress() != null) {
            dto.setCity(cif.getAddress().getCity());
            dto.setState(cif.getAddress().getState());
        }

        System.out.println("CIF id: " + cif.getId() + ", userCode: " + userCode +
                ", city: " + dto.getCity() + ", state: " + dto.getState());
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