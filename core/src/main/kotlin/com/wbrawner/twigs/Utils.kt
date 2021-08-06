package com.wbrawner.twigs

import java.util.*

private val CALENDAR_FIELDS = intArrayOf(
        Calendar.MILLISECOND,
        Calendar.SECOND,
        Calendar.MINUTE,
        Calendar.HOUR_OF_DAY,
        Calendar.DATE
)

val firstOfMonth: Date
    get() = GregorianCalendar().run {
        for (calField in CALENDAR_FIELDS) {
            set(calField, getActualMinimum(calField))
        }
        time
    }

val endOfMonth: Date
    get() = GregorianCalendar().run {
        for (calField in CALENDAR_FIELDS) {
            set(calField, getActualMaximum(calField))
        }
        time
    }

val twoWeeksFromNow: Date
    get() = GregorianCalendar().run {
        add(Calendar.DATE, 14)
        time
    }

private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

fun randomString(length: Int = 32): String {
    val id = StringBuilder()
    for (i in 0 until length) {
        id.append(CHARACTERS.random())
    }
    return id.toString()
}

// TODO: Use bcrypt to hash strings
fun String.hash(): String = this
