package com.donyx.lifeops.financeiro.application.usecases.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionRepository;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.CreateTransactionCommand;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    CreateTransactionUseCase useCase;

    @Test
    @DisplayName("execute -> cria transação com createdAt do command quando informado e popula campos opcionais; chama settle quando settledAt != null")
    void execute_usesProvidedCreatedAt_andSetsAllOptionalFields_andSettles() {
        // given
        UserId ownerId = UserId.of(UUID.randomUUID());
        BigDecimal amount = new BigDecimal("123.45");
        TransactionType type = TransactionType.EXPENSE;

        Instant createdAt = Instant.parse("2026-02-01T10:00:00Z");
        boolean recurring = true;

        String description = "Mercado";
        String notes = "compras do mês";
        LocalDate dueDate = LocalDate.of(2026, 2, 10);

        CategoryId categoryId = mock(CategoryId.class);
        UUID catUuid = UUID.randomUUID();
        when(categoryId.asUuid()).thenReturn(catUuid);

        LocalDate settledAt = LocalDate.of(2026, 2, 11);

        CreateTransactionCommand command = mock(CreateTransactionCommand.class);
        when(command.ownerId()).thenReturn(ownerId);
        when(command.amount()).thenReturn(amount);
        when(command.type()).thenReturn(type);
        when(command.createdAt()).thenReturn(createdAt);
        when(command.recurring()).thenReturn(recurring);

        when(command.description()).thenReturn(description);
        when(command.notes()).thenReturn(notes);
        when(command.dueDate()).thenReturn(dueDate);
        when(command.categoryId()).thenReturn(categoryId);
        when(command.settledAt()).thenReturn(settledAt);

        // capturar o Transaction que vai para o repo
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);

        // repo devolve o mesmo objeto (simula persistência)
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        Transaction result = useCase.execute(command);

        // then
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();

        assertSame(saved, result);

        // createdAt deve ser o que veio no command (não Instant.now)
        assertEquals(createdAt, saved.createdAt());

        // campos opcionais
        assertEquals(description, saved.description());
        assertEquals(notes, saved.notes());
        assertEquals(dueDate, saved.dueDate());

        // categoryId (depende do teu domínio: pode ser CategoryId VO ou UUID interno)
        // aqui eu valido pelo UUID (porque o spec usa asUuid)
        assertNotNull(saved.categoryId());
        assertEquals(catUuid, saved.categoryId().asUuid());

        // settled
        // depende do teu domínio: pode ser status SETTLED, ou settledAt != null, etc.
        // o mínimo verificável sem conhecer teu domínio é: settledAt não pode ficar null
        assertNotNull(saved.settledAt());
        assertEquals(settledAt, saved.settledAt());
    }

    @Test
    @DisplayName("execute -> quando createdAt é null, usa Instant.now() (aproximado) e não seta notes quando notes é null")
    void execute_whenCreatedAtNull_usesNow_andSkipsNullNotes() {
        // given
        UserId ownerId = UserId.of(UUID.randomUUID());
        BigDecimal amount = new BigDecimal("10.00");
        TransactionType type = TransactionType.INCOME;

        CreateTransactionCommand command = mock(CreateTransactionCommand.class);
        when(command.ownerId()).thenReturn(ownerId);
        when(command.amount()).thenReturn(amount);
        when(command.type()).thenReturn(type);
        when(command.createdAt()).thenReturn(null);
        when(command.recurring()).thenReturn(false);

        when(command.description()).thenReturn("Salário");
        when(command.notes()).thenReturn(null);
        when(command.dueDate()).thenReturn(null);
        when(command.categoryId()).thenReturn(null);
        when(command.settledAt()).thenReturn(null);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();

        // when
        Transaction result = useCase.execute(command);

        Instant after = Instant.now();

        // then
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();

        assertSame(saved, result);

        // createdAt deve estar entre before e after (tolerância real)
        assertFalse(saved.createdAt().isBefore(before), "createdAt deveria ser >= before");
        assertFalse(saved.createdAt().isAfter(after), "createdAt deveria ser <= after");

        // notes deveria ficar null (porque command.notes() é null e o usecase não seta)
        assertNull(saved.notes());

        // opcionais não setados
        assertNull(saved.dueDate());
        assertNull(saved.categoryId());
        assertNull(saved.settledAt());
    }

    @Test
    @DisplayName("execute -> seta dueDate e categoryId quando informados, mesmo sem notes e sem settledAt")
    void execute_setsDueDateAndCategory_whenProvided() {
        // given
        UserId ownerId = UserId.of(UUID.randomUUID());
        BigDecimal amount = new BigDecimal("50.00");
        TransactionType type = TransactionType.EXPENSE;

        LocalDate dueDate = LocalDate.of(2026, 3, 5);

        CategoryId categoryId = mock(CategoryId.class);
        UUID catUuid = UUID.randomUUID();
        when(categoryId.asUuid()).thenReturn(catUuid);

        CreateTransactionCommand command = mock(CreateTransactionCommand.class);
        when(command.ownerId()).thenReturn(ownerId);
        when(command.amount()).thenReturn(amount);
        when(command.type()).thenReturn(type);
        when(command.createdAt()).thenReturn(Instant.parse("2026-03-01T00:00:00Z"));
        when(command.recurring()).thenReturn(false);

        when(command.description()).thenReturn("Conta de luz");
        when(command.notes()).thenReturn(null);
        when(command.dueDate()).thenReturn(dueDate);
        when(command.categoryId()).thenReturn(categoryId);
        when(command.settledAt()).thenReturn(null);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(command);

        // then
        verify(transactionRepository).save(txCaptor.capture());
        Transaction saved = txCaptor.getValue();

        assertEquals(dueDate, saved.dueDate());
        assertNotNull(saved.categoryId());
        assertEquals(catUuid, saved.categoryId().asUuid());

        // notes e settled continuam null
        assertNull(saved.notes());
        assertNull(saved.settledAt());
    }
}