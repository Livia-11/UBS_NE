package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.entity.AppUser;
import com.ubs.billing.entity.Customer;
import com.ubs.billing.enums.AccountStatus;
import com.ubs.billing.enums.UserRole;
import com.ubs.billing.exception.BusinessRuleException;
import com.ubs.billing.repository.AppUserRepository;
import com.ubs.billing.repository.CustomerRepository;
import com.ubs.billing.security.JwtService;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;

    public AuthService(
            AppUserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService,
            OtpGenerator otpGenerator
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.otpGenerator = otpGenerator;
    }

    @Transactional
    public Dtos.AuthResponse signup(Dtos.SignupRequest request) {
        var existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            return attachCustomerProfileToExistingUser(existingUser.get(), request);
        }

        Customer customer = resolveCustomerForSignup(request);
        AppUser user = new AppUser();
        user.setFullNames(request.fullNames());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.INACTIVE);
        user.setRoles(Set.of(UserRole.ROLE_CUSTOMER));
        user.setMustChangePassword(false);
        user.setCustomer(customer);
        String otp = otpGenerator.generate();
        user.setEmailVerificationOtp(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        AppUser saved = userRepository.save(user);

        emailService.sendCustomerSignupOtp(saved.getEmail(), saved.getFullNames(), otp);
        return new Dtos.AuthResponse(null, saved.getEmail(), saved.getRoles(), false, "Signup successful. Check your email for the OTP before logging in.");
    }

    @Transactional(readOnly = true)
    public Dtos.AuthResponse login(Dtos.LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));
        if (user.getStatus() != AccountStatus.ACTIVE) {
            if (user.getEmailVerificationOtp() != null) {
                throw new BusinessRuleException("Account is not verified. Please verify the OTP sent to your email before login.");
            }
            throw new BusinessRuleException("User account is inactive");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String token = jwtService.generateToken(userDetails(user));
        String message = user.isMustChangePassword()
                ? "Login successful. You must change your temporary password before using other secured APIs."
                : "Login successful";
        return new Dtos.AuthResponse(token, user.getEmail(), user.getRoles(), user.isMustChangePassword(), message);
    }

    @Transactional
    public Dtos.MessageResponse verifyOtp(Dtos.VerifyOtpRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or OTP"));
        if (user.getEmailVerificationOtp() == null) {
            return new Dtos.MessageResponse("Account is already verified.");
        }
        if (user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("OTP has expired. Please sign up again or ask support to resend verification.");
        }
        if (!user.getEmailVerificationOtp().equals(request.otp())) {
            throw new BusinessRuleException("Invalid OTP");
        }
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerificationOtp(null);
        user.setOtpExpiresAt(null);
        if (user.getCustomer() != null) {
            user.getCustomer().setStatus(AccountStatus.ACTIVE);
        }
        emailService.sendWelcomeCustomerSignup(user.getEmail(), user.getFullNames());
        return new Dtos.MessageResponse("Account verified successfully. You can now log in.");
    }

    @Transactional
    public Dtos.MessageResponse changePassword(Authentication authentication, Dtos.ChangePasswordRequest request) {
        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessRuleException("Authenticated user was not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessRuleException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        return new Dtos.MessageResponse("Password changed successfully. You can now use the system.");
    }

    private org.springframework.security.core.userdetails.UserDetails userDetails(AppUser user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .build();
    }

    private Dtos.AuthResponse attachCustomerProfileToExistingUser(AppUser user, Dtos.SignupRequest request) {
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessRuleException("Email is already registered. Use the existing account password to add a customer profile.");
        }
        if (user.getCustomer() != null) {
            throw new BusinessRuleException("This user is already linked to a customer profile");
        }
        Customer customer = resolveCustomerForSignup(request);
        customer.setStatus(AccountStatus.ACTIVE);
        user.setCustomer(customer);
        user.getRoles().add(UserRole.ROLE_CUSTOMER);
        user.setStatus(AccountStatus.ACTIVE);
        emailService.sendWelcomeCustomerSignup(user.getEmail(), user.getFullNames());
        return new Dtos.AuthResponse(null, user.getEmail(), user.getRoles(), user.isMustChangePassword(), "Customer profile linked to existing user account.");
    }

    private Customer resolveCustomerForSignup(Dtos.SignupRequest request) {
        return customerRepository.findByNationalId(request.nationalId())
                .map(existing -> {
                    if (userRepository.existsByCustomerId(existing.getId())) {
                        throw new BusinessRuleException("Customer with this National ID already has a user account");
                    }
                    if (!existing.getEmail().equalsIgnoreCase(request.email())) {
                        throw new BusinessRuleException("National ID already belongs to a different customer email");
                    }
                    existing.setFullNames(request.fullNames());
                    existing.setPhoneNumber(request.phoneNumber());
                    existing.setAddress(request.address());
                    existing.setStatus(AccountStatus.INACTIVE);
                    return existing;
                })
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setFullNames(request.fullNames());
                    customer.setNationalId(request.nationalId());
                    customer.setEmail(request.email());
                    customer.setPhoneNumber(request.phoneNumber());
                    customer.setAddress(request.address());
                    customer.setStatus(AccountStatus.INACTIVE);
                    return customerRepository.save(customer);
                });
    }
}
