import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { MainCategory, MainCategoryStatus } from '../models/main-category.model';
import { ApiResponse, PagedResponse } from '../models/common.types';

@Injectable({
  providedIn: 'root'
})
export class MainCategoryService {
  private apiUrl = `${environment.apiUrl}/mainCategory`;

  constructor(private http: HttpClient) {}

  getAllMainCategories(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id'
  ): Observable<PagedResponse<MainCategory>> {
    const params = {
      page: page.toString(),
      size: size.toString(),
      sortBy: sortBy
    };

    return this.http.get<ApiResponse<PagedResponse<MainCategory>>>(`${this.apiUrl}/getAll`, { params })
      .pipe(
        map(response => response.data)
      );
  }

  createMainCategory(category: string): Observable<MainCategory> {
    return this.http.post<ApiResponse<MainCategory>>(`${this.apiUrl}/createMainCat`, { category })
      .pipe(
        map(response => response.data)
      );
  }

  updateMainCategory(id: number, category: string): Observable<MainCategory> {
    const payload = { id, category }; // Construct full object
    return this.http.put<ApiResponse<MainCategory>>(`${this.apiUrl}/update/${id}`, payload)
      .pipe(
        map(response => response.data)
      );
  }

  softDelete(id: number): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/delete/${id}`, null, { observe: 'body' });
}

activate(id: number): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/activate/${id}`, null);
}





} 