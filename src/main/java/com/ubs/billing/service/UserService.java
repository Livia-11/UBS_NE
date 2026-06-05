package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.AppUser;
import com.ubs.billing.entity.Customer;
import com.ubs.billing.enums.UserRole;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.exception.ResourceNotFoundException;
import com.ubs.billing.repository.AppUserRepository;
import com.ubs.billing.repository.CustomerRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final EmailService emailService;

    public UserService(
            AppUserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            TemporaryPasswordGenerator temporaryPasswordGenerator,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<Dtos.UserResponse> findAll() {
        return userRepository.findAll().stream().map(DtoMapper::user).toList();
    }

    @Transactional
    public Dtos.UserResponse create(Dtos.UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Email is already registered");
        }
        Customer customer = resolveCustomerForUser(request);
        AppUser user = new AppUser();
        apply(user, request);
        user.setCustomer(customer);
        // Admin-created accounts start with a generated password and must replace it after first login.
        String temporaryPassword = temporaryPasswordGenerator.generate();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        AppUser saved = userRepository.save(user);
        emailService.sendTemporaryCredentials(saved.getEmail(), saved.getFullNames(), temporaryPassword, saved.getRoles());
        return DtoMapper.user(saved);
    }

    @Transactional
    public Dtos.UserResponse update(Long id, Dtos.UserRequest request) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Set<?> previousRoles = new HashSet<>(user.getRoles());
        userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Email is already registered");
                });
        if (user.getCustomer() != null && !user.getCustomer().getNationalId().equals(request.nationalId())) {
            customerRepository.findByNationalId(request.nationalId())
                    .filter(existing -> !existing.getId().equals(user.getCustomer().getId()))
                    .ifPresent(existing -> {
                        throw new BusinessRuleException("Customer with this National ID already exists");
                    });
            user.getCustomer().setNationalId(request.nationalId());
        }
        apply(user, request);
        if (user.getCustomer() == null) {
            user.setCustomer(resolveCustomerForUser(request));
        }
        if (!previousRoles.equals(user.getRoles())) {
            emailService.sendRoleChanged(user.getEmail(), user.getFullNames(), user.getRoles());
        }
        return DtoMapper.user(user);
    }

    private void apply(AppUser user, Dtos.UserRequest request) {
        user.setFullNames(request.fullNames());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setStatus(request.status());
        HashSet<UserRole> roles = new HashSet<>(request.roles());
        roles.add(UserRole.ROLE_CUSTOMER);
        user.setRoles(roles);
        if (user.getCustomer() != null) {
            user.getCustomer().setFullNames(request.fullNames());
            user.getCustomer().setEmail(request.email());
            user.getCustomer().setPhoneNumber(request.phoneNumber());
            user.getCustomer().setAddress(request.address());
            user.getCustomer().setStatus(request.status());
        }
    }

    private Customer resolveCustomerForUser(Dtos.UserRequest request) {
        return customerRepository.findByNationalId(request.nationalId())
                .map(existing -> {
                    if (userRepository.existsByCustomerId(existing.getId())) {
                        throw new BusinessRuleException("This customer already has a system user account");
                    }
                    if (!existing.getEmail().equalsIgnoreCase(request.email())) {
                        throw new BusinessRuleException("National ID already belongs to a different customer email");
                    }
                    existing.setFullNames(request.fullNames());
                    existing.setPhoneNumber(request.phoneNumber());
                    existing.setAddress(request.address());
                    existing.setStatus(request.status());
                    return existing;
                })
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setFullNames(request.fullNames());
                    customer.setNationalId(request.nationalId());
                    customer.setEmail(request.email());
                    customer.setPhoneNumber(request.phoneNumber());
                    customer.setAddress(request.address());
                    customer.setStatus(request.status());
                    return customerRepository.save(customer);
                });
    }
}
