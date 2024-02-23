package com.wbrawner.twigs

import com.wbrawner.twigs.model.Frequency
import java.time.Instant
import java.util.*

private val CALENDAR_FIELDS = intArrayOf(
    Calendar.MILLISECOND,
    Calendar.SECOND,
    Calendar.MINUTE,
    Calendar.HOUR_OF_DAY,
    Calendar.DATE
)

val firstOfMonth: Instant
    get() = GregorianCalendar(TimeZone.getTimeZone("UTC")).run {
        for (calField in CALENDAR_FIELDS) {
            set(calField, getActualMinimum(calField))
        }
        toInstant()
    }

val endOfMonth: Instant
    get() = GregorianCalendar(TimeZone.getTimeZone("UTC")).run {
        for (calField in CALENDAR_FIELDS) {
            set(calField, getActualMaximum(calField))
        }
        toInstant()
    }

val twoWeeksFromNow: Instant
    get() = GregorianCalendar(TimeZone.getTimeZone("UTC")).run {
        add(Calendar.DATE, 14)
        toInstant()
    }

val tomorrow: Instant
    get() = GregorianCalendar(TimeZone.getTimeZone("UTC")).run {
        add(Calendar.DATE, 1)
        toInstant()
    }

private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

fun randomString(length: Int = 32): String {
    val id = StringBuilder()
    for (i in 0 until length) {
        id.append(CHARACTERS.random())
    }
    return id.toString()
}

fun String.toInstant(): Instant = Instant.parse(this)

fun String.asFrequency(): Frequency = Frequency.parse(this)
