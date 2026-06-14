package com.springboot.razorpay.merchant.service;


import com.springboot.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.springboot.razorpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signUp(MerchantSignupRequest merchantSignupRequest);
}
