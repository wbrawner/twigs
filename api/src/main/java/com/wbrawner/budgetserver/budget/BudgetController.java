package com.wbrawner.budgetserver.budget;

import com.wbrawner.budgetserver.permission.Permission;
import com.wbrawner.budgetserver.permission.UserPermission;
import com.wbrawner.budgetserver.permission.UserPermissionRepository;
import com.wbrawner.budgetserver.transaction.TransactionRepository;
import com.wbrawner.budgetserver.user.User;
import com.wbrawner.budgetserver.user.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wbrawner.budgetserver.Utils.getCurrentUser;
import static com.wbrawner.budgetserver.Utils.getFirstOfMonth;

@RestController
@RequestMapping(value = "/budgets")
@Api(value = "Budgets", tags = {"Budgets"}, authorizations = {@Authorization(value = "basic")})
@Transactional
public class BudgetController {
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionsRepository;

    public BudgetController(
            BudgetRepository budgetRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            UserPermissionRepository userPermissionsRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userPermissionsRepository = userPermissionsRepository;
    }

    @GetMapping(value = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getBudgets", nickname = "getBudgets", tags = {"Budgets"})
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
    @ApiOperation(value = "getBudget", nickname = "getBudget", tags = {"Budgets"})
    public ResponseEntity<BudgetResponse> getBudget(@PathVariable long id) {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, id).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }

        var budget = userPermission.getBudget();
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new BudgetResponse(budget, userPermissionsRepository.findAllByBudget(budget, null)));
    }

    @GetMapping(value = "/{id}/balance", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getBudgetBalance", nickname = "getBudgetBalance", tags = {"Budgets"})
    public ResponseEntity<BudgetBalanceResponse> getBudgetBalance(@PathVariable long id) {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, id).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }

        var budget = userPermission.getBudget();
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }
        var balance = transactionRepository.sumBalanceByBudgetId(budget.getId(), getFirstOfMonth());
        return ResponseEntity.ok(new BudgetBalanceResponse(budget.getId(), balance));
    }

    @PostMapping(value = "", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "newBudget", nickname = "newBudget", tags = {"Budgets"})
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

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "updateBudget", nickname = "updateBudget", tags = {"Budgets"})
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable long id, @RequestBody BudgetRequest request) {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, id).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }

        if (userPermission.getPermission().isNotAtLeast(Permission.MANAGE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var budget = userPermission.getBudget();
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }

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
        } else {
            users.addAll(userPermissionsRepository.findAllByBudget(budget, null));
        }

        return ResponseEntity.ok(new BudgetResponse(budgetRepository.save(budget), users));
    }

    @DeleteMapping(value = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ApiOperation(value = "deleteBudget", nickname = "deleteBudget", tags = {"Budgets"})
    public ResponseEntity<Void> deleteBudget(@PathVariable long id) {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        var userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, id).orElse(null);
        if (userPermission == null) {
            return ResponseEntity.notFound().build();
        }

        if (userPermission.getPermission().isNotAtLeast(Permission.MANAGE)) {
            return ResponseEntity.status(403).build();
        }

        var budget = userPermission.getBudget();
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }
        budgetRepository.delete(budget);
        return ResponseEntity.ok().build();
    }
}
