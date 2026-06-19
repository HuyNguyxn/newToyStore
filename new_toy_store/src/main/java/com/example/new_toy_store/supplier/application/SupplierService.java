package com.example.new_toy_store.supplier.application;

import com.example.new_toy_store.supplier.application.dto.request.SupplierRequest;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierRepository;
import com.example.new_toy_store.supplier.domain.SupplierStatus;
import com.example.new_toy_store.supplier.mapper.SupplierMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        return repository.findAll(pageable).map(SupplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> searchSuppliersByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable).map(SupplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierDetails(Integer id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (repository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại nhà cung cấp đã tồn tại");
        }
        Supplier supplier = SupplierMapper.toEntity(request);
        repository.save(supplier);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Integer id, SupplierRequest request) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

        repository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi nhà cung cấp khác");
                    }
                });

        supplier.updateInfo(
                request.getName(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress()
        );

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            supplier.setStatus(SupplierStatus.valueOf(request.getStatus().toUpperCase()));
        }

        repository.save(supplier);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        supplier.delete();
        repository.save(supplier);
    }
}