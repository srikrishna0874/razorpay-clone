package com.springboot.razorpay.common.entity;

import jakarta.persistence.Embeddable;
import lombok.*;


@Embeddable
@NoArgsConstructor
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Money {

    private int amountUnits;
    private String currency;


    public static Money of(int amountUnits, String currency) {
        return new Money(amountUnits, currency);
    }

    public Money inr(int amountUnits) {
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other) {
        if(!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency already exists!");
        }

        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    public Money subtract(Money other) {
        if(!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency already exists!");
        }

        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }
}
