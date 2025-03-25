import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { CompanyService } from '../../../services/company.service';
import { Company } from '../../../models/company.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-business-update',
  templateUrl: './business-update.component.html',
  styleUrls: ['./business-update.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class BusinessUpdateComponent implements OnInit {
  @Input() company!: Company;
  businessForm!: FormGroup;
  loading = false;

  // Address data
  townshipData: any = {};
  states: string[] = [];
  townships: string[] = [];
  cities: string[] = [];

  constructor(
    private formBuilder: FormBuilder,
    private companyService: CompanyService,
    private activeModal: NgbActiveModal,
    private toastr: ToastrService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadTownshipData();
  }

  private loadTownshipData() {
    this.http.get<any>('assets/myanmar-townships.json').subscribe({
      next: (data) => {
        this.townshipData = data;
        this.states = Object.keys(this.townshipData);
        
        // Initialize townships and cities based on current values
        const currentState = this.company?.address?.state;
        if (currentState && this.townshipData[currentState]) {
          this.townships = Object.keys(this.townshipData[currentState]);
          const currentTownship = this.company?.address?.township;
          if (currentTownship && this.townshipData[currentState][currentTownship]) {
            this.cities = this.townshipData[currentState][currentTownship];
          }
        }
      },
      error: (error) => {
        console.error('Error loading townships:', error);
        this.toastr.error('Failed to load address data');
      }
    });
  }

  private initForm(): void {

    const address = this.company?.address || { state: '', township: '', city: '', additionalAddress: '' };

    this.businessForm = this.formBuilder.group({
      name: [this.company?.name || '', [Validators.required]],
      companyType: [this.company?.companyType || '', [Validators.required]],
      category: [this.company?.category || '', [Validators.required]],
      businessType: [this.company?.businessType || '', [Validators.required]],
      registrationDate: [this.formatDateForInput(this.company?.registrationDate), [Validators.required]],
      licenseNumber: [this.company?.licenseNumber || '', [Validators.required]],
      licenseIssueDate: [this.formatDate(this.company?.licenseIssueDate), [Validators.required]],
      licenseExpiryDate: [this.formatDate(this.company?.licenseExpiryDate), [Validators.required]],
      phoneNumber: [this.company?.phoneNumber || '', [Validators.required]],
      address: this.formBuilder.group({
        state: [address.state || '', [Validators.required]],
        township: [address.township || '', [Validators.required]],
        city: [address.city || '', [Validators.required]],
        additionalAddress: [address.additionalAddress || '']
      })
    });

    // Setup address change listeners
    const addressForm = this.businessForm.get('address');
    if (addressForm) {
      addressForm.get('state')?.valueChanges.subscribe(state => {
        if (state !== this.company.address.state) {
          // Only reset if the state has changed from the original
          this.onStateChange(true);
        } else {
          this.onStateChange(false);
        }
      });

      addressForm.get('township')?.valueChanges.subscribe(township => {
        if (township !== this.company.address.township) {
          // Only reset if the township has changed from the original
          this.onTownshipChange(true);
        } else {
          this.onTownshipChange(false);
        }
      });
    }
  }

  private formatDate(date: Date | string): string {
    if (!date) return '';
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  }

  private formatDateForInput(date?: Date | string): string {
    // For datetime-local input, include time (LocalDateTime compatibility)
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().slice(0, 16); // e.g., "2025-03-24T00:00"
  }

  onStateChange(resetDependents: boolean = true): void {
    const selectedState = this.businessForm.get('address.state')?.value;
    if (selectedState && this.townshipData[selectedState]) {
      this.townships = Object.keys(this.townshipData[selectedState]);
      
      if (resetDependents) {
        this.businessForm.patchValue({
          address: {
            township: '',
            city: ''
          }
        });
        this.cities = [];
      }
    }
  }

  onTownshipChange(resetCity: boolean = true): void {
    const selectedState = this.businessForm.get('address.state')?.value;
    const selectedTownship = this.businessForm.get('address.township')?.value;
    
    if (selectedState && selectedTownship) {
      const cities = this.townshipData[selectedState]?.[selectedTownship];
      if (cities) {
        this.cities = cities;
        
        if (resetCity) {
          this.businessForm.patchValue({
            address: {
              city: ''
            }
          });
        }
      }
    }
  }

  isFieldInvalid(field: string): boolean {
    const formControl = this.businessForm.get(field);
    return formControl ? (formControl.invalid && (formControl.dirty || formControl.touched)) : false;
  }

  getErrorMessage(field: string): string {
    const control = this.businessForm.get(field);
    if (control?.errors) {
      if (control.errors['required']) {
          return `${field.charAt(0).toUpperCase()}${field.slice(1)} is required`;
        }
    }
    return '';
  }

  onSubmit(): void {
    if (this.businessForm.valid) {
      this.loading = true;
      const formValue = this.businessForm.value;
      const updatedData = {
        id: this.company.id,
        name: formValue.name,
        companyType: formValue.companyType,
        category: formValue.category,
        businessType: formValue.businessType,
        registrationDate: new Date(formValue.registrationDate).toISOString(), // LocalDateTime
        licenseNumber: formValue.licenseNumber,
        licenseIssueDate: formValue.licenseIssueDate, // YYYY-MM-DD
        licenseExpiryDate: formValue.licenseExpiryDate, // YYYY-MM-DD
        phoneNumber: formValue.phoneNumber,
        state: formValue.address.state,
        township: formValue.address.township,
        city: formValue.address.city,
        address: formValue.address.additionalAddress || '',
        createdUserId: this.company.createdUser?.id,
        cifId: this.company.cif?.id
      };
  
      this.companyService.updateCompany(this.company.id, updatedData).subscribe({
        next: (response) => {
          this.toastr.success('Business information updated successfully');
          this.activeModal.close(response);
        },
        error: (error) => {
          console.error('Error updating business:', error);
          this.toastr.error(error.message || 'Failed to update business information');
        },
        complete: () => {
          this.loading = false;
        }
      });
    } else {
      this.businessForm.markAllAsTouched();
      this.toastr.error('Please fill all required fields');
    }
  }

  onCancel(): void {
    this.activeModal.dismiss();
  }
}
