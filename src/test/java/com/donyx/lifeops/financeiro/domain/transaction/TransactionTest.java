package com.donyx.lifeops.financeiro.domain.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TransactionTest {

    private static final UserId OWNER = UserId.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    private static final TransactionId TX_ID = TransactionId.of(UUID.fromString("22222222-2222-2222-2222-222222222222"));
    private static final Instant CREATED_AT = Instant.parse("2026-02-20T10:00:00Z"); // UTC
    private static final LocalDate CREATED_DATE_UTC = CREATED_AT.atZone(ZoneOffset.UTC).toLocalDate();

    @Test
    @DisplayName("constructor -> status começa como PENDING")
    void constructor_setsPendingStatus() {
        Transaction tx = new Transaction(TX_ID, OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        assertThat(tx.status()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("constructor -> lança quando amount <= 0")
    void constructor_amountMustBePositive() {
        BigDecimal zero = new BigDecimal("0.00");
        BigDecimal minusOne = new BigDecimal("-1.00");
        assertThatThrownBy(() ->
                new Transaction(TX_ID, OWNER, zero, TransactionType.EXPENSE, CREATED_AT, false)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");

        assertThatThrownBy(() ->
                new Transaction(TX_ID, OWNER, minusOne, TransactionType.EXPENSE, CREATED_AT, false)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("hydrate -> lança quando description é blank")
    void hydrate_blankDescription_throws() {
        BigDecimal ten = new BigDecimal("10.00");
        assertThatThrownBy(() ->
                Transaction.hydrate(
                        TX_ID, OWNER, ten, TransactionType.EXPENSE, CREATED_AT,
                        "   ", null, null, null, TransactionStatus.PENDING, null, false
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description cannot be blank");
    }

    @Test
    @DisplayName("hydrate -> lança quando notes é blank")
    void hydrate_blankNotes_throws() {
        BigDecimal twenty = new BigDecimal("20.00");
        assertThatThrownBy(() ->
                Transaction.hydrate(
                        TX_ID, OWNER, twenty, TransactionType.EXPENSE, CREATED_AT,
                        null, "   ", null, null, TransactionStatus.PENDING, null, false
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notes cannot be blank");
    }

    @Test
    @DisplayName("hydrate -> lança quando status é null")
    void hydrate_nullStatus_throws() {
        BigDecimal twenty = new BigDecimal("20.00");
        assertThatThrownBy(() ->
                Transaction.hydrate(
                        TX_ID, OWNER, twenty, TransactionType.EXPENSE, CREATED_AT,
                        null, null, null, null, null, null, false
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TransactionStatus cannot be null");
    }

    @Test
    @DisplayName("changeAmount -> altera quando PENDING")
    void changeAmount_pending_ok() {
        Transaction tx = Transaction.hydrate(
                TX_ID, OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT,
                null, null, null, null, TransactionStatus.PENDING, null, false
        );

        tx.changeAmount(new BigDecimal("99.90"));
        assertThat(tx.amount()).isEqualByComparingTo("99.90");
    }

    @Test
    @DisplayName("changeAmount -> lança quando newAmount <= 0")
    void changeAmount_nonPositive_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        BigDecimal zero = new BigDecimal("0.00");
        BigDecimal minusOne = new BigDecimal("-1.00");
        assertThatThrownBy(() -> tx.changeAmount(zero))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount must be positive");

        assertThatThrownBy(() -> tx.changeAmount(minusOne))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("changeAmount -> lança quando COMPLETED")
    void changeAmount_completed_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.settle(CREATED_DATE_UTC); // vira COMPLETED

        BigDecimal twenty = new BigDecimal("20.00");
        assertThatThrownBy(() -> tx.changeAmount(twenty))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot change amount of a settled transaction");
    }

    @Test
    @DisplayName("settle -> completa e seta settledAt")
    void settle_ok() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);

        LocalDate settleDate = CREATED_DATE_UTC.plusDays(1);
        tx.settle(settleDate);

        assertThat(tx.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(tx.settledAt()).isEqualTo(settleDate);
    }

    @Test
    @DisplayName("settle -> lança quando date < createdAt(UTC)")
    void settle_beforeCreated_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);

        LocalDate before = CREATED_DATE_UTC.minusDays(1);

        assertThatThrownBy(() -> tx.settle(before))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot settle before createdAt");
    }

    @Test
    @DisplayName("settle -> lança quando já COMPLETED")
    void settle_alreadyCompleted_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);

        tx.settle(CREATED_DATE_UTC);
        LocalDate dueDate = CREATED_DATE_UTC.minusDays(1);
        assertThatThrownBy(() -> tx.settle(dueDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transaction already settled");
    }

    @Test
    @DisplayName("settle -> lança quando status FAILED")
    void settle_failed_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.fail();

        assertThat(tx.status()).isEqualTo(TransactionStatus.FAILED);

        assertThatThrownBy(() -> tx.settle(CREATED_DATE_UTC))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot settle a failed transaction");
    }

    @Test
    @DisplayName("fail -> muda para FAILED e limpa settledAt")
    void fail_ok_clearsSettledAt() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.setSettledAt(CREATED_DATE_UTC.plusDays(1)); // allowed pela validação
        assertThat(tx.settledAt()).isNotNull();

        tx.fail();

        assertThat(tx.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(tx.settledAt()).isNull();
    }

    @Test
    @DisplayName("fail -> idempotente quando já FAILED")
    void fail_idempotent() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.fail();
        assertThat(tx.status()).isEqualTo(TransactionStatus.FAILED);

        tx.fail(); // não deve lançar
        assertThat(tx.status()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("fail -> lança quando COMPLETED")
    void fail_completed_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.settle(CREATED_DATE_UTC);

        assertThatThrownBy(tx::fail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot mark a completed transaction as failed");
    }

    @Test
    @DisplayName("reopen -> de COMPLETED para PENDING e limpa settledAt")
    void reopen_fromCompleted_ok() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.settle(CREATED_DATE_UTC.plusDays(1));

        assertThat(tx.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(tx.settledAt()).isNotNull();

        tx.reopen();

        assertThat(tx.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(tx.settledAt()).isNull();
    }

    @Test
    @DisplayName("reopen -> de FAILED para PENDING e limpa settledAt")
    void reopen_fromFailed_ok() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.fail();

        tx.reopen();

        assertThat(tx.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(tx.settledAt()).isNull();
    }

    @Test
    @DisplayName("reopen -> idempotente quando PENDING")
    void reopen_pending_idempotent() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        assertThat(tx.status()).isEqualTo(TransactionStatus.PENDING);

        tx.reopen(); // não deve lançar
        assertThat(tx.status()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("cancel -> muda para CANCELLED e limpa settledAt; idempotente")
    void cancel_ok_andIdempotent() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.cancel();

        assertThat(tx.status()).isEqualTo(TransactionStatus.CANCELLED);
        assertThat(tx.settledAt()).isNull();

        tx.cancel(); // idempotente
        assertThat(tx.status()).isEqualTo(TransactionStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel -> lança quando COMPLETED")
    void cancel_completed_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        tx.settle(CREATED_DATE_UTC);

        assertThatThrownBy(tx::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete a settled transaction");
    }

    @Test
    @DisplayName("setDueDate -> lança quando dueDate < createdAt(UTC)")
    void setDueDate_beforeCreated_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        LocalDate dueDate = CREATED_DATE_UTC.minusDays(1);
        assertThatThrownBy(() -> tx.setDueDate(dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DueDate cannot be before createdAt");
    }

    @Test
    @DisplayName("setSettledAt -> lança quando settledAt < createdAt(UTC)")
    void setSettledAt_beforeCreated_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);
        LocalDate dueDate = CREATED_DATE_UTC.minusDays(1);
        assertThatThrownBy(() -> tx.setSettledAt(dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SettledAt cannot be before createdAt");
    }

    @Test
    @DisplayName("setCategoryId -> lança quando null")
    void setCategoryId_null_throws() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);

        assertThatThrownBy(() -> tx.setCategoryId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("CategoryId cannot be null");
    }

    @Test
    @DisplayName("setCategoryId -> seta quando não null")
    void setCategoryId_ok() {
        Transaction tx = Transaction.create(OWNER, new BigDecimal("10.00"), TransactionType.EXPENSE, CREATED_AT, false);

        CategoryId catId = CategoryId.random();
        tx.setCategoryId(catId);

        assertThat(tx.categoryId()).isEqualTo(catId);
    }
}