package com.flowbiz.controller;

import com.flowbiz.model.Enquiry;
import com.flowbiz.repository.EnquiryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryController {

    private final EnquiryRepository enquiries;

    public EnquiryController(EnquiryRepository enquiries) {
        this.enquiries = enquiries;
    }

    public record EnquiryRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            String phone,
            String company,
            String product,
            @NotBlank String message) {}

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EnquiryRequest req) {
        Enquiry e = new Enquiry();
        e.setName(req.name().trim());
        e.setEmail(req.email().trim().toLowerCase());
        e.setPhone(req.phone());
        e.setCompany(req.company());
        e.setProduct(req.product());
        e.setMessage(req.message().trim());
        enquiries.save(e); // saved in MySQL -> "enquiries" table
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thank you! Your enquiry has been received."));
    }
}
