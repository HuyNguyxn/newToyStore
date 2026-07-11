package com.example.new_toy_store.supplier.application;

import com.example.new_toy_store.supplier.application.dto.request.SupplierFilterRequest;
import com.example.new_toy_store.supplier.application.dto.request.SupplierRequest;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierRepository;
import com.example.new_toy_store.supplier.domain.SupplierStatus;
import com.example.new_toy_store.supplier.infrastructure.specification.SupplierSpecification;
import com.example.new_toy_store.supplier.mapper.SupplierMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }
    @Transactional(readOnly = true)
    public Page<SupplierResponse> filterSuppliers(SupplierFilterRequest filterRequest, Pageable pageable) {
        return repository.findAll(SupplierSpecification.filter(filterRequest), pageable)
                .map(SupplierMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierDetails(Integer id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
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
    public SupplierResponse create(SupplierRequest request) {
        Optional<Supplier> existing = repository.findByPhoneNumberIncludingDeleted(request.getPhoneNumber());
        if (existing.isPresent()) {
            if (existing.get().isDeleted()) {
                throw new IllegalStateException("Số điện thoại này thuộc về một nhà cung cấp đã bị xóa. Vui lòng khôi phục lại dữ liệu cũ thay vì tạo mới.");
            }
            throw new IllegalArgumentException("Số điện thoại nhà cung cấp đã tồn tại trong hệ thống.");
        }

        Supplier supplier = SupplierMapper.toEntity(request);
        repository.save(supplier);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Integer id, SupplierRequest request) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
        repository.findByPhoneNumberIncludingDeleted(request.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi nhà cung cấp khác (bao gồm cả dữ liệu đã xóa)");
                    }
                });

        supplier.updateInfo(
                request.getName(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress()
        );

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            supplier.setStatus(SupplierStatus.from(request.getStatus()));
        }

        repository.save(supplier);
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
        supplier.delete();
        repository.save(supplier);
    }

    @Transactional
    public void changeStatus(Integer id, String statusStr) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
        supplier.setStatus(SupplierStatus.from(statusStr));
        repository.save(supplier);
    }

    @Transactional
    public void restore(Integer id) {
        Supplier supplier = repository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));

        if (!supplier.isDeleted()) {
            throw new IllegalStateException("Nhà cung cấp này vẫn đang hoạt động.");
        }
        supplier.restore();
        repository.save(supplier);
    }
}