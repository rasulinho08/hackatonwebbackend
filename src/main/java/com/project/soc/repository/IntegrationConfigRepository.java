package com.project.soc.repository;

import com.project.soc.entity.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, Long> {

    Optional<IntegrationConfig> findByProviderNameIgnoreCase(String providerName);
}
