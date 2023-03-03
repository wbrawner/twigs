package com.wbrawner.twigs.budget;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface BudgetRepository extends PagingAndSortingRepository<Budget, String> {
}