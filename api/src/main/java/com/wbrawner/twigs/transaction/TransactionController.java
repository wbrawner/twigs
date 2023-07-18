package com.wbrawner.twigs.transaction;

import com.wbrawner.twigs.ErrorResponse;
import com.wbrawner.twigs.category.Category;
import com.wbrawner.twigs.category.CategoryRepository;
import com.wbrawner.twigs.permission.Permission;
import com.wbrawner.twigs.permission.UserPermission;
import com.wbrawner.twigs.permission.UserPermissionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.wbrawner.twigs.Utils.*;

@RestController
@RequestMapping(path = "/api/transactions")
@Transactional
public class TransactionController {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserPermissionRepository userPermissionsRepository;

    private final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            UserPermissionRepository userPermissionsRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userPermissionsRepository = userPermissionsRepository;
    }

    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE})
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
            if (!(e instanceof NullPointerException)) {
                logger.error("Failed to parse '" + from + "' to Instant for 'from' parameter", e);
            }
            fromInstant = getFirstOfMonth().toInstant();
        }
        Instant toInstant;
        try {
            toInstant = Instant.parse(to);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                logger.error("Failed to parse '" + to + "' to Instant for 'to' parameter", e);
            }
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
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        var budgets = userPermissionsRepository.findAllByUser(getCurrentUser(), null)
                .stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());
        var transaction = transactionRepository.findByIdAndBudgetIn(id, budgets).orElse(null);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }

    @PostMapping(
            path = "",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
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

    @PutMapping(
            path = "/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<Object> updateTransaction(
            @PathVariable String id, @RequestBody UpdateTransactionRequest request
    ) {
        var transaction = transactionRepository.findById(id).orElse(null);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(
                getCurrentUser(),
                transaction.getBudget().getId()
        ).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }
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
            var newUserPermission = userPermissionsRepository.findByUserAndBudget_Id(
                    getCurrentUser(),
                    request.getBudgetId()
            ).orElse(null);
            if (newUserPermission == null || newUserPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("Invalid budget"));
            }
            transaction.setBudget(newUserPermission.getBudget());
        }
        if (request.getCategoryId() != null) {
            var category = categoryRepository.findByBudgetAndId(transaction.getBudget(), request.getCategoryId())
                    .orElse(null);
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
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        var transaction = transactionRepository.findById(id).orElse(null);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        // Check that the transaction belongs to an budget that the user has access to before deleting it
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(
                getCurrentUser(),
                transaction.getBudget().getId()
        ).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }
        if (userPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        transactionRepository.delete(transaction);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/sum", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BalanceResponse> getSum(
            @RequestParam(value = "budgetId", required = false) String budgetId,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Instant fromInstant;
        try {
            fromInstant = Instant.parse(from);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                logger.error("Failed to parse '" + from + "' to Instant for 'from' parameter", e);
            }
            fromInstant = Instant.ofEpochSecond(0);
        }
        Instant toInstant;
        try {
            toInstant = Instant.parse(to);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                logger.error("Failed to parse '" + to + "' to Instant for 'to' parameter", e);
            }
            toInstant = Instant.now();
        }

        if (((budgetId == null || budgetId.isBlank())
                && (categoryId == null || categoryId.isBlank()))
                || (budgetId != null && !budgetId.isEmpty() && categoryId != null && !categoryId.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        List<Transaction> transactions;
        if (categoryId != null) {
            var budgets = userPermissionsRepository.findAllByUser(user, null)
                    .stream()
                    .map(UserPermission::getBudget)
                    .collect(Collectors.toList());
            var category = categoryRepository.findByBudgetInAndId(budgets, categoryId).orElse(null);
            if (category == null) {
                return ResponseEntity.notFound().build();
            }
            transactions = transactionRepository.findAllByBudgetInAndCategoryInAndDateGreaterThanAndDateLessThan(
                    Collections.emptyList(),
                    List.of(category),
                    fromInstant,
                    toInstant,
                    null
            );
            AtomicLong balance = new AtomicLong(0L);
            transactions.forEach(transaction -> {
                if (transaction.getExpense()) {
                    balance.addAndGet(transaction.getAmount() * -1);
                } else {
                    balance.addAndGet(transaction.getAmount());
                }
            });
            return ResponseEntity.ok(new BalanceResponse(categoryId, balance.get()));
        } else {
            var userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, budgetId).orElse(null);
            if (userPermission == null) {
                return ResponseEntity.notFound().build();
            }

            if (userPermission.getPermission().isNotAtLeast(Permission.READ)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            transactions = transactionRepository.findAllByBudgetInAndDateGreaterThanAndDateLessThan(
                    List.of(userPermission.getBudget()),
                    fromInstant,
                    toInstant,
                    null
            );
            AtomicLong balance = new AtomicLong(0L);
            transactions.forEach(transaction -> {
                if (transaction.getExpense()) {
                    balance.addAndGet(transaction.getAmount() * -1);
                } else {
                    balance.addAndGet(transaction.getAmount());
                }
            });
            return ResponseEntity.ok(new BalanceResponse(budgetId, balance.get()));
        }
    }
}