package com.donyx.lifeops.financeiro.adapters.inbound.web;

import com.donyx.lifeops.financeiro.adapters.inbound.web.dto.category.CategoryResponse;
import com.donyx.lifeops.financeiro.adapters.inbound.web.dto.category.CreateCategoryRequest;
import com.donyx.lifeops.financeiro.application.usecases.category.CreateCategoryUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.DeleteCategoryUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.GetCategoryByIdUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.ListCategoriesUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.command.CreateCategoryCommand;
import com.donyx.lifeops.financeiro.domain.category.Category;
import com.donyx.lifeops.financeiro.domain.category.CategoryId;
import com.donyx.lifeops.financeiro.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final GetCategoryByIdUseCase getCategoryByIdUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase, ListCategoriesUseCase listCategoriesUseCase, DeleteCategoryUseCase deleteCategoryUseCase, GetCategoryByIdUseCase getCategoryByIdUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.getCategoryByIdUseCase = getCategoryByIdUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CreateCategoryRequest request,
                                                   Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        var command = new CreateCategoryCommand(userId, request.name(), request.description(), request.type());
        Category category = createCategoryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(
               toResponse(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        List<Category> categories = listCategoriesUseCase.execute(UserId.of(userId));
        List<CategoryResponse> categoriesResponse = categories.stream().map(
                this::toResponse).toList();

        return ResponseEntity.ok(categoriesResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Category category = getCategoryByIdUseCase.execute(CategoryId.of(id), UserId.of(userId));

        return ResponseEntity.ok(toResponse(category));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        deleteCategoryUseCase.execute(CategoryId.of(id), UserId.of(userId));
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.id().asUuid(),
                category.userId().asUuid(),
                category.name(), category.description(),
                category.type(), category.createdAt(),
                category.updatedAt());
    }
}
