package com.wbrawner.budgetserver.recurrence;

import com.wbrawner.budgetserver.ErrorResponse;
import com.wbrawner.budgetserver.category.Category;
import com.wbrawner.budgetserver.category.CategoryRepository;
import com.wbrawner.budgetserver.permission.Permission;
import com.wbrawner.budgetserver.permission.UserPermission;
import com.wbrawner.budgetserver.permission.UserPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.wbrawner.budgetserver.Utils.getCurrentUser;

@RestController
@RequestMapping(path = "/recurrence")
@Transactional
public class RecurringTransactionController {
    private final CategoryRepository categoryRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final UserPermissionRepository userPermissionsRepository;

    private final Logger logger = LoggerFactory.getLogger(RecurringTransactionController.class);

    public RecurringTransactionController(
            CategoryRepository categoryRepository,
            RecurringTransactionRepository recurringTransactionRepository,
            UserPermissionRepository userPermissionsRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.userPermissionsRepository = userPermissionsRepository;
    }

    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<RecurringTransactionResponse>> getRecurringTransactions(
            @RequestParam("budgetId") String budgetId
    ) {
        List<UserPermission> userPermissions = userPermissionsRepository.findAllByUserAndBudget_IdIn(
                getCurrentUser(),
                Collections.singletonList(budgetId),
                null
        );
        if (userPermissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var budget = userPermissions.get(0).getBudget();
        var transactions = recurringTransactionRepository.findAllByBudget(budget)
                .stream()
                .map(RecurringTransactionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RecurringTransactionResponse> getRecurringTransaction(@PathVariable String id) {
        var budgets = userPermissionsRepository.findAllByUser(getCurrentUser(), null)
                .stream()
                .map(UserPermission::getBudget)
                .collect(Collectors.toList());
        var transaction = recurringTransactionRepository.findByIdAndBudgetIn(id, budgets).orElse(null);
        if (transaction == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RecurringTransactionResponse(transaction));
    }

    @PostMapping(path = "", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> newTransaction(@RequestBody RecurringTransactionRequest request) {
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
        return ResponseEntity.ok(new RecurringTransactionResponse(recurringTransactionRepository.save(new RecurringTransaction(
                request.getTitle(),
                request.getDescription(),
                request.getFrequencyUnit(),
                request.getFrequencyValue(),
                request.getTimeZone(),
                request.getTime(),
                request.getAmount(),
                category,
                request.getExpense(),
                getCurrentUser(),
                budget
        ))));
    }

    @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> updateTransaction(@PathVariable String id, @RequestBody RecurringTransactionRequest request) {
        var transaction = recurringTransactionRepository.findById(id).orElse(null);
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
        if (request.getTimeZone() != null) {
            transaction.setTimeZone(request.getTimeZone());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getExpense() != null) {
            transaction.setExpense(request.getExpense());
        }
        if (request.getTime() != null) {
            transaction.setTimeOfDayInSeconds(request.getTime());
        }
        if (request.getFrequencyUnit() != null && request.getFrequencyValue() != null) {
            transaction.setFrequency(request.getFrequencyUnit(), request.getFrequencyValue());
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
        return ResponseEntity.ok(new RecurringTransactionResponse(recurringTransactionRepository.save(transaction)));
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        var transaction = recurringTransactionRepository.findById(id).orElse(null);
        if (transaction == null) return ResponseEntity.notFound().build();
        // Check that the transaction belongs to an budget that the user has access to before deleting it
        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(getCurrentUser(), transaction.getBudget().getId()).orElse(null);
        if (userPermission == null) return ResponseEntity.notFound().build();
        if (userPermission.getPermission().isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        recurringTransactionRepository.delete(transaction);
        return ResponseEntity.ok().build();
    }
}