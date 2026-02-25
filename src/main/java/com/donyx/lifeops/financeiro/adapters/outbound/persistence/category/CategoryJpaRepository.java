package com.donyx.lifeops.financeiro.adapters.outbound.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<JpaCategoryEntity, UUID> {

    List<JpaCategoryEntity> findAllByOwnerId(UUID ownerId);

    @Transactional
    long deleteByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<JpaCategoryEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
