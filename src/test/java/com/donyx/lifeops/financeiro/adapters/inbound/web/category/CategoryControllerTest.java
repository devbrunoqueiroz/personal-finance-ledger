package com.donyx.lifeops.financeiro.adapters.inbound.web.category;

import com.donyx.lifeops.financeiro.adapters.inbound.web.category.dto.CreateCategoryRequest;
import com.donyx.lifeops.financeiro.application.usecases.category.CreateCategoryUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.DeleteCategoryUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.GetCategoryByIdUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.ListCategoriesUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.command.CreateCategoryCommand;
import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.category.CategoryType;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    CreateCategoryUseCase createCategoryUseCase;
    @MockitoBean
    ListCategoriesUseCase listCategoriesUseCase;
    @MockitoBean
    DeleteCategoryUseCase deleteCategoryUseCase;
    @MockitoBean
    GetCategoryByIdUseCase getCategoryByIdUseCase;

    @Test
    @DisplayName("POST /categories -> 201 e retorna CategoryResponse")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void create_returns201_andBody_andCallsUseCaseWithCommand() throws Exception {
        UUID userUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID categoryUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        var req = new CreateCategoryRequest(
                "Food",
                "Everything related to food",
                CategoryType.EXPENSE
        );

        // Mock do domínio Category
        Category category = mock(Category.class);
        when(category.id()).thenReturn(CategoryId.of(categoryUuid));
        when(category.userId()).thenReturn(UserId.of(userUuid));
        when(category.name()).thenReturn("Food");
        when(category.description()).thenReturn("Everything related to food");
        when(category.type()).thenReturn(CategoryType.EXPENSE);
        when(category.createdAt()).thenReturn(Instant.parse("2026-02-01T10:00:00Z"));
        when(category.updatedAt()).thenReturn(Instant.parse("2026-02-02T10:00:00Z"));

        when(createCategoryUseCase.execute(any(CreateCategoryCommand.class)))
                .thenReturn(category);

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(categoryUuid.toString()))
                .andExpect(jsonPath("$.userId").value(userUuid.toString()))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.description").value("Everything related to food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.createdAt").value("2026-02-01T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-02-02T10:00:00Z"));

        ArgumentCaptor<CreateCategoryCommand> captor = ArgumentCaptor.forClass(CreateCategoryCommand.class);
        verify(createCategoryUseCase).execute(captor.capture());

        CreateCategoryCommand cmd = captor.getValue();
        assertThat(cmd.userId()).isEqualTo(userUuid);
        assertThat(cmd.name()).isEqualTo("Food");
        assertThat(cmd.description()).isEqualTo("Everything related to food");
        assertThat(cmd.type()).isEqualTo(com.donyx.lifeops.financeiro.domain.category.CategoryType.EXPENSE);
    }

    @Test
    @DisplayName("GET /categories -> 200 e retorna lista mapeada")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void list_returns200_andMappedList() throws Exception {
        UUID userUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        UUID cat1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID cat2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Category c1 = mock(Category.class);
        when(c1.id()).thenReturn(CategoryId.of(cat1));
        when(c1.userId()).thenReturn(UserId.of(userUuid));
        when(c1.name()).thenReturn("Food");
        when(c1.description()).thenReturn("Food stuff");
        when(c1.type()).thenReturn(com.donyx.lifeops.financeiro.domain.category.CategoryType.EXPENSE);
        when(c1.createdAt()).thenReturn(Instant.parse("2026-02-01T10:00:00Z"));
        when(c1.updatedAt()).thenReturn(Instant.parse("2026-02-02T10:00:00Z"));

        Category c2 = mock(Category.class);
        when(c2.id()).thenReturn(CategoryId.of(cat2));
        when(c2.userId()).thenReturn(UserId.of(userUuid));
        when(c2.name()).thenReturn("Salary");
        when(c2.description()).thenReturn("Salary income");
        when(c2.type()).thenReturn(com.donyx.lifeops.financeiro.domain.category.CategoryType.INCOME);
        when(c2.createdAt()).thenReturn(Instant.parse("2026-02-03T10:00:00Z"));
        when(c2.updatedAt()).thenReturn(Instant.parse("2026-02-04T10:00:00Z"));

        when(listCategoriesUseCase.execute(UserId.of(userUuid)))
                .thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(cat1.toString()))
                .andExpect(jsonPath("$[0].userId").value(userUuid.toString()))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].id").value(cat2.toString()))
                .andExpect(jsonPath("$[1].type").value("INCOME"));

        verify(listCategoriesUseCase).execute(UserId.of(userUuid));
    }

    @Test
    @DisplayName("GET /categories/{id} -> 200 e retorna item mapeado")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getById_returns200_andMappedItem() throws Exception {
        UUID userUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID categoryUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Category c = mock(Category.class);
        when(c.id()).thenReturn(CategoryId.of(categoryUuid));
        when(c.userId()).thenReturn(UserId.of(userUuid));
        when(c.name()).thenReturn("Food");
        when(c.description()).thenReturn("Food stuff");
        when(c.type()).thenReturn(com.donyx.lifeops.financeiro.domain.category.CategoryType.EXPENSE);
        when(c.createdAt()).thenReturn(Instant.parse("2026-02-01T10:00:00Z"));
        when(c.updatedAt()).thenReturn(Instant.parse("2026-02-02T10:00:00Z"));

        when(getCategoryByIdUseCase.execute(CategoryId.of(categoryUuid), UserId.of(userUuid)))
                .thenReturn(c);

        mockMvc.perform(get("/categories/{id}", categoryUuid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(categoryUuid.toString()))
                .andExpect(jsonPath("$.userId").value(userUuid.toString()))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(getCategoryByIdUseCase).execute(CategoryId.of(categoryUuid), UserId.of(userUuid));
    }

    @Test
    @DisplayName("DELETE /categories/{id} -> 204 e chama usecase com ids corretos")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void delete_returns204_andCallsUseCase() throws Exception {
        UUID userUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID categoryUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        mockMvc.perform(delete("/categories/{id}", categoryUuid)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteCategoryUseCase).execute(CategoryId.of(categoryUuid), UserId.of(userUuid));
    }
}