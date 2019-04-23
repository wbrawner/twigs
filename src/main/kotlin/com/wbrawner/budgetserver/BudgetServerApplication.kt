package com.wbrawner.budgetserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BudgetServerApplication

fun main(args: Array<String>) {
    runApplication<BudgetServerApplication>(*args)
}
