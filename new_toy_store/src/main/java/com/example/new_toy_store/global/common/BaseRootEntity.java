package com.example.new_toy_store.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class BaseRootEntity extends BaseSoftDeleteEntity {

    @Version
    @Column(name = "version")
    private Long version;

    public Long getVersion() { return version; }
}