package com.wbrawner.twigs.service.recurringtransaction

import com.wbrawner.twigs.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Month
import java.time.MonthDay
import java.time.temporal.ChronoUnit

@Serializable
data class RecurringTransactionRequest(
    val title: String? = null,
    val description: String? = null,
    val amount: Long? = null,
    val categoryId: String? = null,
    val expense: Boolean? = null,
    val budgetId: String? = null,
    val frequency: FrequencyApi,
    val start: String? = null,
    val finish: String? = null,
)

@Serializable
data class RecurringTransactionResponse(
    val id: String,
    val title: String?,
    val description: String?,
    val frequency: FrequencyApi,
    val start: String,
    val finish: String?,
    val lastRun: String?,
    val amount: Long?,
    val expense: Boolean?,
    val budgetId: String,
    val categoryId: String?,
    val createdBy: String
)

fun RecurringTransaction.asResponse(): RecurringTransactionResponse = RecurringTransactionResponse(
    id = id,
    title = title,
    description = description,
    frequency = frequency.asResponse(),
    start = start.truncatedTo(ChronoUnit.SECONDS).toString(),
    finish = finish?.truncatedTo(ChronoUnit.SECONDS)?.toString(),
    lastRun = lastRun?.truncatedTo(ChronoUnit.SECONDS)?.toString(),
    amount = amount,
    expense = expense,
    budgetId = budgetId,
    categoryId = categoryId,
    createdBy = createdBy
)

@Serializable
sealed class FrequencyApi {
    abstract val count: Int
    abstract val time: TimeApi

    @Serializable
    @SerialName("DAILY")
    data class Daily(override val count: Int, override val time: TimeApi) : FrequencyApi()

    @Serializable
    @SerialName("WEEKLY")
    data class Weekly(override val count: Int, override val time: TimeApi, val daysOfWeek: Set<DayOfWeek>) :
        FrequencyApi()

    @Serializable
    @SerialName("MONTHLY")
    data class Monthly(override val count: Int, override val time: TimeApi, val dayOfMonth: DayOfMonthApi) :
        FrequencyApi() {
        @Serializable
        sealed class DayOfMonthApi {
            @Serializable
            @SerialName("FIXED")
            data class FixedDayOfMonth(val day: Int) : DayOfMonthApi()

            @Serializable
            @SerialName("POSITIONAL")
            data class PositionalDayOfMonth(val position: Position, val day: DayOfWeek) : DayOfMonthApi()
        }
    }

    @Serializable
    @SerialName("YEARLY")
    data class Yearly(override val count: Int, override val time: TimeApi, val day: Int, val month: Month) :
        FrequencyApi()
}

@Serializable
data class TimeApi(val hours: Int, val minutes: Int, val seconds: Int)

fun Frequency.asResponse(): FrequencyApi = when (this) {
    is Frequency.Daily -> FrequencyApi.Daily(count, time.asResponse())
    is Frequency.Weekly -> FrequencyApi.Weekly(count, time.asResponse(), daysOfWeek)
    is Frequency.Monthly -> FrequencyApi.Monthly(count, time.asResponse(), dayOfMonth.asResponse())
    is Frequency.Yearly -> FrequencyApi.Yearly(count, time.asResponse(), dayOfYear.dayOfMonth, dayOfYear.month)
}

fun Time.asResponse(): TimeApi = TimeApi(hours, minutes, seconds)

fun <T> DayOfMonth<T>.asResponse(): FrequencyApi.Monthly.DayOfMonthApi = when (this) {
    is DayOfMonth.FixedDayOfMonth -> FrequencyApi.Monthly.DayOfMonthApi.FixedDayOfMonth(selection)
    is DayOfMonth.PositionalDayOfMonth -> FrequencyApi.Monthly.DayOfMonthApi.PositionalDayOfMonth(position, selection)
}

fun FrequencyApi.asFrequency(): Frequency = when (this) {
    is FrequencyApi.Daily -> Frequency.Daily(count = count, time = time.asTime())
    is FrequencyApi.Weekly -> Frequency.Weekly(count = count, time = time.asTime(), daysOfWeek = daysOfWeek)
    is FrequencyApi.Monthly -> Frequency.Monthly(
        count = count,
        time = time.asTime(),
        dayOfMonth = dayOfMonth.asDayOfMonth()
    )

    is FrequencyApi.Yearly -> Frequency.Yearly(count = count, time = time.asTime(), dayOfYear = MonthDay.of(month, day))
}

fun TimeApi.asTime() = Time(hours = hours, minutes = minutes, seconds = seconds)

fun FrequencyApi.Monthly.DayOfMonthApi.asDayOfMonth() = when (this) {
    is FrequencyApi.Monthly.DayOfMonthApi.FixedDayOfMonth -> DayOfMonth.FixedDayOfMonth(day)
    is FrequencyApi.Monthly.DayOfMonthApi.PositionalDayOfMonth -> DayOfMonth.PositionalDayOfMonth(position, day)
}