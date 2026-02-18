package com.donyx.lifeops.financeiro.adapters.outbound.persistance;

import com.donyx.lifeops.financeiro.domain.user.User;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.donyx.lifeops.financeiro.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
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

        try (MockedStatic<UserPersistanceMapper> mapper = mockStatic(UserPersistanceMapper.class)) {
            mapper.when(() -> UserPersistanceMapper.toEntity(domainIn)).thenReturn(entityIn);
            mapper.when(() -> UserPersistanceMapper.toDomain(entitySaved)).thenReturn(domainOut);

            User result = adapter.save(domainIn);

            assertSame(domainOut, result);

            mapper.verify(() -> UserPersistanceMapper.toEntity(domainIn));
            verify(repository).save(entityIn);
            mapper.verify(() -> UserPersistanceMapper.toDomain(entitySaved));
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

        try (MockedStatic<UserPersistanceMapper> mapper = mockStatic(UserPersistanceMapper.class)) {
            mapper.when(() -> UserPersistanceMapper.toDomain(entity)).thenReturn(domain);

            Optional<User> result = adapter.findByName("Bruno");

            assertTrue(result.isPresent());
            assertSame(domain, result.get());

            verify(repository).findActiveByName("Bruno", UserStatus.DELETED);
            mapper.verify(() -> UserPersistanceMapper.toDomain(entity));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    @DisplayName("findByEmail -> (atual) retorna Optional.empty (teste denuncia implementação faltando)")
    void findByEmail_currentlyEmpty() {
        Optional<User> result = adapter.findByEmail("a@b.com");
        assertTrue(result.isEmpty());
        verifyNoInteractions(repository);
    }
}