package com.example.new_toy_store.logistics.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentTrackingLogRepository extends JpaRepository<ShipmentTrackingLog, Integer> {

    Page<ShipmentTrackingLog> findByShipmentId(Integer shipmentId, Pageable pageable);
}
