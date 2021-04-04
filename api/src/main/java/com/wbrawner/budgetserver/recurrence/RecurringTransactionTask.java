package com.wbrawner.budgetserver.recurrence;

import com.wbrawner.budgetserver.transaction.Transaction;
import com.wbrawner.budgetserver.transaction.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Component
public class RecurringTransactionTask {
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public RecurringTransactionTask(
            RecurringTransactionRepository recurringTransactionRepository,
            TransactionRepository transactionRepository
    ) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void createTransactions() {
        recurringTransactionRepository.findAll().forEach(recurringTransaction -> {
            GregorianCalendar today = new GregorianCalendar(TimeZone.getTimeZone(recurringTransaction.getTimeZone()));
            // if recurrence matches today, create a new transaction
            int adjustedFrequencyValue, calendarField;
            switch (recurringTransaction.getFrequencyUnit()) {
                case DAILY -> {
                    // Daily transactions should have the frequency value set to 0, so we just force it to be the same
                    // as the current date in order to force the transaction creation.
                    adjustedFrequencyValue = today.get(Calendar.DATE);
                    calendarField = Calendar.DATE;
                }
                case WEEKLY -> {
                    // No adjustments needed for day of week
                    adjustedFrequencyValue = today.get(Calendar.DAY_OF_WEEK);
                    calendarField = Calendar.DAY_OF_WEEK;
                }
                case MONTHLY -> {
                    // Check if the day of the month is correct
                    adjustedFrequencyValue = Math.min(recurringTransaction.getFrequencyValue(), today.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calendarField = Calendar.DAY_OF_MONTH;
                }
                case YEARLY -> {
                    adjustedFrequencyValue = recurringTransaction.getFrequencyValue();
                    if (today.isLeapYear(today.get(Calendar.YEAR)) && today.get(Calendar.DAY_OF_YEAR) >= 31 + 29) {
                        // We're just pretending that Feb 29th doesn't exist here...
                        adjustedFrequencyValue -= 1;
                    }
                    calendarField = Calendar.DAY_OF_YEAR;
                }
                default -> throw new IllegalStateException("Unexpected value: " + recurringTransaction.getFrequencyUnit());
            }
            if (adjustedFrequencyValue == today.get(calendarField)) {
                createTransaction(recurringTransaction, today);
            }
        });
    }

    private void createTransaction(RecurringTransaction recurringTransaction, Calendar transactionCalendar) {
        transactionCalendar.set(Calendar.HOUR, 0);
        transactionCalendar.set(Calendar.MINUTE, 0);
        transactionCalendar.set(Calendar.SECOND, 0);
        transactionCalendar.set(Calendar.MILLISECOND, 0);
        transactionCalendar.add(Calendar.SECOND, recurringTransaction.getTimeOfDayInSeconds());
        transactionRepository.save(new Transaction(
                recurringTransaction.getTitle(),
                recurringTransaction.getDescription(),
                transactionCalendar.toInstant(),
                recurringTransaction.getAmount(),
                recurringTransaction.getCategory(),
                recurringTransaction.isExpense(),
                recurringTransaction.getCreatedBy(),
                recurringTransaction.getBudget(),
                recurringTransaction
        ));
    }
}
