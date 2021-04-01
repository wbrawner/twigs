package com.wbrawner.budgetserver.category;

import com.wbrawner.budgetserver.ErrorResponse;
import com.wbrawner.budgetserver.permission.Permission;
import com.wbrawner.budgetserver.permission.UserPermission;
import com.wbrawner.budgetserver.permission.UserPermissionRepository;
import com.wbrawner.budgetserver.transaction.TransactionRepository;
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
    ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(name = "budgetIds", required = false) List<String> budgetIds,
            @RequestParam(name = "isExpense", required = false) Boolean isExpense,
            @RequestParam(name = "includeArchived", required = false) Boolean includeArchived,
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
        Boolean archived = includeArchived == null || includeArchived == false ? false : null;
        List<Category> categories = categoryRepository.findAllByBudgetIn(budgets, isExpense, archived, pageRequest);
        return ResponseEntity.ok(
                categories.stream()
                        .map(CategoryResponse::new)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<CategoryResponse> getCategory(@PathVariable String id) {
        var budgets = userPermissionsRepository.findAllByUser(getCurrentUser(), null)
                .stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());
        var category = categoryRepository.findByBudgetInAndId(budgets, id).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new CategoryResponse(category));
    }

    @GetMapping(path = "/{id}/balance", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<CategoryBalanceResponse> getCategoryBalance(@PathVariable String id) {
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
    ResponseEntity<CategoryResponse> updateCategory(@PathVariable String id, @RequestBody UpdateCategoryRequest request) {
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
        if (request.getArchived() != null) {
            category.setArchived(request.getArchived());
        }
        return ResponseEntity.ok(new CategoryResponse(categoryRepository.save(category)));
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    ResponseEntity<Void> deleteCategory(@PathVariable String id) {
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