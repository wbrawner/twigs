package com.wbrawner.twigs.server.budget

import org.springframework.data.repository.PagingAndSortingRepository

interface BudgetRepository : PagingAndSortingRepository<Budget, String>