package com.wbrawner.twigs.web

import com.wbrawner.twigs.endOfMonth
import com.wbrawner.twigs.firstOfMonth
import com.wbrawner.twigs.model.Frequency
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionResponse
import com.wbrawner.twigs.service.transaction.TransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.toInstant
import com.wbrawner.twigs.toInstantOrNull
import com.wbrawner.twigs.web.category.CategoryOption
import com.wbrawner.twigs.web.category.asOption
import com.wbrawner.twigs.web.recurring.toListItem
import com.wbrawner.twigs.web.transaction.toListItem
import io.ktor.http.*
import io.ktor.server.util.*
import io.ktor.util.date.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
val decimalFormat: NumberFormat = DecimalFormat.getNumberInstance(Locale.US).apply {
    with(this as DecimalFormat) {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            currencySymbol = ""
            isGroupingUsed = false
        }
    }
}

val dateFormat = DateTimeFormatter.ofPattern("H:mm a 'on' MMMM d, yyyy")

val webInputFormat = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss")

val shortDateFormat = DateTimeFormatter.ofPattern("M/d/yy")

fun Parameters.getAmount() = decimalFormat.parse(get("amount"))
    ?.toDouble()
    ?.toBigDecimal()
    ?.times(BigDecimal(100))
    ?.toLong()
    ?: 0L

fun Parameters.getDateString(name: String) = get(name)?.takeUnless { it.isBlank() }?.plus(":00Z")

fun Long?.toDecimalString(): String {
    if (this == null) return ""
    return decimalFormat.format(toBigDecimal().divide(BigDecimal(100), 2, RoundingMode.HALF_UP))
}

fun Instant.toHtmlInputString() = truncatedTo(ChronoUnit.MINUTES).toString().substringBefore(":00Z")

data class ListGroup<T>(val label: String, val items: List<T>)

fun List<TransactionResponse>.groupByDate() =
    groupBy {
        it.date.toInstant().truncatedTo(ChronoUnit.DAYS)
    }
        .entries
        .sortedByDescending { it.key }
        .map { (date, transactions) ->
            ListGroup(
                shortDateFormat.format(date.atOffset(ZoneOffset.UTC)),
                transactions.map { it.toListItem(currencyFormat) })
        }


val RecurringTransactionResponse.isThisMonth: Boolean
    get() {
        if (isExpired) {
            return false
        }
        // TODO: Check user's timezone for this
        return when (val frequencyObj = Frequency.parse(frequency)) {
            is Frequency.Daily -> true
            is Frequency.Weekly -> frequencyObj.count < 5
                    || (lastRun ?: start).toInstant()
                .plus(frequencyObj.count.toLong(), ChronoUnit.WEEKS)
                .run {
                    isAfter(firstOfMonth) && isBefore(endOfMonth)
                }

            is Frequency.Monthly -> frequencyObj.count < 2
                    || (lastRun ?: start).toInstant()
                .plus(frequencyObj.count.toLong(), ChronoUnit.MONTHS)
                .run {
                    isAfter(firstOfMonth) && isBefore(endOfMonth)
                }

            is Frequency.Yearly -> ZonedDateTime.now().month == frequencyObj.dayOfYear.month
        }
    }

val RecurringTransactionResponse.isExpired: Boolean
    get() = finish?.toInstantOrNull()?.let { firstOfMonth > it } == true

fun List<RecurringTransactionResponse>.groupByOccurrence() =
    groupBy {
        when {
            it.isThisMonth -> RecurringTransactionOccurrence.THIS_MONTH
            !it.isExpired -> RecurringTransactionOccurrence.FUTURE
            else -> RecurringTransactionOccurrence.EXPIRED
        }
    }
        .entries
        .sortedBy { (k, _) -> k.ordinal }
        .map { (occurrence, transactions) ->
            // TODO: I18n
            ListGroup(occurrence.niceName, transactions.sortedBy { it.title }.map { it.toListItem(currencyFormat) })
        }

enum class RecurringTransactionOccurrence {
    THIS_MONTH,
    FUTURE,
    EXPIRED;

    val niceName = name.split("_")
        .joinToString(" ") { word ->
            word.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}

suspend fun categoryOptions(
    selectedCategoryId: String?,
    categoryService: CategoryService,
    budgetId: String,
    user: UserResponse
): List<CategoryOption> {
    val categoryOptions = listOf(
        CategoryOption(
            "",
            "Select a category",
            isSelected = selectedCategoryId.isNullOrBlank(),
            isDisabled = true
        ),
        CategoryOption("income", "Income", isDisabled = true),
    )
        .plus(
            categoryService.categories(
                budgetIds = listOf(budgetId),
                userId = user.id,
                expense = false,
                archived = false
            ).map { category ->
                category.asOption(selectedCategoryId.orEmpty())
            }
        )
        .plus(
            CategoryOption("expense", "Expense", isDisabled = true),
        )
        .plus(
            categoryService.categories(
                budgetIds = listOf(budgetId),
                userId = user.id,
                expense = true,
                archived = false
            ).map { category ->
                category.asOption(selectedCategoryId.orEmpty())
            }
        )
    return categoryOptions
}
