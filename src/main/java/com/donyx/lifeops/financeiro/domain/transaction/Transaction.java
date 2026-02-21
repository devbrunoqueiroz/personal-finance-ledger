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
    private String description;
    private String notes;
    private final BigDecimal amount;
    private final TransactionType type;
    private TransactionStatus status;
    private LocalDate dueDate;
    private     LocalDate settledAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private CategoryId categoryId;

    public Transaction(TransactionId id, UserId ownerId, BigDecimal amount, TransactionType type, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "TransactionId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    public static Transaction hydrate(
            TransactionId id,
            UserId ownerId,
            String description,
            String notes,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            LocalDate dueDate,
            LocalDate settledAt,
            CategoryId categoryId,
            Instant createdAt,
            Instant updatedAt
    ) {
        Transaction tx = new Transaction(id, ownerId, amount, type, createdAt);

        if (description != null && description.trim().isEmpty()) throw new IllegalArgumentException("Description cannot be blank");
        if (notes != null && notes.trim().isEmpty()) throw new IllegalArgumentException("Notes cannot be blank");
        if (status == null) throw new IllegalArgumentException("TransactionStatus cannot be null");

        tx.description = description;
        tx.notes = notes;
        tx.status = status;
        tx.dueDate = dueDate;
        tx.settledAt = settledAt;
        tx.categoryId = categoryId;
        tx.updatedAt = (updatedAt != null ? updatedAt : createdAt);

        return tx;
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
    public Instant updatedAt() { return updatedAt; }
    public CategoryId categoryId() { return categoryId; }

    // Setters for mutable fields
    public void setDescription(String description) {
        if (description != null && description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void setNotes(String notes) {
        if (notes != null && notes.trim().isEmpty()) {
            throw new IllegalArgumentException("Notes cannot be blank");
        }
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public void setStatus(TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("TransactionStatus cannot be null");
        }
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate())) {
            throw new IllegalArgumentException("DueDate cannot be before createdAt");
        }
        this.dueDate = dueDate;
        this.updatedAt = Instant.now();
    }

    public void setSettledAt(LocalDate settledAt) {
        if (settledAt != null && settledAt.isBefore(createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate())) {
            throw new IllegalArgumentException("SettledAt cannot be before createdAt");
        }
        this.settledAt = settledAt;
        this.updatedAt = Instant.now();
    }

    public void setCategoryId(CategoryId categoryId) {
        this.categoryId = Objects.requireNonNull(categoryId, "CategoryId cannot be null");
        this.updatedAt = Instant.now();
    }
}
