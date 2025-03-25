import { Component } from '@angular/core';
import { PaymentMethod } from 'src/app/models/payment-method.model';
import { PaymentMethodService } from 'src/app/services/payment-method.service';

@Component({
  selector: 'app-payment-method-create',
  standalone: false,
  templateUrl: './payment-method-create.component.html',
  styleUrl: './payment-method-create.component.css'
})
export class PaymentMethodCreateComponent {

  paymentMethod: Partial<PaymentMethod> = { paymentType: '' };
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private paymentMethodService: PaymentMethodService) {}

  createPaymentMethod(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.paymentMethod.paymentType?.trim()) {
      this.errorMessage = 'Payment type is required.';
      return;
    }

    this.paymentMethodService.createPaymentMethod(this.paymentMethod).subscribe({
      next: (response) => {
        this.successMessage = response.message; // "Payment method created successfully"
        this.paymentMethod = { paymentType: '' }; // Reset form
      },
      error: (err) => {
        console.error('Error:', err);
        this.errorMessage = err.message || 'Error creating payment method';
      },
    });
  }

  // Optional: Clear messages after a delay
  clearMessages(): void {
    setTimeout(() => {
      this.successMessage = '';
      this.errorMessage = '';
    }, 3000); // Clear after 3 seconds
  }

}
