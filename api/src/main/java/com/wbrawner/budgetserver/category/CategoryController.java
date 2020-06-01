package com.wbrawner.budgetserver.category;

import com.wbrawner.budgetserver.ErrorResponse;
import com.wbrawner.budgetserver.permission.Permission;
import com.wbrawner.budgetserver.permission.UserPermission;
import com.wbrawner.budgetserver.permission.UserPermissionRepository;
import com.wbrawner.budgetserver.transaction.TransactionRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.wbrawner.budgetserver.Utils.getCurrentUser;
import static com.wbrawner.budgetserver.Utils.getFirstOfMonth;

@RestController
@RequestMapping(path = "/categories")
@Api(value = "Categories", tags = {"Categories"}, authorizations = {@Authorization("basic")})
@Transactional
class CategoryController {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserPermissionRepository userPermissionsRepository;

    CategoryController(CategoryRepository categoryRepository,
                       TransactionRepository transactionRepository,
                       UserPermissionRepository userPermissionsRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userPermissionsRepository = userPermissionsRepository;
    }

    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getCategories", nickname = "getCategories", tags = {"Categories"})
    ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(name = "budgetIds", required = false) List<Long> budgetIds,
            @RequestParam(name = "isExpense", required = false) Boolean isExpense,
            @RequestParam(name = "count", required = false) Integer count,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "false", required = false) String sortBy,
            @RequestParam(name = "sortOrder", required = false) Sort.Direction sortOrder
    ) {
        List<UserPermission> userPermissions;
        if (budgetIds != null && !budgetIds.isEmpty()) {
            userPermissions = userPermissionsRepository.findAllByUserAndBudget_IdIn(
                    getCurrentUser(),
                    budgetIds,
                    PageRequest.of(page != null ? page : 0, count != null ? count : 1000)
            );
        } else {
            userPermissions = userPermissionsRepository.findAllByUser(getCurrentUser(), null);
        }
        var budgets = userPermissions.stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());

        var pageRequest = PageRequest.of(
                Math.min(0, page != null ? page - 1 : 0),
                count != null ? count : 1000,
                sortOrder != null ? sortOrder : Sort.Direction.ASC,
                sortBy != null ? sortBy : "title"
        );
        List<Category> categories;
        if (isExpense == null) {
            categories = categoryRepository.findAllByBudgetIn(budgets, pageRequest);
        } else {
            categories = categoryRepository.findAllByBudgetInAndExpense(budgets, isExpense, pageRequest);
        }

        return ResponseEntity.ok(
                categories.stream()
                        .map(CategoryResponse::new)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getCategory", nickname = "getCategory", tags = {"Categories"})
    ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        var budgets = userPermissionsRepository.findAllByUser(getCurrentUser(), null)
                .stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());
        var category = categoryRepository.findByBudgetInAndId(budgets, id).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CategoryResponse(category));
    }

    @GetMapping(path = "/{id}/balance", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getCategoryBalance", nickname = "getCategoryBalance", tags = {"Categories"})
    ResponseEntity<CategoryBalanceResponse> getCategoryBalance(@PathVariable Long id) {
        var budgets = userPermissionsRepository.findAllByUser(getCurrentUser(), null)
                .stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());
        var category = categoryRepository.findByBudgetInAndId(budgets, id).orElse(null);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        var sum = transactionRepository.sumBalanceByCategoryId(category.getId(), getFirstOfMonth());
        return ResponseEntity.ok(new CategoryBalanceResponse(category.getId(), sum));
    }

    @PostMapping(path = "", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "newCategory", nickname = "newCategory", tags = {"Categories"})
    ResponseEntity<Object> newCategory(@RequestBody NewCategoryRequest request) {
        var userResponse = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), request.getBudgetId())
                .orElse(null);
        if (userResponse == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid budget ID"));
        }
        if (userResponse.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var budget = userResponse.getBudget();
        return ResponseEntity.ok(new CategoryResponse(categoryRepository.save(new Category(
                request.getTitle(),
                request.getDescription(),
                request.getAmount(),
                budget,
                request.getExpense()
        ))));
    }

    @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "updateCategory", nickname = "updateCategory", tags = {"Categories"})
    ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest request) {
        var category = categoryRepository.findById(id).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), category.getBudget().getId()).orElse(null);
        if (userPermission == null) return ResponseEntity.notFound().build();
        if (userPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (request.getTitle() != null) {
            category.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            category.setAmount(request.getAmount());
        }
        if (request.getExpense() != null) {
            category.setExpense(request.getExpense());
        }
        return ResponseEntity.ok(new CategoryResponse(categoryRepository.save(category)));
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ApiOperation(value = "deleteCategory", nickname = "deleteCategory", tags = {"Categories"})
    ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        var category = categoryRepository.findById(id).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), category.getBudget().getId()).orElse(null);
        if (userPermission == null) return ResponseEntity.notFound().build();
        if (userPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        transactionRepository.findAllByBudgetAndCategory(userPermission.getBudget(), category)
                .forEach(transaction -> {
                    transaction.setCategory(null);
                    transactionRepository.save(transaction);
                });
        categoryRepository.delete(category);
        return ResponseEntity.ok().build();
    }
}