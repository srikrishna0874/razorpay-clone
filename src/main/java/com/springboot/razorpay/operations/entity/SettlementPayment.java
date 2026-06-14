package com.springboot.razorpay.operations.entity;

import jakarta.persistence.*;

@Table
@Entity(name = "settlement_payment")
public class SettlementPayment {

    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;


}
