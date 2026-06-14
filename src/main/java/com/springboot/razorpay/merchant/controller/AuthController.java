package com.springboot.razorpay.merchant.controller;

import com.springboot.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.springboot.razorpay.merchant.dto.response.MerchantResponse;
import com.springboot.razorpay.merchant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/signup")
    public ResponseEntity<MerchantResponse> signUp(@RequestBody @Valid MerchantSignupRequest merchantSignupRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.signUp(merchantSignupRequest)
        );
    }

}
