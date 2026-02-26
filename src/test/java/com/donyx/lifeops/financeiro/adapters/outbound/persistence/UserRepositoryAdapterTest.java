package com.donyx.lifeops.financeiro.adapters.outbound.persistence;

import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.JpaUserEntity;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.SpringDataJpaRepository;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.UserPersistenceMapper;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.UserRepositoryAdapter;
import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserRole;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryAdapterTest {

    private SpringDataJpaRepository repository;
    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataJpaRepository.class);
        adapter = new UserRepositoryAdapter(repository);
    }

    @Test
    @DisplayName("existsByName -> delega para existsActiveByName com status DELETED")
    void existsByName_delegatesWithDeleted() {
        when(repository.existsActiveByName("Bruno", UserStatus.DELETED)).thenReturn(true);

        boolean exists = adapter.existsByName("Bruno");

        assertTrue(exists);
        verify(repository).existsActiveByName("Bruno", UserStatus.DELETED);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("existsByEmail -> delega para existsActiveByEmail com status DELETED")
    void existsByEmail_delegatesWithDeleted() {
        when(repository.existsActiveByEmail("a@b.com", UserStatus.DELETED)).thenReturn(false);

        boolean exists = adapter.existsByEmail("a@b.com");

        assertFalse(exists);
        verify(repository).existsActiveByEmail("a@b.com", UserStatus.DELETED);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("save -> mapeia domain->entity, salva, e volta entity->domain")
    void save_mapsAndPersists() {
        User domainIn = mock(User.class);

        JpaUserEntity entityIn = new JpaUserEntity();
        JpaUserEntity entitySaved = new JpaUserEntity();

        User domainOut = mock(User.class);

        when(repository.save(entityIn)).thenReturn(entitySaved);

        try (MockedStatic<UserPersistenceMapper> mapper = mockStatic(UserPersistenceMapper.class)) {
            mapper.when(() -> UserPersistenceMapper.toEntity(domainIn)).thenReturn(entityIn);
            mapper.when(() -> UserPersistenceMapper.toDomain(entitySaved)).thenReturn(domainOut);

            User result = adapter.save(domainIn);

            assertSame(domainOut, result);

            mapper.verify(() -> UserPersistenceMapper.toEntity(domainIn));
            verify(repository).save(entityIn);
            mapper.verify(() -> UserPersistenceMapper.toDomain(entitySaved));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    @DisplayName("markDeletedById -> busca por id, seta status DELETED e salva")
    void markDeletedById_setsDeletedAndSaves() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.of(uuid);

        JpaUserEntity entity = new JpaUserEntity();
        entity.setId(uuid);
        entity.setStatus(UserStatus.ACTIVE);

        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        adapter.markDeletedById(id);

        assertEquals(UserStatus.DELETED, entity.getStatus());
        verify(repository).findById(uuid);
        verify(repository).save(entity);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("markDeletedById -> lança RuntimeException quando usuário não existe")
    void markDeletedById_notFound_throws() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.of(uuid);

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adapter.markDeletedById(id));
        assertEquals("User not found", ex.getMessage());

        verify(repository).findById(uuid);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("findByName -> delega para findActiveByName e mapeia entity->domain")
    void findByName_delegatesAndMaps() {
        JpaUserEntity entity = new JpaUserEntity();
        User domain = mock(User.class);

        when(repository.findActiveByName("Bruno", UserStatus.DELETED)).thenReturn(Optional.of(entity));

        try (MockedStatic<UserPersistenceMapper> mapper = mockStatic(UserPersistenceMapper.class)) {
            mapper.when(() -> UserPersistenceMapper.toDomain(entity)).thenReturn(domain);

            Optional<User> result = adapter.findByName("Bruno");

            assertTrue(result.isPresent());
            assertSame(domain, result.get());

            verify(repository).findActiveByName("Bruno", UserStatus.DELETED);
            mapper.verify(() -> UserPersistenceMapper.toDomain(entity));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    void findByEmail_returnsMappedUserWhenFound() {
        String email = "a@b.com";

        JpaUserEntity entity = new JpaUserEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Bruno"); // não pode ser vazio
        entity.setEmail(email);
        entity.setPasswordHash("hash"); // não pode ser vazio
        entity.setStatus(UserStatus.ACTIVE);
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-01-02T00:00:00Z"));
        entity.setUpdatedBy(null);
        entity.setRoles(Set.of(UserRole.USER)); // se existir na entidade

        when(repository.findActiveByEmail(email, UserStatus.DELETED))
                .thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().email());
        verify(repository).findActiveByEmail(email, UserStatus.DELETED);
    }
}