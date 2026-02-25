package com.donyx.lifeops.financeiro.config;

import com.donyx.lifeops.financeiro.adapters.outbound.BCryptPasswordHasherAdapter;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.category.CategoryJpaRepository;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.category.CategoryRepositoryAdapter;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.SpringDataJpaRepository;
import com.donyx.lifeops.financeiro.adapters.outbound.persistence.user.UserRepositoryAdapter;
import com.donyx.lifeops.financeiro.application.ports.category.CategoryRepository;
import com.donyx.lifeops.financeiro.application.ports.user.PasswordHasher;
import com.donyx.lifeops.financeiro.application.ports.user.TokenProvider;
import com.donyx.lifeops.financeiro.application.ports.user.UserRepository;
import com.donyx.lifeops.financeiro.application.usecases.auth.LoginUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.CreateCategoryUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.DeleteCategoryUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.GetCategoryByIdUseCase;
import com.donyx.lifeops.financeiro.application.usecases.category.ListCategoriesUseCase;
import com.donyx.lifeops.financeiro.application.usecases.user.RegisterUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {

    // ---- Adapters ----

    @Bean
    public UserRepository userRepository(SpringDataJpaRepository jpaRepo) {
        return new UserRepositoryAdapter(jpaRepo);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasherAdapter(12);
    }

    @Bean
    public CategoryRepository categoryRepository(CategoryJpaRepository categoryJpaRepository) { return new CategoryRepositoryAdapter(categoryJpaRepository);}

    // ---- UseCases ----

    @Bean
    public RegisterUseCase registerUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher, TokenProvider tokenProvider
    ) {
        return new RegisterUseCase(userRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        return new LoginUseCase(userRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public CreateCategoryUseCase createCategoryUseCase(CategoryRepository categoryRepository) {
        return new CreateCategoryUseCase(categoryRepository);
    }

    @Bean
    public ListCategoriesUseCase listCategoriesUseCase(CategoryRepository categoryRepository) {
        return new ListCategoriesUseCase(categoryRepository);
    }

    @Bean
    public GetCategoryByIdUseCase getCategoryByIdUseCase(CategoryRepository categoryRepository) {
        return new GetCategoryByIdUseCase(categoryRepository);
    }

    @Bean
    public DeleteCategoryUseCase deleteCategoryUseCase(CategoryRepository categoryRepository) {
        return new DeleteCategoryUseCase(categoryRepository);
    }
}
