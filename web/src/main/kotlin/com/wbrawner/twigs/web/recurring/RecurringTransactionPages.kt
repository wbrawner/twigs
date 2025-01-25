package com.wbrawner.twigs.web.recurring

import com.wbrawner.twigs.asFrequency
import com.wbrawner.twigs.model.DayOfMonth
import com.wbrawner.twigs.model.Frequency
import com.wbrawner.twigs.model.Position
import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.BudgetListItem
import com.wbrawner.twigs.web.ListGroup
import com.wbrawner.twigs.web.budget.toCurrencyString
import com.wbrawner.twigs.web.category.CategoryOption
import com.wbrawner.twigs.web.recurring.RecurringTransactionFormPage.Option
import com.wbrawner.twigs.web.transaction.TransactionListItem
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Month
import kotlin.enums.EnumEntries

data class RecurringTransactionListPage(
    val budget: BudgetResponse,
    val transactions: List<ListGroup<TransactionListItem>>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = "Recurring Transactions"
}

fun RecurringTransactionResponse.toListItem(numberFormat: NumberFormat) = TransactionListItem(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    budgetId = budgetId,
    expenseClass = if (expense != false) "expense" else "income",
    amountLabel = (amount ?: 0L).toCurrencyString(numberFormat)
)

data class RecurringTransactionDetailsPage(
    val transaction: RecurringTransactionResponse,
    val category: CategoryResponse?,
    val budget: BudgetResponse,
    val amountLabel: String,
    val startLabel: String,
    val finishLabel: String,
    val createdBy: UserResponse,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = transaction.title.orEmpty()

    val frequencyValue: String = when (val frequency = transaction.frequency.asFrequency()) {
        is Frequency.Daily -> if (frequency.count == 1) "Every day" else "Every ${frequency.count} days"
        is Frequency.Weekly -> if (frequency.count == 1) {
            "Every week on "
        } else {
            "Every ${frequency.count} weeks on"
        }.plus(frequency.daysOfWeek.joinToString(", ") { it.name.capitalize() })
        is Frequency.Monthly -> if (frequency.count == 1) {
            "Every month on the"
        } else {
            "Every ${frequency.count} months on the"
        }.plus(when (val dayOfMonth = frequency.dayOfMonth) {
            is DayOfMonth.FixedDayOfMonth -> dayOfMonth.selection.toOrdinalString()
            is DayOfMonth.PositionalDayOfMonth -> "${dayOfMonth.position.name.capitalize()} ${dayOfMonth.selection.name.capitalize()}"
        })
        is Frequency.Yearly -> if (frequency.count == 1) {
            "Every year on "
        } else {
            "Every ${frequency.count} years on"
        }.plus("${frequency.dayOfYear.month.name.capitalize()} ${frequency.dayOfYear.dayOfMonth.toOrdinalString()}")
    }
}

data class RecurringTransactionFormPage(
    val transaction: RecurringTransactionResponse,
    val amountLabel: String,
    val budget: BudgetResponse,
    val categoryOptions: List<CategoryOption>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (transaction.id.isBlank()) {
        "New Recurring Transaction"
    } else {
        "Edit Recurring Transaction"
    }

    val frequencyCount: Int = 1

    val frequencyUnitOptions: List<Option> = listOf(
        Frequency.Daily::class,
        Frequency.Weekly::class,
        Frequency.Monthly::class,
        Frequency.Yearly::class
    )
        .map {
            Option(
                value = it.simpleName!!.uppercase(),
                title = it.simpleName!!.replace("ly", "(s)").replace("i", "y"),
                checked = it.simpleName!!.first() == transaction.frequency.first(),
                selected = it.simpleName!!.first() == transaction.frequency.first(),
                disabled = false
            )

        }

    val dayOfWeekOptions: List<Option> = DayOfWeek.entries.toOptionsList(
        selected = {
            transaction.frequency.contains(it.name)
        }
    )

    val positionOptions: List<Option> = Position.entries.toOptionsList(
        selected = {
            transaction.frequency.contains(it.name)
        }
    )

    val dayOfMonthOptions: List<Option> = (1..31).map {
        Option(
            value = it.toString(),
            title = it.toString(),
            selected = when (val frequency = transaction.frequency.asFrequency()) {
                is Frequency.Monthly -> frequency.dayOfMonth.selection == it
                is Frequency.Yearly -> frequency.dayOfYear.dayOfMonth == it
                else -> false
            },
            disabled = false
        )
    }

    val monthsOfYearOptions: List<Option> = Month.entries.toOptionsList(
        selected = {
            when (val frequency = transaction.frequency.asFrequency()) {
                is Frequency.Yearly -> frequency.dayOfYear.month == it
                else -> false
            }
        }
    )

    data class Option(
        val value: String,
        val title: String,
        val checked: String = "",
        val selected: String = "",
        val disabled: String = "",
    ) {
        constructor(
            value: String,
            title: String,
            checked: Boolean = false,
            selected: Boolean = false,
            disabled: Boolean = false
        ) : this(
            value = value,
            title = title,
            checked = if (checked) "checked" else "",
            selected = if (selected) "selected" else "",
            disabled = if (disabled) "disabled" else ""
        )
    }
}

private fun EnumEntries<*>.toOptionsList(
    checked: (Enum<*>) -> Boolean = { false },
    selected: (Enum<*>) -> Boolean = { false },
    disabled: (Enum<*>) -> Boolean = { false },
) = map { entry ->
    Option(
        value = entry.name,
        title = entry.name.capitalize(),
        checked = checked(entry),
        selected = selected(entry),
        disabled = disabled(entry)
    )
}

private fun String.capitalize() = mapIndexed { i, c ->
    if (i == 0) {
        c.titlecase()
    } else {
        c.lowercase()
    }
}.joinToString("")

private fun Int.toOrdinalString() = when {
    toString().endsWith("1") -> "${this}st"
    toString().endsWith("2") -> "${this}nd"
    toString().endsWith("3") -> "${this}rd"
    else -> "${this}th"
}