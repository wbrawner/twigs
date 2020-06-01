package com.wbrawner.budgetserver.permission;

import com.wbrawner.budgetserver.budget.Budget;
import com.wbrawner.budgetserver.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionRepository extends PagingAndSortingRepository<UserPermission, UserPermissionKey> {
    Optional<UserPermission> findByUserAndBudget_Id(User user, Long budgetId);

    List<UserPermission> findAllByUser(User user, Pageable pageable);

    List<UserPermission> findAllByBudget(Budget budget, Pageable pageable);

    List<UserPermission> findAllByUserAndBudget(User user, Budget budget, Pageable pageable);

    List<UserPermission> findAllByUserAndBudget_IdIn(User user, List<Long> budgetIds, Pageable pageable);
}