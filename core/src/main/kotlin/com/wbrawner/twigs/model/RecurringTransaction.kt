package com.wbrawner.twigs.model

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString
import java.time.DayOfWeek
import java.time.Instant
import java.time.MonthDay

data class RecurringTransaction(
    override val id: String = randomString(),
    val title: String,
    val description: String? = null,
    val frequency: Frequency,
    val start: Instant,
    val finish: Instant? = null,
    val amount: Long,
    val expense: Boolean,
    val createdBy: String,
    val categoryId: String? = null,
    val budgetId: String,
    val lastRun: Instant? = null
) : Identifiable {
    fun toTransaction(now: Instant = Instant.now()): Transaction = Transaction(
        title = title,
        description = description,
        date = frequency.instant(now),
        amount = amount,
        expense = expense,
        createdBy = createdBy,
        categoryId = categoryId,
        budgetId = budgetId
    )
}

sealed class Frequency {
    abstract val count: Int
    abstract val time: Time

    data class Daily(override val count: Int, override val time: Time) : Frequency() {
        override fun toString(): String = "D;$count;$time"

        companion object {
            fun parse(s: String): Daily {
                require(s[0] == 'D') { "Invalid format for Daily: $s" }
                return with(s.split(';')) {
                    Daily(
                        get(1).toInt(),
                        Time.parse(get(2))
                    )
                }
            }
        }
    }

    data class Weekly(override val count: Int, val daysOfWeek: Set<DayOfWeek>, override val time: Time) : Frequency() {
        override fun toString(): String = "W;$count;${daysOfWeek.joinToString(",")};$time"
        companion object {
            fun parse(s: String): Weekly {
                require(s[0] == 'W') { "Invalid format for Weekly: $s" }
                return with(s.split(';')) {
                    Weekly(
                        get(1).toInt(),
                        get(2).split(',').map { DayOfWeek.valueOf(it) }.toSet(),
                        Time.parse(get(3))
                    )
                }
            }
        }
    }

    data class Monthly(
        override val count: Int,
        val dayOfMonth: DayOfMonth,
        override val time: Time
    ) : Frequency() {
        override fun toString(): String = "M;$count;$dayOfMonth;$time"
        companion object {
            fun parse(s: String): Monthly {
                require(s[0] == 'M') { "Invalid format for Monthly: $s" }
                return with(s.split(';')) {
                    Monthly(
                        get(1).toInt(),
                        DayOfMonth.parse(get(2)),
                        Time.parse(get(3))
                    )
                }
            }
        }
    }

    data class Yearly(override val count: Int, val dayOfYear: MonthDay, override val time: Time) : Frequency() {
        override fun toString(): String = "Y;$count;%02d-%02d;$time".format(dayOfYear.monthValue, dayOfYear.dayOfMonth)
        companion object {
            fun parse(s: String): Yearly {
                require(s[0] == 'Y') { "Invalid format for Yearly: $s" }
                return with(s.split(';')) {
                    Yearly(
                        get(1).toInt(),
                        with(get(2).split("-")) {
                            MonthDay.of(get(0).toInt(), get(1).toInt())
                        },
                        Time.parse(get(3))
                    )
                }
            }
        }
    }

    fun instant(now: Instant): Instant = Instant.parse(now.toString().split("T")[0] + "T" + time.toString() + "Z")

    companion object {
        fun parse(s: String): Frequency = when (s[0]) {
            'D' -> Daily.parse(s)
            'W' -> Weekly.parse(s)
            'M' -> Monthly.parse(s)
            'Y' -> Yearly.parse(s)
            else -> throw IllegalArgumentException("Invalid frequency format: $s")
        }
    }
}

data class Time(val hours: Int, val minutes: Int, val seconds: Int) {
    override fun toString(): String {
        val s = StringBuilder()
        if (hours < 10) {
            s.append("0")
        }
        s.append(hours)
        s.append(":")
        if (minutes < 10) {
            s.append("0")
        }
        s.append(minutes)
        s.append(":")
        if (seconds < 10) {
            s.append("0")
        }
        s.append(seconds)
        return s.toString()
    }

    companion object {
        fun parse(s: String): Time {
            require(s.length < 9) { "Invalid time format: $s. Time should be formatted as HH:mm:ss" }
            require(s[2] == ':') { "Invalid time format: $s. Time should be formatted as HH:mm:ss" }
            require(s[5] == ':') { "Invalid time format: $s. Time should be formatted as HH:mm:ss" }
            return Time(
                s.substring(0, 2).toInt(),
                s.substring(3, 5).toInt(),
                s.substring(7).toInt(),
            )
        }
    }
}

class DayOfMonth private constructor(
    val day: Int? = null,
    val positionalDayOfWeek: PositionalDayOfWeek? = null
) {
    override fun toString() = day?.let { "DAY-${it}" } ?: positionalDayOfWeek!!.toString()

    companion object {
        fun day(day: Int): DayOfMonth {
            require(day in 1..31) { "Day out of range: $day" }
            return DayOfMonth(day = day)
        }

        fun positionalDayOfWeek(position: Position, dayOfWeek: DayOfWeek): DayOfMonth {
            return DayOfMonth(positionalDayOfWeek = PositionalDayOfWeek(position, dayOfWeek))
        }

        fun parse(s: String): DayOfMonth = with(s.split("-")) {
            when (size) {
                2 -> when (first()) {
                    "DAY" -> day(get(1).toInt())
                    else -> positionalDayOfWeek(
                        Position.valueOf(first()),
                        DayOfWeek.valueOf(get(1))
                    )
                }
                else -> throw IllegalArgumentException("Failed to parse string $s")
            }
        }
    }

    data class PositionalDayOfWeek(val position: Position, val dayOfWeek: DayOfWeek) {
        override fun toString(): String = "${position.name}-${dayOfWeek.name}"
    }
}

enum class Position {
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    LAST
}
