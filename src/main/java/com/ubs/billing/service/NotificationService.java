package com.ubs.billing.service;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.repository.NotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Dtos.NotificationResponse> findAll() {
        return notificationRepository.findAll().stream().map(DtoMapper::notification).toList();
    }

    @Transactional(readOnly = true)
    public List<Dtos.NotificationResponse> findByCustomer(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(DtoMapper::notification)
                .toList();
    }
}
