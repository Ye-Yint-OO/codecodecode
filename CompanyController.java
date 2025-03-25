package com.prj.LoneHPManagement.Controller;

import com.prj.LoneHPManagement.Service.CompanyService;
import com.prj.LoneHPManagement.model.dto.ApiResponse;
import com.prj.LoneHPManagement.model.dto.CompanyDTO;
import com.prj.LoneHPManagement.model.dto.PagedResponse;
import com.prj.LoneHPManagement.model.entity.BusinessPhoto;
import com.prj.LoneHPManagement.model.entity.Company;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @GetMapping("/byCompany/{companyId}")
    public ResponseEntity<ApiResponse<List<BusinessPhoto>>> getBusinessPhotosByCompany(
            @PathVariable int companyId) {

        List<BusinessPhoto> photos = companyService.getPhotosByCompanyId(companyId);

        ApiResponse<List<BusinessPhoto>> response = ApiResponse.success(
                200,
                "Business photos for company " + companyId + " retrieved successfully",
                photos
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/byCif/{cifId}")
    public ResponseEntity<ApiResponse<PagedResponse<Company>>> getCompaniesByCif(
            @PathVariable int cifId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<Company> companies = companyService.getCompaniesByCifId(cifId, page, size, sortBy);

        PagedResponse<Company> pagedResponse = new PagedResponse<>(
                companies.getContent(),
                companies.getTotalPages(),
                companies.getTotalElements(),
                companies.getSize(),
                companies.getNumber(),
                companies.getNumberOfElements(),
                companies.isFirst(),
                companies.isLast(),
                companies.isEmpty()
        );

        ApiResponse<PagedResponse<Company>> response = ApiResponse.success(
                200,
                "Companies for CIF " + cifId + " retrieved successfully",
                pagedResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Company>> save(@RequestBody CompanyDTO companyDTO, HttpServletRequest request) {
        System.out.println("Request received from: " + request.getRemoteAddr());
        System.out.println("Headers: " + request.getHeaderNames());
        System.out.println("DTO: " + companyDTO);
        try {
            Company savedCompany = companyService.save(companyDTO);
            ApiResponse<Company> response = ApiResponse.success(HttpStatus.OK.value(),
                    "Company created successfully", savedCompany);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(403, e.getMessage()));
        }
    }


    // ✅ Get all companies
    @GetMapping("/list")
    public ResponseEntity<?> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Companies fetched successfully", companies));
    }

    // ✅ Get company by ID
    @GetMapping("list/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable int id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Company found", company));
    }

    // ✅ Update company by ID
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable int id, @RequestBody CompanyDTO companyDTO) {
        Company updatedCompany = companyService.updateCompany(id, companyDTO);
        updatedCompany = ApiResponse.success(HttpStatus.OK.value(), "Company updated successfully", updatedCompany).getData();
        return ResponseEntity.ok(updatedCompany);
    }

    // ✅ Delete company by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable int id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "Company deleted successfully", null));
    }


}

