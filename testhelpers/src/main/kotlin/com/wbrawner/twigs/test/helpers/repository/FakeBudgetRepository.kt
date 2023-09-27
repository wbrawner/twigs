package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.storage.BudgetRepository

class FakeBudgetRepository : FakeRepository<Budget>(), BudgetRepository