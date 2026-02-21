package com.donyx.lifeops.financeiro.adapters.outbound.persistence.transaction;

import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.UUID;

public final class TransactionSpecifications {
    private TransactionSpecifications() {}

    public static Specification<JpaTransactionEntity> byUserAndQuery(UUID userId, TransactionQuery q) {
        Specification<JpaTransactionEntity> spec =
                (root, cq, cb) -> cb.equal(root.get("ownerId"), userId);

        if (q == null) return spec;

        if (q.text() != null && !q.text().isBlank()) {
            String like = "%" + q.text().trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("notes")), like)
            ));
        }

        if (q.minAmount() != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), q.minAmount()));
        }

        if (q.maxAmount() != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("amount"), q.maxAmount()));
        }

        if (q.dueFrom() != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), q.dueFrom()));
        }

        if (q.dueTo() != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), q.dueTo()));
        }

        if (q.settledFrom() != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("settledAt"), q.settledFrom()));
        }

        if (q.settledTo() != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("settledAt"), q.settledTo()));
        }

        if (q.type() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("type"), q.type()));
        }

        if (q.status() != null) { // TransactionStatus
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), q.status()));
        }

        if (q.categoryId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("categoryId"), q.categoryId().asUuid()));
        }

        return spec;
    }
}