package com.springboot.razorpay.vault.entity;

import com.springboot.razorpay.common.entity.BaseEntity;
import com.springboot.razorpay.merchant.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_token")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_card_id", nullable = false)
    private VaultCard card;

    @Column(nullable = false)
    private UUID customer;

    @Column(nullable = false)
    private UUID merchant;

    private LocalDateTime revokedAt;


}
