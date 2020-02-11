package com.wbrawner.budgetserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class BudgetServerApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<BudgetServerApplication>(*args)
        }
    }
}
