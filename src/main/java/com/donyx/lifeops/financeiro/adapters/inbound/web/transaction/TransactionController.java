package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction;

import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.CreateTransactionRequest;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.TransactionResponse;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.usecases.transaction.CreateTransactionUseCase;
import com.donyx.lifeops.financeiro.application.usecases.transaction.SearchTransactionsUseCase;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.PaginationResponse;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination.Sort.Direction;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransaction;
    private final SearchTransactionsUseCase searchTransactionsUseCase;

    public TransactionController(CreateTransactionUseCase createTransaction, SearchTransactionsUseCase searchTransactionsUseCase) {
        this.createTransaction = createTransaction;
        this.searchTransactionsUseCase = searchTransactionsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest req, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

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
        UserId ownerId = UserId.of(UUID.fromString(auth.getName()));

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
                searchTransactionsUseCase.execute(ownerId, query, pageRequest);

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

    private static List<Pagination.Sort> parseSorts(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) return List.of();

        // Aceita: ?sort=dueDate,desc&sort=amount,asc
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
