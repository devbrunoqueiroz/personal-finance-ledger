package com.donyx.lifeops.financeiro.adapters.inbound.web.transaction;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.CreateTransactionRequest;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.SettleTransactionRequest;
import com.donyx.lifeops.financeiro.adapters.inbound.web.transaction.dto.UpdateTransactionRequest;
import com.donyx.lifeops.financeiro.application.ports.common.PageResult;
import com.donyx.lifeops.financeiro.application.ports.common.Pagination;
import com.donyx.lifeops.financeiro.application.ports.transaction.TransactionQuery;
import com.donyx.lifeops.financeiro.application.usecases.transaction.*;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.CreateTransactionCommand;
import com.donyx.lifeops.financeiro.application.usecases.transaction.command.UpdateTransactionCommand;
import com.donyx.lifeops.financeiro.config.SecurityFilter;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.transaction.Transaction;
import com.donyx.lifeops.financeiro.domain.transaction.TransactionId;
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
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockitoBean
    UpdateTransactionUsecase updateTransactionUseCase;

    @MockitoBean
    SettleTransactionUseCase settleTransactionUseCase;

    @MockitoBean
    CancelTransactionUseCase cancelTransactionUseCase;

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
                TransactionType.INCOME,
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

    @Test
    @DisplayName("PATCH /transactions/{id} -> 200 e chama usecase com command correto")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void update_ok_returns200_andCallsUseCase() throws Exception {
        UUID txId = TransactionId.random().asUuid();
        UUID categoryId = CategoryId.random().asUuid();

        var req = new UpdateTransactionRequest(
                "New desc",
                "New notes",
                new BigDecimal("123.45"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 10),
                categoryId,
                Boolean.TRUE
        );

        Transaction domainTx = Transaction.hydrate(
                TransactionId.of(txId),
                UserId.random(),
                new BigDecimal("123.45"),
                TransactionType.EXPENSE,
                Instant.parse("2026-02-20T10:00:00Z"),
                "New desc",
                "New notes",
                LocalDate.of(2026, 3, 10),
                null,
                TransactionStatus.PENDING,
                CategoryId.of(categoryId),
                true
        );

        when(updateTransactionUseCase.execute(any(UpdateTransactionCommand.class)))
                .thenReturn(domainTx);

        mockMvc.perform(patch("/transactions/{id}", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(txId.toString()))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.description").value("New desc"))
                .andExpect(jsonPath("$.notes").value("New notes"))
                .andExpect(jsonPath("$.dueDate").value("2026-03-10"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.recurring").value(true));

        ArgumentCaptor<UpdateTransactionCommand> captor = ArgumentCaptor.forClass(UpdateTransactionCommand.class);
        verify(updateTransactionUseCase).execute(captor.capture());

        UpdateTransactionCommand cmd = captor.getValue();
        assertThat(cmd.transactionId()).isEqualTo(TransactionId.of(txId));
        assertThat(cmd.ownerId()).isEqualTo(UserId.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")));
        assertThat(cmd.description()).isEqualTo("New desc");
        assertThat(cmd.notes()).isEqualTo("New notes");
        assertThat(cmd.amount()).isEqualByComparingTo("123.45");
        assertThat(cmd.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(cmd.dueDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(cmd.categoryId()).isEqualTo(CategoryId.of(categoryId));
        assertThat(cmd.recurring()).isTrue();
    }

    @Test
    @DisplayName("POST /transactions/{id}/settle -> 204 e chama usecase com settledAt do body")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void settle_withBody_usesBodyDate() throws Exception {
        UUID txId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        var req = new SettleTransactionRequest(LocalDate.of(2026, 2, 26));

        mockMvc.perform(post("/transactions/{id}/settle", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(settleTransactionUseCase).execute(
                TransactionId.of(txId),
                UserId.of("550e8400-e29b-41d4-a716-446655440000"),
                LocalDate.of(2026, 2, 26)
        );
    }

    @Test
    @DisplayName("POST /transactions/{id}/settle -> 204 e usa LocalDate.now(America/Sao_Paulo) quando body é null")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void settle_withoutBody_usesNowFromSaoPaulo() throws Exception {
        UUID txId = TransactionId.random().asUuid();

        // capturamos a data passada, porque "now" não dá pra fixar sem Clock
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        mockMvc.perform(post("/transactions/{id}/settle", txId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(settleTransactionUseCase).execute(
                eq(TransactionId.of(txId)),
                eq(UserId.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))),
                dateCaptor.capture()
        );

        LocalDate expected = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        assertThat(dateCaptor.getValue()).isEqualTo(expected);
    }

    @Test
    @DisplayName("DELETE /transactions/{id} -> 204 e chama cancel usecase com ids corretos")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void cancel_returns204_andCallsUseCase() throws Exception {
        UUID txId = TransactionId.random().asUuid();

        mockMvc.perform(delete("/transactions/{id}", txId)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cancelTransactionUseCase).execute(
                TransactionId.of(txId),
                UserId.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
        );
    }

    @Test
    @DisplayName("PATCH /transactions/{id} -> 400 quando request falha validação (ex: amount 0)")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void update_invalidRequest_returns400() throws Exception {
        UUID txId = TransactionId.random().asUuid();

        var req = new UpdateTransactionRequest(
                "New desc",
                null,
                new BigDecimal("0.00"),
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/transactions/{id}", txId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updateTransactionUseCase);
    }
}