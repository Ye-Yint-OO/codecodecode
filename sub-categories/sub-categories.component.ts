import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubCategoryService } from '../../../services/sub-category.service';
import { MainCategoryService } from '../../../services/main-category.service';
import { SubCategory } from '../../../models/sub-category.model';
import { MainCategory } from '../../../models/main-category.model';
import { ToastrService } from 'ngx-toastr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-sub-categories',
  standalone: false,
  templateUrl: './sub-categories.component.html',
  styleUrl: './sub-categories.component.css'
})
export class SubCategoriesComponent implements OnInit {
  categories: SubCategory[] = [];
  mainCategories: MainCategory[] = [];
  loading = false;
  error: string | null = null;
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  categoryForm: FormGroup;
  isSubmitting = false;
  editingCategory: SubCategory | null = null;
confirmAction: any;
confirmCategory: any;

  constructor(
    private subCategoryService: SubCategoryService,
    private mainCategoryService: MainCategoryService,
    private toastr: ToastrService,
    private modalService: NgbModal,
    private fb: FormBuilder
  ) {
    this.categoryForm = this.fb.group({
      category: ['', [Validators.required, Validators.maxLength(50)]],
      mainCategoryId: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadCategories();
    this.loadMainCategories();
  }

  loadCategories(page: number = 0) {
    this.loading = true;
    this.error = null;
    this.subCategoryService.getAllSubCategories(page, this.pageSize)
      .subscribe({
        next: (response) => {
          this.categories = response.content;
          this.currentPage = page;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load subcategories';
          this.loading = false;
          this.toastr.error('Failed to load subcategories');
        }
      });
  }

  loadMainCategories() {
    this.mainCategoryService.getAllMainCategories(0, 1000)
      .subscribe({
        next: (response) => {
          this.mainCategories = response.content.filter(mc => mc.status === 'active');
        },
        error: (error) => {
          this.toastr.error('Failed to load main categories');
        }
      });
  }

  onPageChange(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.loadCategories(page);
    }
  }

  openAddModal(content: any) {
    this.editingCategory = null;
    this.categoryForm.reset();
    this.modalService.open(content, { centered: true });
  }

  openEditModal(content: any, category: SubCategory) {
    this.editingCategory = category;
    this.categoryForm.patchValue({
      mainCategoryId: category.mainCategory.id,
      category: category.category
    });
    this.modalService.open(content, { centered: true });
  }

  openConfirmModal(content: any, category: SubCategory, action: 'delete' | 'activate') {
    this.confirmCategory = category;
    this.confirmAction = action;
    this.modalService.open(content, { centered: true });
  }

  onSubmit() {
    if (this.categoryForm.invalid || this.isSubmitting) return;

    const { mainCategoryId, category } = this.categoryForm.value;
    this.isSubmitting = true;

    if (this.editingCategory) {
      this.subCategoryService.updateSubCategory(this.editingCategory.id, category, mainCategoryId)
        .subscribe({
          next: () => {
            this.toastr.success('Subcategory updated successfully');
            this.modalService.dismissAll();
            this.loadCategories(this.currentPage);
            this.isSubmitting = false;
            this.editingCategory = null;
          },
          error: (error) => {
            this.toastr.error('Failed to update subcategory');
            this.isSubmitting = false;
          }
        });
    } else {
      this.subCategoryService.createSubCategory(mainCategoryId, category)
        .subscribe({
          next: () => {
            this.toastr.success('Subcategory added successfully');
            this.modalService.dismissAll();
            this.loadCategories(this.currentPage);
            this.isSubmitting = false;
          },
          error: (error) => {
            this.toastr.error('Failed to create subcategory');
            this.isSubmitting = false;
          }
        });
    }
  }

  confirmToggleStatus() {
    if (!this.confirmCategory || !this.confirmAction) return;

    const isDeleting = this.confirmAction === 'delete';
    const request = isDeleting
      ? this.subCategoryService.deleteSubCategory(this.confirmCategory.id)
      : this.subCategoryService.activateSubCategory(this.confirmCategory.id);

    request.subscribe({
      next: (response) => {
        this.toastr.success(`Subcategory ${isDeleting ? 'deleted' : 'activated'} successfully`);
        this.loadCategories(this.currentPage);
      },
      error: (error) => {
        this.toastr.error(`Failed to ${isDeleting ? 'delete' : 'activate'} subcategory`);
      }
    });
  }

  get mainCategoryField() {
    return this.categoryForm.get('mainCategoryId');
  }

  get categoryField() {
    return this.categoryForm.get('category');
  }
}
