package com.wbrawner.twigs.permission;

import com.wbrawner.twigs.budget.Budget;
import com.wbrawner.twigs.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionRepository extends CrudRepository<UserPermission, UserPermissionKey>,
        PagingAndSortingRepository<UserPermission, UserPermissionKey> {
    Optional<UserPermission> findByUserAndBudget_Id(User user, String budgetId);

    List<UserPermission> findAllByUser(User user, Pageable pageable);

    List<UserPermission> findAllByBudget(Budget budget, Pageable pageable);

    List<UserPermission> findAllByUserAndBudget(User user, Budget budget, Pageable pageable);

    List<UserPermission> findAllByUserAndBudget_IdIn(User user, List<String> budgetIds, Pageable pageable);

    void deleteAllByBudget(Budget budget);
}