package com.prj.LoneHPManagement.Controller;


import com.prj.LoneHPManagement.Service.impl.SubCategoryService;
import com.prj.LoneHPManagement.model.dto.ApiResponse;
import com.prj.LoneHPManagement.model.dto.PagedResponse;
import com.prj.LoneHPManagement.model.entity.SubCategory;
import com.prj.LoneHPManagement.model.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subCategory")
public class SubCategoryController {

        @Autowired
        private SubCategoryService subCategoryService;


    // SubCategoryController.java
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<PagedResponse<SubCategory>>> getAllSubCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<SubCategory> categories = subCategoryService.getAllSubCategories(page, size, sortBy);

        PagedResponse<SubCategory> pagedResponse = new PagedResponse<>(
                categories.getContent(),
                categories.getTotalPages(),
                categories.getTotalElements(),
                categories.getSize(),
                categories.getNumber(),
                categories.getNumberOfElements(),
                categories.isFirst(),
                categories.isLast(),
                categories.isEmpty()
        );

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Sub-categories retrieved successfully",
                pagedResponse
        ));
    }

    @PostMapping("/createSubCat/{mainCategoryId}")
    public ResponseEntity<SubCategory> createSubCategory(
            @PathVariable Integer mainCategoryId,
            @RequestBody SubCategory subCategory) {
        SubCategory createdSubCategory = subCategoryService.createSubCategory(mainCategoryId, subCategory);
        return new ResponseEntity<>(createdSubCategory, HttpStatus.CREATED);
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<SubCategory> getSubCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(subCategoryService.getCategoryById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SubCategory> updateSubCategory(@PathVariable Integer id, @RequestBody SubCategory subCategory) {
        return ResponseEntity.ok(subCategoryService.updateSubCategory(id, subCategory));
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> softDeleteSubCategory(@PathVariable Integer id) { // Changed to match MainCategory naming
        try {
            subCategoryService.softDeleteSubCategory(id); // Updated method name
            ApiResponse<String> response = ApiResponse.success(HttpStatus.OK.value(), "Subcategory soft deleted successfully", "Soft Deleted");
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // Changed to NOT_FOUND
        }
    }

    @PutMapping("/activate/{id}") // Changed from /restore to /activate
    public ResponseEntity<ApiResponse<String>> activateSubCategory(@PathVariable Integer id) {
        try {
            subCategoryService.activateSubCategory(id); // Changed method name
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Subcategory activated successfully", "Activated")); // Updated message
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
}
