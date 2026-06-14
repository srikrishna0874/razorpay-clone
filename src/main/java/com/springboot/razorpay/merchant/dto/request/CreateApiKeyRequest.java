package com.springboot.razorpay.merchant.dto.request;

import com.springboot.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
