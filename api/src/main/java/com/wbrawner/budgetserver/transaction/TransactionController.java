package com.wbrawner.budgetserver.transaction;

import com.wbrawner.budgetserver.ErrorResponse;
import com.wbrawner.budgetserver.category.Category;
import com.wbrawner.budgetserver.category.CategoryRepository;
import com.wbrawner.budgetserver.permission.Permission;
import com.wbrawner.budgetserver.permission.UserPermission;
import com.wbrawner.budgetserver.permission.UserPermissionRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.wbrawner.budgetserver.Utils.*;

@RestController
@RequestMapping(path = "/transactions")
@Api(value = "Transactions", tags = {"Transactions"}, authorizations = {@Authorization("basic")})
@Transactional
public class TransactionController {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserPermissionRepository userPermissionsRepository;

    private final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(CategoryRepository categoryRepository,
                                 TransactionRepository transactionRepository,
                                 UserPermissionRepository userPermissionsRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userPermissionsRepository = userPermissionsRepository;
    }

    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getTransactions", nickname = "getTransactions", tags = {"Transactions"})
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(value = "categoryIds", required = false) List<String> categoryIds,
            @RequestParam(value = "budgetIds", required = false) List<String> budgetIds,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "count", required = false) Integer count,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) Sort.Direction sortOrder
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

        List<Category> categories = null;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categories = categoryRepository.findAllByBudgetInAndIdIn(budgets, categoryIds, null);
        }
        var pageRequest = PageRequest.of(
                Math.min(0, page != null ? page - 1 : 0),
                count != null ? count : 1000,
                sortOrder != null ? sortOrder : Sort.Direction.DESC,
                sortBy != null ? sortBy : "date"
        );
        Instant fromInstant;
        try {
            fromInstant = Instant.parse(from);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException))
                logger.error("Failed to parse '" + from + "' to Instant for 'from' parameter", e);
            fromInstant = getFirstOfMonth().toInstant();
        }
        Instant toInstant;
        try {
            toInstant = Instant.parse(to);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException))
                logger.error("Failed to parse '" + to + "' to Instant for 'to' parameter", e);
            toInstant = getEndOfMonth().toInstant();
        }
        var query = categories == null ? transactionRepository.findAllByBudgetInAndDateGreaterThanAndDateLessThan(
                budgets,
                fromInstant,
                toInstant,
                pageRequest
        ) : transactionRepository.findAllByBudgetInAndCategoryInAndDateGreaterThanAndDateLessThan(
                budgets,
                categories,
                fromInstant,
                toInstant,
                pageRequest
        );
        var transactions = query
                .stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getTransaction", nickname = "getTransaction", tags = {"Transactions"})
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        var budgets = userPermissionsRepository.findAllByUser(getCurrentUser(), null)
                .stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());
        var transaction = transactionRepository.findByIdAndBudgetIn(id, budgets).orElse(null);
        if (transaction == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }

    @PostMapping(path = "", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "newTransaction", nickname = "newTransaction", tags = {"Transactions"})
    public ResponseEntity<Object> newTransaction(@RequestBody NewTransactionRequest request) {
        var userResponse = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), request.getBudgetId())
                .orElse(null);
        if (userResponse == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid budget ID"));
        }
        if (userResponse.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var budget = userResponse.getBudget();
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByBudgetAndId(budget, request.getCategoryId()).orElse(null);
        }
        return ResponseEntity.ok(new TransactionResponse(transactionRepository.save(new Transaction(
                request.getTitle(),
                request.getDescription(),
                Instant.parse(request.getDate()),
                request.getAmount(),
                category,
                request.getExpense(),
                getCurrentUser(),
                budget
        ))));
    }

    @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "updateTransaction", nickname = "updateTransaction", tags = {"Transactions"})
    public ResponseEntity<Object> updateTransaction(@PathVariable String id, @RequestBody UpdateTransactionRequest request) {
        var transaction = transactionRepository.findById(id).orElse(null);
        if (transaction == null) return ResponseEntity.notFound().build();
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), transaction.getBudget().getId()).orElse(null);
        if (userPermission == null) return ResponseEntity.notFound().build();
        if (userPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (request.getTitle() != null) {
            transaction.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getDate() != null) {
            transaction.setDate(Instant.parse(request.getDate()));
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getExpense() != null) {
            transaction.setExpense(request.getExpense());
        }
        if (request.getBudgetId() != null) {
            var newUserPermission = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), request.getBudgetId()).orElse(null);
            if (newUserPermission == null || newUserPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("Invalid budget"));
            }
            transaction.setBudget(newUserPermission.getBudget());
        }
        if (request.getCategoryId() != null) {
            var category = categoryRepository.findByBudgetAndId(transaction.getBudget(), request.getCategoryId()).orElse(null);
            if (category == null) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("Invalid category"));
            }
            transaction.setCategory(category);
        }
        return ResponseEntity.ok(new TransactionResponse(transactionRepository.save(transaction)));
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ApiOperation(value = "deleteTransaction", nickname = "deleteTransaction", tags = {"Transactions"})
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        var transaction = transactionRepository.findById(id).orElse(null);
        if (transaction == null) return ResponseEntity.notFound().build();
        // Check that the transaction belongs to an budget that the user has access to before deleting it
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), transaction.getBudget().getId()).orElse(null);
        if (userPermission == null) return ResponseEntity.notFound().build();
        if (userPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        transactionRepository.delete(transaction);
        return ResponseEntity.ok().build();
    }
}