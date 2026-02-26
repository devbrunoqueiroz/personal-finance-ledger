package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction;

import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.*;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.usecases.transaction.*;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.UpdateTransactionCommand;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination.Sort.Direction;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransaction;
    private final SearchTransactionsUseCase searchTransactionsUseCase;
    private final UpdateTransactionUsecase updateTransactionUseCase;
    private final SettleTransactionUseCase settleTransactionUseCase;
    private final CancelTransactionUseCase cancelTransactionUseCase;

    public TransactionController(CreateTransactionUseCase createTransaction, SearchTransactionsUseCase searchTransactionsUseCase, UpdateTransactionUsecase updateTransactionUseCase, SettleTransactionUseCase settleTransactionUseCase, CancelTransactionUseCase cancelTransactionUseCase) {
        this.createTransaction = createTransaction;
        this.searchTransactionsUseCase = searchTransactionsUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.settleTransactionUseCase = settleTransactionUseCase;
        this.cancelTransactionUseCase = cancelTransactionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest req,
            Authentication auth) {
        UserId userId = currentUser(auth);

        var tx = createTransaction.execute(TransactionInboundMapper.toCommand(req, userId));

        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromDomain(tx));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<TransactionResponse>> search(
            Authentication auth,

            @RequestParam(required = false) String text,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) LocalDate dueFrom,
            @RequestParam(required = false) LocalDate dueTo,
            @RequestParam(required = false) LocalDate settledFrom,
            @RequestParam(required = false) LocalDate settledTo,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) UUID categoryId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort
    ) {
        UserId userId = currentUser(auth);

        TransactionQuery query = new TransactionQuery(
                text,
                minAmount,
                maxAmount,
                dueFrom,
                dueTo,
                settledFrom,
                settledTo,
                type,
                status,
                categoryId == null ? null : CategoryId.of(categoryId)
        );


        Pagination pageRequest =
                new Pagination(page, size, parseSorts(sort));

        PageResult<Transaction> result =
                searchTransactionsUseCase.execute(userId, query, pageRequest);

        return ResponseEntity.ok(
                new PaginationResponse<>(
                        result.content().stream()
                                .map(TransactionResponse::fromDomain)
                                .toList(),
                        result.page(),
                        result.size(),
                        result.totalElements(),
                        result.totalPages()
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest req,
            Authentication auth
    ) {
        UserId userId = currentUser(auth);

        UpdateTransactionCommand cmd = TransactionInboundMapper.toUpdateCommand(id, req, userId);

        var transaction = updateTransactionUseCase.execute(cmd);
        TransactionResponse txResponse = TransactionResponse.fromDomain(transaction);
        return ResponseEntity.ok(txResponse);
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<Void> settle(
            @PathVariable UUID id,
            @RequestBody(required = false) SettleTransactionRequest req,
            Authentication auth
    ) {
        UserId userId = currentUser(auth);

        LocalDate settledAt = (req != null && req.settledAt() != null)
                ? req.settledAt()
                : LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        settleTransactionUseCase.execute(TransactionId.of(id), userId, settledAt);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id,
            Authentication auth
    ) {
        UserId userId = currentUser(auth);

        cancelTransactionUseCase.execute(TransactionId.of(id), userId);

        return ResponseEntity.noContent().build();
    }

    private static UserId currentUser(Authentication auth) {
        return UserId.of(UUID.fromString(auth.getName()));
    }

    private static List<Pagination.Sort> parseSorts(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) return List.of();

        List<Pagination.Sort> out = new ArrayList<>();
        for (String s : sortParams) {
            if (s == null || s.isBlank()) continue;

            String[] parts = s.split(",", 2);
            String field = parts[0].trim();
            Direction dir = Direction.ASC;

            if (parts.length == 2) {
                String raw = parts[1].trim().toUpperCase(Locale.ROOT);
                if ("DESC".equals(raw)) dir = Direction.DESC;
            }

            out.add(new Pagination.Sort(field, dir));
        }
        return out;
    }
}
