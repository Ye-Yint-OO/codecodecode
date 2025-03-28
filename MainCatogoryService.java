package com.prj.LoneHPManagement.Service.impl;

import com.prj.LoneHPManagement.model.entity.ConstraintEnum;
import com.prj.LoneHPManagement.model.entity.MainCategory;
import com.prj.LoneHPManagement.model.exception.ServiceException;
import com.prj.LoneHPManagement.model.repo.MainCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MainCatogoryService {

    @Autowired
    private MainCategoryRepository mainCategoryRepostirory;

    @Transactional
    public MainCategory createMainCat(MainCategory mainCategory) {
        // Ensure the status is set to ACTIVE (13) if not provided or invalid
        if (mainCategory.getStatus() == 0 || ConstraintEnum.fromCode(mainCategory.getStatus()) == null) {
            mainCategory.setStatus(13); // Default to ACTIVE
        }
        // Check if the category already exists (case-insensitive)
        MainCategory existingCategory = mainCategoryRepostirory.findByCategoryIgnoreCase(mainCategory.getCategory());
        if (existingCategory != null) {
            throw new ServiceException("Category '" + mainCategory.getCategory() + "' already exists");
        }
        return mainCategoryRepostirory.save(mainCategory);
    }

    public Page<MainCategory> getAllMainCategories(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return mainCategoryRepostirory.findAll(pageable);
    }

    public MainCategory getCategoryById(Integer id) {
        return mainCategoryRepostirory.findById(id)
                .orElseThrow(() -> new ServiceException("Category not found with ID: " + id));
    }

    @Transactional
    public MainCategory updateCategory(Integer id, String categoryName) {
        MainCategory category = mainCategoryRepostirory.findById(id)
                .orElseThrow(() -> new ServiceException("Category not found with ID: " + id));
        // Check if the new category name already exists (case-insensitive)
        MainCategory existingCategory = mainCategoryRepostirory.findByCategoryIgnoreCase(categoryName);
        if (existingCategory != null && existingCategory.getId() != id) {
            throw new ServiceException("Category '" + categoryName + "' already exists");
        }
        category.setCategory(categoryName);
        return mainCategoryRepostirory.save(category);
    }

    public MainCategory showById(int id) {
        return mainCategoryRepostirory.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    public void softDeleteMainCategory(int id) {
        MainCategory category = mainCategoryRepostirory.findById(id)
                .orElseThrow(() -> new ServiceException("Main category not found"));

        category.setStatus(ConstraintEnum.DELETED.getCode()); // Soft delete
        mainCategoryRepostirory.save(category);
    }

    public List<MainCategory> findByStatus(int status) {
        // Validate the status code
        if (ConstraintEnum.fromCode(status) == null) {
            throw new ServiceException("Invalid status code: " + status);
        }
        return mainCategoryRepostirory.findByStatus(status);
    }

    @Transactional
    public void activateMainCategory(int id) throws ServiceException {
        MainCategory category = mainCategoryRepostirory.findById(id)
                .orElseThrow(() -> new ServiceException("Main category not found with ID: " + id));

        // Check if category is already active
        if (category.getStatus() == ConstraintEnum.ACTIVE.getCode()) {
            throw new ServiceException("Category is already active");
        }

        // Activate the category
        category.setStatus(ConstraintEnum.ACTIVE.getCode());
        mainCategoryRepostirory.save(category);
    }



}