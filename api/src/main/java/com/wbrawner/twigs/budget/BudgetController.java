package com.wbrawner.twigs.budget;

import com.wbrawner.twigs.category.CategoryRepository;
import com.wbrawner.twigs.permission.Permission;
import com.wbrawner.twigs.permission.UserPermission;
import com.wbrawner.twigs.permission.UserPermissionRepository;
import com.wbrawner.twigs.transaction.TransactionRepository;
import com.wbrawner.twigs.user.User;
import com.wbrawner.twigs.user.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wbrawner.twigs.Utils.getCurrentUser;

@RestController
@RequestMapping(value = "/api/budgets")
@Transactional
public class BudgetController {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionsRepository;
    private final Logger logger = LoggerFactory.getLogger(BudgetController.class);

    public BudgetController(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            UserPermissionRepository userPermissionsRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userPermissionsRepository = userPermissionsRepository;
    }

    @GetMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<BudgetResponse>> getBudgets(Integer page, Integer count) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<BudgetResponse> budgets = userPermissionsRepository.findAllByUser(
                        getCurrentUser(),
                        PageRequest.of(
                                page != null ? page : 0,
                                count != null ? count : 1000
                        )
                )
                .stream()
                .map(userPermission -> {
                    Budget budget = userPermission.getBudget();
                    if (budget == null) {
                        return null;
                    }
//                    Hibernate.initialize(budget);
                    return new BudgetResponse(budget, userPermissionsRepository.findAllByBudget(budget, null));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(budgets);
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BudgetResponse> getBudget(@PathVariable String id) {
        return getBudgetWithPermission(id, Permission.READ, (budget) ->
                ResponseEntity.ok(new BudgetResponse(budget, userPermissionsRepository.findAllByBudget(budget, null)))
        );
    }

    @PostMapping(
            value = "",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<BudgetResponse> newBudget(@RequestBody BudgetRequest request) {
        final var budget = budgetRepository.save(new Budget(request.name, request.description));
        var users = request.getUsers()
                .stream()
                .map(userPermissionRequest -> {
                    var user = userRepository.findById(userPermissionRequest.getUser()).orElse(null);
                    if (user == null) {
                        return null;
                    }

                    return userPermissionsRepository.save(
                            new UserPermission(budget, user, userPermissionRequest.getPermission())
                    );
                })
                .collect(Collectors.toSet());

        var currentUserIncluded = users.stream().anyMatch(userPermission ->
                userPermission.getUser().getId().equals(getCurrentUser().getId())
        );
        if (!currentUserIncluded) {
            users.add(
                    userPermissionsRepository.save(
                            new UserPermission(budget, getCurrentUser(), Permission.OWNER)
                    )
            );
        }
        return ResponseEntity.ok(new BudgetResponse(budget, new ArrayList<>(users)));
    }

    @PutMapping(
            value = "/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable String id, @RequestBody BudgetRequest request) {
        // TODO: Make sure no changes in ownership are being attempted (except by the owner)
        return getBudgetWithPermission(id, Permission.MANAGE, (budget) -> {
            if (request.name != null) {
                budget.setName(request.name);
            }

            if (request.description != null) {
                budget.setDescription(request.description);
            }

            var users = new ArrayList<UserPermission>();
            if (!request.getUsers().isEmpty()) {
                request.getUsers().forEach(userPermissionRequest ->
                        userRepository.findById(userPermissionRequest.getUser()).ifPresent(requestedUser ->
                                users.add(userPermissionsRepository.save(
                                        new UserPermission(
                                                budget,
                                                requestedUser,
                                                userPermissionRequest.getPermission()
                                        )
                                ))
                        ));
            }
            if (users.isEmpty()) {
                users.addAll(userPermissionsRepository.findAllByBudget(budget, null));
            }

            return ResponseEntity.ok(new BudgetResponse(budgetRepository.save(budget), users));
        });
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable String id) {
        return getBudgetWithPermission(id, Permission.MANAGE, (budget) -> {
            categoryRepository.deleteAllByBudget(budget);
            transactionRepository.deleteAllByBudget(budget);
            userPermissionsRepository.deleteAllByBudget(budget);
            budgetRepository.delete(budget);
            return ResponseEntity.noContent().build();
        });
    }

    private <T> ResponseEntity<T> getBudgetWithPermission(
            String budgetId,
            Permission permission,
            Function<Budget, ResponseEntity<T>> callback
    ) {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, budgetId).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }

        if (userPermission.getPermission().isNotAtLeast(permission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var budget = userPermission.getBudget();
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }

        return callback.apply(budget);
    }
}
