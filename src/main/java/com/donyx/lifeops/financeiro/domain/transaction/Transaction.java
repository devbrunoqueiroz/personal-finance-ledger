package com.donyx.lifeops.financeiro.domain.transaction;

import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Transaction {

    private final TransactionId id;
    private final UserId ownerId;
    private final BigDecimal amount;           // value object (BigDecimal + moeda, opcional)
    private final TransactionType type;   // INCOME / EXPENSE
    private final Instant createdAt;

    private String description;
    private String notes;
    private LocalDate dueDate;
    private LocalDate settledAt;
    private TransactionStatus status;
    private CategoryId categoryId;

    private final boolean recurring; // MVP: tag apenas

    public Transaction(TransactionId id, UserId ownerId, BigDecimal amount, TransactionType type, Instant createdAt, boolean recurring) {
        this.id = Objects.requireNonNull(id, "TransactionId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.recurring = recurring;
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    public static Transaction hydrate(
            TransactionId id,
            UserId ownerId,
            BigDecimal amount,
            TransactionType type,
            Instant createdAt,
            String description,
            String notes,
            LocalDate dueDate,
            LocalDate settledAt,
            TransactionStatus status,
            CategoryId categoryId,
            boolean recurring
    ) {
        Transaction tx = new Transaction(id, ownerId, amount, type, createdAt, recurring);

        if (description != null && description.trim().isEmpty()) throw new IllegalArgumentException("Description cannot be blank");
        if (notes != null && notes.trim().isEmpty()) throw new IllegalArgumentException("Notes cannot be blank");
        if (status == null) throw new IllegalArgumentException("TransactionStatus cannot be null");

        tx.description = description;
        tx.notes = notes;
        tx.status = status;
        tx.dueDate = dueDate;
        tx.settledAt = settledAt;
        tx.categoryId = categoryId;
        return tx;
    }

    public static Transaction create(
            UserId ownerId,
            BigDecimal amount,
            TransactionType type,
            Instant now,
            boolean recurring
    ) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(now, "now");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Transaction tx = new Transaction(
                TransactionId.random(),
                ownerId,
                amount,
                type,
                now,
                recurring
        );

        tx.status = TransactionStatus.PENDING; // ou OPEN se for seu enum

        return tx;
    }

    public void settle(LocalDate date) {
        Objects.requireNonNull(date, "settledAt");
        this.settledAt = date;
        this.status = TransactionStatus.COMPLETED;
    }

    public TransactionId id() { return id; }
    public UserId ownerId() { return ownerId; }
    public String description() { return description; }
    public String notes() { return notes; }
    public BigDecimal amount() { return amount; }
    public TransactionType type() { return type; }
    public TransactionStatus status() { return status; }
    public LocalDate dueDate() { return dueDate; }
    public LocalDate settledAt() { return settledAt; }
    public Instant createdAt() { return createdAt; }
    public CategoryId categoryId() { return categoryId; }
    public boolean recurring() { return recurring; }
    // Setters for mutable fields
    public void setDescription(String description) {
        if (description != null && description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        this.description = description;
    }

    public void setNotes(String notes) {
        if (notes != null && notes.trim().isEmpty()) {
            throw new IllegalArgumentException("Notes cannot be blank");
        }
        this.notes = notes;
    }

    public void setStatus(TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("TransactionStatus cannot be null");
        }
        this.status = status;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate())) {
            throw new IllegalArgumentException("DueDate cannot be before createdAt");
        }
        this.dueDate = dueDate;
    }

    public void setSettledAt(LocalDate settledAt) {
        if (settledAt != null && settledAt.isBefore(createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate())) {
            throw new IllegalArgumentException("SettledAt cannot be before createdAt");
        }
        this.settledAt = settledAt;
    }

    public void setCategoryId(CategoryId categoryId) {
        this.categoryId = Objects.requireNonNull(categoryId, "CategoryId cannot be null");
    }
}
