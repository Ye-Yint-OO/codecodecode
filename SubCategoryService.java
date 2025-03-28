package com.prj.LoneHPManagement.Service.impl;

import com.prj.LoneHPManagement.model.entity.ConstraintEnum;
import com.prj.LoneHPManagement.model.entity.MainCategory;
import com.prj.LoneHPManagement.model.entity.SubCategory;
import com.prj.LoneHPManagement.model.exception.ServiceException;
import com.prj.LoneHPManagement.model.repo.MainCategoryRepository;
import com.prj.LoneHPManagement.model.repo.SubCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubCategoryService {

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private MainCategoryRepository mainCategoryRepository;
    public Page<SubCategory> getAllSubCategories(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return subCategoryRepository.findAll(pageable); // Only active
    }

    @Transactional
    public SubCategory createSubCategory(Integer mainCategoryId, SubCategory subCategory) {
        MainCategory mainCategory = mainCategoryRepository.findById(mainCategoryId)
                .orElseThrow(() -> new ServiceException("Main Category not found with ID: " + mainCategoryId));
        subCategory.setMainCategory(mainCategory);

        if (subCategoryRepository.findByCategoryIgnoreCaseAndStatus(subCategory.getCategory(), ConstraintEnum.ACTIVE.getCode()) != null) {
            throw new ServiceException("Subcategory '" + subCategory.getCategory() + "' already exists");
        }

        return subCategoryRepository.save(subCategory);
    }


    public SubCategory getCategoryById(Integer id) {
        return subCategoryRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Subcategory not found with ID: " + id));
    }

    @Transactional
    public SubCategory updateSubCategory(Integer id, SubCategory updatedSubCategory) {
        SubCategory existingSubCategory = getCategoryById(id);
        existingSubCategory.setCategory(updatedSubCategory.getCategory());

        if (updatedSubCategory.getMainCategory() != null) {
            MainCategory mainCategory = mainCategoryRepository.findById(updatedSubCategory.getMainCategory().getId())
                    .orElseThrow(() -> new ServiceException("Main Category not found with ID: " + updatedSubCategory.getMainCategory().getId()));
            existingSubCategory.setMainCategory(mainCategory);
        } else {
            throw new ServiceException("Main Category must not be null");
        }

        if (subCategoryRepository.findByCategoryIgnoreCaseAndStatusAndIdNot(updatedSubCategory.getCategory(), ConstraintEnum.ACTIVE.getCode(), id) != null) {
            throw new ServiceException("Subcategory '" + updatedSubCategory.getCategory() + "' already exists");
        }

        return subCategoryRepository.save(existingSubCategory);
    }



    @Transactional
    public void softDeleteSubCategory(Integer id) { // Renamed to match MainCategory
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Subcategory not found")); // Simplified lookup
        subCategory.setStatus(ConstraintEnum.DELETED.getCode()); // Soft delete
        subCategoryRepository.save(subCategory);
    }

    @Transactional
    public void activateSubCategory(Integer id) { // Changed from restoreSubCategory to activateSubCategory
        SubCategory subCategory = getCategoryById(id);
        if (subCategory.getStatus() == ConstraintEnum.ACTIVE.getCode()) {
            throw new ServiceException("Subcategory is already active");
        }
        subCategory.setStatus(ConstraintEnum.ACTIVE.getCode());
        subCategoryRepository.save(subCategory);
    }
}
