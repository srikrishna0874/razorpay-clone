package com.springboot.razorpay.merchant.service.impl;

import com.springboot.razorpay.common.exception.ResourceNotFoundException;
import com.springboot.razorpay.merchant.entity.Customer;
import com.springboot.razorpay.merchant.entity.Merchant;
import com.springboot.razorpay.merchant.repository.CustomerRepository;
import com.springboot.razorpay.merchant.repository.MerchantRepository;
import com.springboot.razorpay.merchant.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final MerchantRepository merchantRepository;

    @Override
    public UUID findOrCreate(UUID merchantId, String email, String name, String phoneNumber) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return customerRepository.findByMerchant_IdAndEmail(merchantId, email)
                .map(Customer::getId)
                .orElseGet(() -> createNewCustomer(merchantId, email, name, phoneNumber));
    }

    private UUID createNewCustomer(UUID merchantId, String email, String name, String phoneNumber) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        Customer customer = Customer.builder()
                .merchant(merchant)
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();

        customer = customerRepository.save(customer);

        log.info("Created new customer with id {}", customer);

        return customer.getId();
    }
}
