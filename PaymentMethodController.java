package com.prj.LoneHPManagement.Controller;

import com.prj.LoneHPManagement.Service.PaymentMethodService;
import com.prj.LoneHPManagement.model.dto.ApiResponse;
import com.prj.LoneHPManagement.model.dto.PagedResponse;
import com.prj.LoneHPManagement.model.entity.PaymentMethod;
import com.prj.LoneHPManagement.model.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;
    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createPaymentMethod(@PathVariable int userId,@RequestBody PaymentMethod method) {
        System.out.println(userId);
        System.out.println("receive dto"+method);
        try {
            PaymentMethod createdPaymentMethod = paymentMethodService.createPaymentMethod(method, userId);
            ApiResponse<PaymentMethod> response = ApiResponse.success(HttpStatus.CREATED.value(), "Payment method created successfully", createdPaymentMethod);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ServiceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentMethod>>> getAllPaymentMethods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<PaymentMethod> paymentMethodsPage = paymentMethodService.getAllPaymentMethods(page, size, sortBy);

        PagedResponse<PaymentMethod> pagedResponse = new PagedResponse<>(
                paymentMethodsPage.getContent(),
                paymentMethodsPage.getTotalPages(),
                paymentMethodsPage.getTotalElements(),
                paymentMethodsPage.getSize(),
                paymentMethodsPage.getNumber(),
                paymentMethodsPage.getNumberOfElements(),
                paymentMethodsPage.isFirst(),
                paymentMethodsPage.isLast(),
                paymentMethodsPage.isEmpty()
        );

        ApiResponse<PagedResponse<PaymentMethod>> response = ApiResponse.success(
                HttpStatus.OK.value(),
                "Payment methods fetched successfully",
                pagedResponse
        );

        return ResponseEntity.ok(response);
    }




    @GetMapping("/list/{id}")
    public ResponseEntity<?> getPaymentMethodById(@PathVariable int id) {
        try {
            PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(id);
            ApiResponse<PaymentMethod> response = ApiResponse.success(HttpStatus.OK.value(), "Payment method fetched successfully", paymentMethod);
            return ResponseEntity.ok(response);
        } catch (ServiceException ex) {
            ApiResponse<String> errorResponse = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePaymentMethod(@PathVariable int id, @RequestBody PaymentMethod paymentMethodDetails) {
        try {
            PaymentMethod updatedPaymentMethod = paymentMethodService.updatePaymentMethod(id, paymentMethodDetails);
            ApiResponse<PaymentMethod> response = ApiResponse.success(HttpStatus.OK.value(), "Payment method updated successfully", updatedPaymentMethod);
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable int id) {
        try {
            paymentMethodService.deletePaymentMethod(id);
            ApiResponse<String> response = ApiResponse.success(
                    HttpStatus.OK.value(),
                    "Payment method deleted successfully",
                    "Deleted"
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

    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restorePaymentMethod(@PathVariable int id) {
        try {
            paymentMethodService.restorePaymentMethod(id);
            ApiResponse<String> response = ApiResponse.success(
                    HttpStatus.OK.value(),
                    "Payment method restored successfully",
                    "Active"
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentMethodsByUserId(@PathVariable int userId) {
        try {
            List<PaymentMethod> paymentMethods = paymentMethodService.getPaymentMethodsByUserId(userId);
            ApiResponse<List<PaymentMethod>> response = ApiResponse.success(HttpStatus.OK.value(), "Payment methods retrieved successfully", paymentMethods);
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
