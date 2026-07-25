package com.springboot.razorpay.vault.entity;

import com.springboot.razorpay.common.entity.BaseEntity;
import com.springboot.razorpay.common.enums.CardBrand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vault_card")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VaultCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Column(nullable = false, length = 6)
    private String bin; //first 6 digits


    @Column(nullable = false)
    private byte[] encryptedPan;

    @Column(nullable = false)
    private byte[] encryptedDek;

    @Column(nullable = false)
    private CardBrand brand; //VISA, MasterCard, Rupay

    @Column(nullable = false)
    private String expiryMonth;

    @Column(nullable = false)
    private String expiryYear;

    @Column(nullable = false)
    private String cardHolderName;


    private LocalDateTime deletedAt;
}

// DEK: another key for pan (locker key)
// PAN: real pan (gold inside locker)