package com.donyx.lifeops.financeiro.adapters.outbound.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<JpaCategoryEntity, UUID> {

    List<JpaCategoryEntity> findAllByOwnerId(UUID ownerId);

    void deleteByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<JpaCategoryEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
