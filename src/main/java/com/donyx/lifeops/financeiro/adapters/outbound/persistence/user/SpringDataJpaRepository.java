package com.donyx.lifeops.financeiro.adapters.outbound.persistence.user;

import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJpaRepository extends JpaRepository<JpaUserEntity, UUID> {

    @Query("""
            select (count(u) > 0) from JpaUserEntity u
            where u.name = :name and u.status <> :deleted
            """)
    boolean existsActiveByName(@Param("name") String name,
                               @Param("deleted") UserStatus deleted);

    @Query("""
            select (count(u) > 0) from JpaUserEntity u
            where u.email = :email and u.status <> :deleted
            """)
    boolean existsActiveByEmail(@Param("email") String email,
                                @Param("deleted") UserStatus deleted);

    @Query("""
            select u from JpaUserEntity u
            where u.email = :email and u.status <> :deleted
            """)
    Optional<JpaUserEntity> findActiveByEmail(@Param("email") String email,
                                              @Param("deleted") UserStatus deleted);

    @Query("""
            select u from JpaUserEntity u
            where u.name = :name and u.status <> 'DELETED'
            """)
    Optional<JpaUserEntity> findActiveByName(@Param("name") String name,
                                             @Param("deleted") UserStatus deleted);
}
