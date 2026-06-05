package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.Customer;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.exception.ResourceNotFoundException;
import com.ubs.billing.repository.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Dtos.CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(DtoMapper::customer).toList();
    }

    @Transactional
    public Dtos.CustomerResponse create(Dtos.CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.nationalId())) {
            throw new BusinessRuleException("Customer with this National ID already exists");
        }
        Customer customer = new Customer();
        apply(customer, request);
        return DtoMapper.customer(customerRepository.save(customer));
    }

    @Transactional
    public Dtos.CustomerResponse update(Long id, Dtos.CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customerRepository.findByNationalId(request.nationalId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Customer with this National ID already exists");
                });
        apply(customer, request);
        return DtoMapper.customer(customer);
    }

    public Customer requireCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private void apply(Customer customer, Dtos.CustomerRequest request) {
        customer.setFullNames(request.fullNames());
        customer.setNationalId(request.nationalId());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setAddress(request.address());
        customer.setStatus(request.status());
    }
}
