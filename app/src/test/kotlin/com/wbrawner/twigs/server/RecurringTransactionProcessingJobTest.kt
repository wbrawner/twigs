package com.wbrawner.twigs.server

import com.wbrawner.twigs.model.*
import com.wbrawner.twigs.storage.RecurringTransactionRepository
import com.wbrawner.twigs.storage.TransactionRepository
import com.wbrawner.twigs.test.helpers.repository.FakeRecurringTransactionsRepository
import com.wbrawner.twigs.test.helpers.repository.FakeTransactionRepository
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

class RecurringTransactionProcessingJobTest {
    private lateinit var recurringTransactionRepository: RecurringTransactionRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var job: RecurringTransactionProcessingJob

    @BeforeEach
    fun setup() {
        recurringTransactionRepository = FakeRecurringTransactionsRepository()
        transactionRepository = FakeTransactionRepository()
        job = RecurringTransactionProcessingJob(recurringTransactionRepository, transactionRepository)
    }

    @Test
    fun `daily transactions are created every day`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Daily transaction",
                amount = 123,
                frequency = Frequency.Daily(1, Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 3)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(3, createdTransactions.size)
        assertEquals("1970-01-01T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-01-02T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1970-01-03T09:00:00Z", createdTransactions[2].date.toString())
    }

    @Test
    fun `daily transactions are created every other day`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Daily transaction",
                amount = 123,
                frequency = Frequency.Daily(2, Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 3)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(2, createdTransactions.size)
    }

    @Test
    fun `weekly transactions are created every thursday`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Weekly transaction",
                amount = 123,
                frequency = Frequency.Weekly(1, setOf(DayOfWeek.THURSDAY), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 28)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(4, createdTransactions.size)
        assertEquals("1970-01-01T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-01-08T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1970-01-15T09:00:00Z", createdTransactions[2].date.toString())
        assertEquals("1970-01-22T09:00:00Z", createdTransactions[3].date.toString())
    }

    @Test
    fun `weekly transactions are created every third thursday`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Weekly transaction",
                amount = 123,
                frequency = Frequency.Weekly(3, setOf(DayOfWeek.THURSDAY), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 28)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(2, createdTransactions.size)
        assertEquals("1970-01-01T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-01-22T09:00:00Z", createdTransactions[1].date.toString())
    }

    @Test
    fun `monthly transactions are created every 1st of month`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Monthly transaction",
                amount = 123,
                frequency = Frequency.Monthly(1, DayOfMonth.day(1), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 90)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(3, createdTransactions.size)
        assertEquals("1970-01-01T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-02-01T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1970-03-01T09:00:00Z", createdTransactions[2].date.toString())
    }

    @Test
    fun `monthly transactions are created every last day of month when greater than max days in month`() =
        runBlockingTest {
            val start = Instant.parse("1970-01-01T00:00:00Z")
            recurringTransactionRepository.save(
                RecurringTransaction(
                    title = "Monthly transaction",
                    amount = 123,
                    frequency = Frequency.Monthly(1, DayOfMonth.day(31), Time(9, 0, 0)),
                    expense = true,
                    start = start,
                    createdBy = "tester",
                    budgetId = "budgetId"
                )
            )
            loopForDays(start, 120)
            val createdTransactions = transactionRepository.findAll()
            assertEquals(4, createdTransactions.size)
            assertEquals("1970-01-31T09:00:00Z", createdTransactions[0].date.toString())
            assertEquals("1970-02-28T09:00:00Z", createdTransactions[1].date.toString())
            assertEquals("1970-03-31T09:00:00Z", createdTransactions[2].date.toString())
            assertEquals("1970-04-30T09:00:00Z", createdTransactions[3].date.toString())
        }

    @Test
    fun `monthly transactions are created every 6 months`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Monthly transaction",
                amount = 123,
                frequency = Frequency.Monthly(6, DayOfMonth.day(15), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 197)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(2, createdTransactions.size)
        assertEquals("1970-01-15T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-07-15T09:00:00Z", createdTransactions[1].date.toString())
    }

    @Test
    fun `monthly transactions are created every 2nd tuesday`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Monthly transaction",
                amount = 123,
                frequency = Frequency.Monthly(
                    1,
                    DayOfMonth.positionalDayOfWeek(Position.SECOND, DayOfWeek.TUESDAY),
                    Time(9, 0, 0)
                ),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 120)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(4, createdTransactions.size)
        assertEquals("1970-01-13T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-02-10T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1970-03-10T09:00:00Z", createdTransactions[2].date.toString())
        assertEquals("1970-04-14T09:00:00Z", createdTransactions[3].date.toString())
    }

    @Test
    fun `monthly transactions are created every last friday`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Monthly transaction",
                amount = 123,
                frequency = Frequency.Monthly(
                    1,
                    DayOfMonth.positionalDayOfWeek(Position.LAST, DayOfWeek.FRIDAY),
                    Time(9, 0, 0)
                ),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 120)
        val createdTransactions = transactionRepository.findAll()
        assertEquals(4, createdTransactions.size)
        assertEquals("1970-01-30T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1970-02-27T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1970-03-27T09:00:00Z", createdTransactions[2].date.toString())
        assertEquals("1970-04-24T09:00:00Z", createdTransactions[3].date.toString())
    }

    @Test
    fun `yearly transactions are created every march 31st`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Yearly transaction",
                amount = 123,
                frequency = Frequency.Yearly(1, MonthDay.of(3, 31), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 1460) // 4 years from Jan 1, 1970
        val createdTransactions = transactionRepository.findAll()
        assertEquals(4, createdTransactions.size)
        assertEquals("1970-03-31T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1971-03-31T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1972-03-31T09:00:00Z", createdTransactions[2].date.toString()) // 1972 was a leap year
        assertEquals("1973-03-31T09:00:00Z", createdTransactions[3].date.toString())
    }

    @Test
    fun `yearly transactions are created every other march 31st`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Yearly transaction",
                amount = 123,
                frequency = Frequency.Yearly(2, MonthDay.of(3, 31), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 1460) // 4 years from Jan 1, 1970
        val createdTransactions = transactionRepository.findAll()
        assertEquals(2, createdTransactions.size)
        assertEquals("1970-03-31T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1972-03-31T09:00:00Z", createdTransactions[1].date.toString()) // 1972 was a leap year
    }

    @Test
    fun `yearly transactions are created every february 29th`() = runBlockingTest {
        val start = Instant.parse("1970-01-01T00:00:00Z")
        recurringTransactionRepository.save(
            RecurringTransaction(
                title = "Yearly transaction",
                amount = 123,
                frequency = Frequency.Yearly(1, MonthDay.of(2, 29), Time(9, 0, 0)),
                expense = true,
                start = start,
                createdBy = "tester",
                budgetId = "budgetId"
            )
        )
        loopForDays(start, 1460) // 4 years from Jan 1, 1970
        val createdTransactions = transactionRepository.findAll()
        assertEquals(4, createdTransactions.size)
        assertEquals("1970-02-28T09:00:00Z", createdTransactions[0].date.toString())
        assertEquals("1971-02-28T09:00:00Z", createdTransactions[1].date.toString())
        assertEquals("1972-02-29T09:00:00Z", createdTransactions[2].date.toString()) // 1972 was a leap year
        assertEquals("1973-02-28T09:00:00Z", createdTransactions[3].date.toString())
    }

    private suspend fun loopForDays(start: Instant, days: Int) {
        if (days == 0) return
        val maxDays = GregorianCalendar.from(ZonedDateTime.ofInstant(start, ZoneId.of("UTC")))
            .getActualMaximum(Calendar.DAY_OF_MONTH)
        job.createTransactions(start, maxDays)
        loopForDays(start.plus(1, ChronoUnit.DAYS), days - 1)
    }
}