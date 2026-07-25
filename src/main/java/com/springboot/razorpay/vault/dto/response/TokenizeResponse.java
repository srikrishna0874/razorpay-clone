package com.springboot.razorpay.vault.dto.response;

import com.springboot.razorpay.common.enums.CardBrand;

public record TokenizeResponse(
        String token,

        String lastFour,

        CardBrand brand,

        Integer expiryMonth,

        Integer expiryYear
) {
}
