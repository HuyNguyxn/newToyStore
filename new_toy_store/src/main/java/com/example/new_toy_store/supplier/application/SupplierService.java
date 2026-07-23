package com.example.new_toy_store.supplier.application;

import com.example.new_toy_store.global.event.SupplierDeletedEvent;
import com.example.new_toy_store.global.event.SupplierStatusChangedEvent;
import com.example.new_toy_store.infrastructure.specification.SupplierSpecification;
import com.example.new_toy_store.supplier.application.dto.request.SupplierCreateRequest;
import com.example.new_toy_store.supplier.application.dto.request.SupplierFilterRequest;
import com.example.new_toy_store.supplier.application.dto.request.SupplierUpdateRequest;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierRepository;
import com.example.new_toy_store.supplier.domain.SupplierStatus;
import com.example.new_toy_store.supplier.domain.exception.*;
import com.example.new_toy_store.supplier.mapper.SupplierMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public SupplierService(SupplierRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> filterSuppliers(SupplierFilterRequest filterRequest, Pageable pageable) {
        return repository.findAll(SupplierSpecification.filter(filterRequest), pageable)
                .map(SupplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierDetails(Integer id) {
        Supplier supplier = getSupplierEntity(id);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getSuppliersByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(ids).stream()
                .map(SupplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplierResponse create(SupplierCreateRequest request) {
        Optional<Supplier> existing = repository.findByPhoneNumberIncludingDeleted(request.getPhoneNumber());
        if (existing.isPresent()) {
            if (existing.get().isDeleted()) {
                throw new SupplierDeletedConflictException(request.getPhoneNumber());
            }
            throw new DuplicateSupplierException(request.getPhoneNumber());
        }

        Supplier supplier = SupplierMapper.toEntity(request);
        repository.save(supplier);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Integer id, SupplierUpdateRequest request) {
        Supplier supplier = getSupplierEntity(id);

        repository.findByPhoneNumberIncludingDeleted(request.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        if (existing.isDeleted()) {
                            throw new SupplierDeletedConflictException(request.getPhoneNumber());
                        }
                        throw new DuplicateSupplierException(request.getPhoneNumber());
                    }
                });

        supplier.updateInfo(
                request.getName(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress()
        );

        repository.save(supplier);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        Supplier supplier = getSupplierEntity(id);
        int updatedRows = repository.softDeleteWithVersion(supplier.getId(), supplier.getVersion());
        verifyBulkCommandSucceeded(updatedRows, supplier.getId());
        eventPublisher.publishEvent(SupplierDeletedEvent.now(supplier.getId(), supplier.getPhoneNumber()));
    }

    @Transactional
    public void changeStatus(Integer id, String statusStr) {
        Supplier supplier = getSupplierEntity(id);
        SupplierStatus previousStatus = supplier.getStatus();
        SupplierStatus targetStatus = SupplierStatus.from(statusStr);

        if (previousStatus == targetStatus) {
            return;
        }

        int updatedRows = repository.updateStatusWithVersion(supplier.getId(), targetStatus, supplier.getVersion());
        verifyBulkCommandSucceeded(updatedRows, supplier.getId());
        eventPublisher.publishEvent(SupplierStatusChangedEvent.now(supplier.getId(), previousStatus, targetStatus));
    }

    @Transactional
    public void restore(Integer id) {
        Supplier supplier = repository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (!supplier.isDeleted()) {
            throw InvalidSupplierOperationException.stillActive(id);
        }
        supplier.restore();
        repository.save(supplier);
    }

    private Supplier getSupplierEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    private void verifyBulkCommandSucceeded(int updatedRows, Integer supplierId) {
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Supplier.class, supplierId);
        }
    }
}
