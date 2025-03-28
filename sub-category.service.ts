import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SubCategory } from '../models/sub-category.model';
import { ApiResponse, PagedResponse } from '../models/common.types';

@Injectable({
  providedIn: 'root'
})
export class SubCategoryService {
  private apiUrl = `${environment.apiUrl}/subCategory`;

  constructor(private http: HttpClient) {}

  getAllSubCategories(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id'
  ): Observable<PagedResponse<SubCategory>> {
    const params = {
      page: page.toString(),
      size: size.toString(),
      sortBy: sortBy
    };

    return this.http.get<ApiResponse<PagedResponse<SubCategory>>>(`${this.apiUrl}/getAll`, { params })
      .pipe(
        map(response => response.data)
      );
  }

  createSubCategory(mainCategoryId: number, category: string): Observable<SubCategory> {
    return this.http.post<SubCategory>(`${this.apiUrl}/createSubCat/${mainCategoryId}`, { category });
  }

  updateSubCategory(id: number, category: string, mainCategoryId: number): Observable<SubCategory> {
    const payload = {
      category,
      mainCategory: { id: mainCategoryId } // Backend expects a nested MainCategory object
    };
    return this.http.put<SubCategory>(`${this.apiUrl}/update/${id}`, payload);
  }

  deleteSubCategory(id: number): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/delete/${id}`, null);
}
activateSubCategory(id: number): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/activate/${id}`, null);
}
} 