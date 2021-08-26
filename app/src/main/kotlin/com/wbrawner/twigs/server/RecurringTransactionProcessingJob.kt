package com.wbrawner.twigs.server

import com.wbrawner.twigs.model.Frequency
import com.wbrawner.twigs.model.Position
import com.wbrawner.twigs.storage.RecurringTransactionRepository
import com.wbrawner.twigs.storage.TransactionRepository
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.ceil

class RecurringTransactionProcessingJob(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) : Job {
    override suspend fun run() {
        val now = Instant.now()
        val maxDaysInMonth = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
            .getActualMaximum(Calendar.DAY_OF_MONTH)
        createTransactions(now, maxDaysInMonth)
    }

    suspend fun createTransactions(now: Instant, maxDaysInMonth: Int) {
        recurringTransactionRepository.findAll(now).forEach {
            val zonedNow = now.atZone(ZoneId.of("UTC"))
            when (it.frequency) {
                is Frequency.Daily -> {
                    if (it.lastRun != null && ChronoUnit.DAYS.between(it.lastRun, now) < it.frequency.count)
                        return@forEach
                }
                is Frequency.Weekly -> {
                    it.lastRun?.let { last ->
                        val zonedLastRun = last.atZone(ZoneId.of("UTC"))
                        if (ChronoUnit.WEEKS.between(zonedLastRun, zonedNow) < it.frequency.count)
                            return@forEach
                    }
                    if (!(it.frequency as Frequency.Weekly).daysOfWeek.contains(DayOfWeek.from(zonedNow)))
                        return@forEach
                }
                is Frequency.Monthly -> {
                    it.lastRun?.let { last ->
                        val zonedLastRun = last.atZone(ZoneId.of("UTC"))
                        if (zonedNow.monthValue - zonedLastRun.monthValue < it.frequency.count)
                            return@forEach
                    }
                    val frequency = (it.frequency as Frequency.Monthly).dayOfMonth
                    frequency.day?.let { day ->
                        if (zonedNow.dayOfMonth != Integer.min(day, maxDaysInMonth))
                            return@forEach
                    }
                    frequency.positionalDayOfWeek?.let { positionalDayOfWeek ->
                        if (positionalDayOfWeek.dayOfWeek != DayOfWeek.from(now.atZone(ZoneId.of("UTC"))))
                            return@forEach
                        val dayOfMonth = now.atZone(ZoneId.of("UTC")).dayOfMonth
                        val position = ceil(dayOfMonth / 7.0).toInt()
                        when (positionalDayOfWeek.position) {
                            Position.FIRST -> if (position != 1) return@forEach
                            Position.SECOND -> if (position != 2) return@forEach
                            Position.THIRD -> if (position != 3) return@forEach
                            Position.FOURTH -> if (position != 4) return@forEach
                            Position.LAST -> {
                                if (dayOfMonth + 7 <= maxDaysInMonth)
                                    return@forEach
                            }
                        }

                    }
                }
                is Frequency.Yearly -> {
                    it.lastRun?.let { last ->
                        val zonedLastRun = last.atZone(ZoneId.of("UTC"))
                        if (zonedNow.year - zonedLastRun.year < it.frequency.count)
                            return@forEach
                    }
                    with((it.frequency as Frequency.Yearly).dayOfYear) {
                        // If the user has selected Feb 29th, then on non-leap years we'll adjust the date to Feb 28th
                        val adjustedMonthDay =
                            if (this.month == Month.FEBRUARY && this.dayOfMonth == 29 && !Year.isLeap(zonedNow.year.toLong())) {
                                MonthDay.of(2, 28)
                            } else {
                                this
                            }
                        if (MonthDay.from(zonedNow) != adjustedMonthDay)
                            return@forEach
                    }
                }
            }
            transactionRepository.save(it.toTransaction(now))
            recurringTransactionRepository.save(it.copy(lastRun = now))
        }
    }
}