package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.CreateTransactionRequest;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.application.usecases.transaction.CreateTransactionUseCase;
import com.donyx.lifeops.financeiro.application.usecases.transaction.SearchTransactionsUseCase;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.CreateTransactionCommand;
import com.donyx.lifeops.financeiro.config.SecurityFilter;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionStatus;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionType;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.security.test.context.support.WithMockUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransactionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityFilter.class
        )
)
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    SearchTransactionsUseCase searchTransactionsUseCase;

    @Test
    @DisplayName("POST /transactions - should create a new transaction and return 201")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void createTransaction_returnsCreated() throws Exception {
        CategoryId categoryId = CategoryId.random();
        CreateTransactionRequest request =
                new CreateTransactionRequest(
                    new BigDecimal("100.00"),
                    TransactionType.EXPENSE,
                    "Test Transaction",
                    "Test Notes",
                        LocalDate.now().plusDays(7),
                        LocalDate.now().plusDays(6),
                        categoryId.asUuid(),
                        false
        );

        var tx = buildTransaction(UserId.of("550e8400-e29b-41d4-a716-446655440000"),
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                "Test Transaction",
                "Test Notes");


        when(createTransactionUseCase.execute(any(CreateTransactionCommand.class)))
                .thenReturn(tx);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    @DisplayName("GET /transactions - should search with filters/pagination and return 200 with PaginationResponse")
    void searchTransactions_returnsOk() throws Exception {
        UUID ownerUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UserId ownerId = UserId.of(ownerUuid);

        // Monta duas transactions do domínio (ADAPTA pro teu construtor/factory real)
        Transaction tx1 = buildTransaction(ownerId, new BigDecimal("100.00"), TransactionType.EXPENSE, "Coffee", "Notes 1");
        Transaction tx2 = buildTransaction(ownerId, new BigDecimal("250.00"), TransactionType.EXPENSE, "Market", "Notes 2");

        PageResult<Transaction> pageResult = new PageResult<>(
                List.of(tx1, tx2),
                0,
                20,
                2L,
                1
        );

        when(searchTransactionsUseCase.execute(any(UserId.class), any(TransactionQuery.class), any(Pagination.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/transactions")
                        .param("text", "mar")
                        .param("minAmount", "10.00")
                        .param("maxAmount", "500.00")
                        .param("dueFrom", "2026-03-01")
                        .param("dueTo", "2026-03-31")
                        .param("settledFrom", "2026-03-01")
                        .param("settledTo", "2026-03-31")
                        .param("type", "EXPENSE")
                        .param("status", "PENDING")
                        .param("categoryId", UUID.randomUUID().toString())
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "dueDate,desc")
                        .param("sort", "amount,asc")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2));

        // Captura e valida os argumentos gerados pelo controller (pra garantir que parseSorts e query montaram certo)
        ArgumentCaptor<UserId> ownerCaptor = ArgumentCaptor.forClass(UserId.class);
        ArgumentCaptor<TransactionQuery> queryCaptor = ArgumentCaptor.forClass(TransactionQuery.class);
        ArgumentCaptor<Pagination> paginationCaptor = ArgumentCaptor.forClass(Pagination.class);

        verify(searchTransactionsUseCase).execute(ownerCaptor.capture(), queryCaptor.capture(), paginationCaptor.capture());

        assertThat(ownerCaptor.getValue()).isEqualTo(ownerId);

        TransactionQuery q = queryCaptor.getValue();
        assertThat(q.text()).isEqualTo("mar");
        assertThat(q.minAmount()).isEqualByComparingTo("10.00");
        assertThat(q.maxAmount()).isEqualByComparingTo("500.00");
        assertThat(q.dueFrom()).isEqualTo(LocalDate.parse("2026-03-01"));
        assertThat(q.dueTo()).isEqualTo(LocalDate.parse("2026-03-31"));
        assertThat(q.settledFrom()).isEqualTo(LocalDate.parse("2026-03-01"));
        assertThat(q.settledTo()).isEqualTo(LocalDate.parse("2026-03-31"));
        assertThat(q.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(q.status()).isEqualTo(TransactionStatus.PENDING);
        // categoryId foi enviado; aqui só checa se não veio null
        assertThat(q.categoryId()).isNotNull();

        Pagination p = paginationCaptor.getValue();
        assertThat(p.page()).isZero();
        assertThat(p.size()).isEqualTo(20);
        assertThat(p.sorts()).hasSize(2);
        assertThat(p.sorts().get(0).field()).isEqualTo("dueDate");
        assertThat(p.sorts().get(0).direction()).isEqualTo(Pagination.Sort.Direction.DESC);
        assertThat(p.sorts().get(1).field()).isEqualTo("amount");
        assertThat(p.sorts().get(1).direction()).isEqualTo(Pagination.Sort.Direction.ASC);
    }


    private static Transaction buildTransaction(UserId ownerId, BigDecimal amount, TransactionType type, String description, String notes) {
        // Se você tiver Transaction.create(...), usa ela.
        // Exemplo "genérico" (troca pelos campos reais):
        Transaction tx = Transaction.create(
                ownerId,
                amount,
                type,
                Instant.now(),
                false
        );
        tx.setDescription(description);
        tx.setNotes(notes);
        return tx;
    }
}