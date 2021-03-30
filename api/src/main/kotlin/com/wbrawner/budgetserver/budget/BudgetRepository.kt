package com.wbrawner.budgetserver.budget

import org.springframework.data.repository.PagingAndSortingRepository

interface BudgetRepository : PagingAndSortingRepository<Budget, String>