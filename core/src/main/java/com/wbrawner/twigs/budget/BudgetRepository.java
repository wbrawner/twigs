package com.wbrawner.twigs.budget;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BudgetRepository extends CrudRepository<Budget, String>,
        PagingAndSortingRepository<Budget, String> {
}