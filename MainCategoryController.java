package com.prj.LoneHPManagement.Controller;


import com.prj.LoneHPManagement.Service.impl.MainCatogoryService;
import com.prj.LoneHPManagement.model.dto.ApiResponse;
import com.prj.LoneHPManagement.model.dto.PagedResponse;
import com.prj.LoneHPManagement.model.entity.MainCategory;
import com.prj.LoneHPManagement.model.exception.ServiceException;
import com.prj.LoneHPManagement.model.repo.MainCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mainCategory")
public class MainCategoryController {

//    @Autowired
//    private MainCategoryRepository mainCategoryRepostirory;

    @Autowired
    private MainCatogoryService mainCatogoryService;
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<PagedResponse<MainCategory>>> getAllMainCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<MainCategory> categories = mainCatogoryService.getAllMainCategories(page, size, sortBy);

        PagedResponse<MainCategory> pagedResponse = new PagedResponse<>(
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
                "Main categories retrieved successfully",
                pagedResponse
        ));
    }

    @PostMapping("/createMainCat")
    public ResponseEntity<MainCategory> createMaincat(@RequestBody MainCategory mainCategory){
        MainCategory createMaincat = mainCatogoryService.createMainCat(mainCategory);
        return new ResponseEntity<>(mainCategory, HttpStatus.CREATED);
    }

//    @GetMapping("/list")
//    public ResponseEntity<List<MainCategory>> getAllCategories() {
//       MainCategory mainCategory = new MainCategory();
//        List<MainCategory> MainCatList = mainCatogoryService.selectAllActiveMainCat();
//        return new ResponseEntity<List<MainCategory>>(HttpStatus.OK).ok(MainCatList);
//    }


    @PutMapping("/update/{id}")
    public MainCategory updateMainCat(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        String categoryName = payload.get("category");
        return mainCatogoryService.updateCategory(id, categoryName); // Assume this method accepts a String
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> softDeleteMainCategory(@PathVariable int id) {
        try {
            mainCatogoryService.softDeleteMainCategory(id);
            ApiResponse<String> response = ApiResponse.success(HttpStatus.OK.value(), "Main category soft deleted successfully", "Soft Deleted");
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<ApiResponse<String>> activateMainCategory(@PathVariable int id) {
        try {
            mainCatogoryService.activateMainCategory(id);
            ApiResponse<String> response = ApiResponse.success(
                    HttpStatus.OK.value(),
                    "Main category activated successfully",
                    "Activated"
            );
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}
