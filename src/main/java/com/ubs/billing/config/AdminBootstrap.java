package com.ubs.billing.config;

import com.ubs.billing.entity.AppUser;
import com.ubs.billing.entity.Customer;
import com.ubs.billing.enums.AccountStatus;
import com.ubs.billing.enums.UserRole;
import com.ubs.billing.repository.AppUserRepository;
import com.ubs.billing.repository.CustomerRepository;
import com.ubs.billing.service.EmailService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private static final String BOOTSTRAP_ADMIN_NATIONAL_ID = "0000000000000001";

    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String adminEmail;
    private final String bootstrapPassword;

    public AdminBootstrap(
            AppUserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.bootstrap-password}") String bootstrapPassword
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.adminEmail = adminEmail;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(String... args) {
        var existingAdmin = userRepository.findByEmail(adminEmail);
        if (existingAdmin.isPresent()) {
            AppUser admin = existingAdmin.get();
            if (admin.getCustomer() == null) {
                admin.setCustomer(resolveBootstrapCustomer());
                admin.getRoles().add(UserRole.ROLE_CUSTOMER);
                userRepository.save(admin);
            }
            return;
        }

        Customer customer = resolveBootstrapCustomer();
        AppUser admin = new AppUser();
        admin.setFullNames("System Administrator");
        admin.setEmail(adminEmail);
        admin.setPhoneNumber("0780000000");
        admin.setPassword(passwordEncoder.encode(bootstrapPassword));
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setRoles(Set.of(UserRole.ROLE_ADMIN, UserRole.ROLE_CUSTOMER));
        admin.setMustChangePassword(true);
        admin.setCustomer(customer);
        AppUser saved = userRepository.save(admin);

        emailService.sendTemporaryCredentials(saved.getEmail(), saved.getFullNames(), bootstrapPassword, saved.getRoles());
    }

    private Customer resolveBootstrapCustomer() {
        return customerRepository.findByNationalId(BOOTSTRAP_ADMIN_NATIONAL_ID)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setFullNames("System Administrator");
                    customer.setNationalId(BOOTSTRAP_ADMIN_NATIONAL_ID);
                    customer.setEmail(adminEmail);
                    customer.setPhoneNumber("0780000000");
                    customer.setAddress("Kigali");
                    customer.setStatus(AccountStatus.ACTIVE);
                    return customerRepository.save(customer);
                });
    }
}
