package com.wbrawner.twigs.web

import io.ktor.http.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
val decimalFormat = DecimalFormat.getNumberInstance(Locale.US).apply {
    with(this as DecimalFormat) {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            currencySymbol = ""
            isGroupingUsed = false
        }
    }
}

fun Parameters.getAmount() = decimalFormat.parse(get("amount"))
    ?.toDouble()
    ?.toBigDecimal()
    ?.times(BigDecimal(100))
    ?.toLong()
    ?: 0L

fun Long?.toDecimalString(): String {
    if (this == null) return ""
    return decimalFormat.format(toBigDecimal().divide(BigDecimal(100), 2, RoundingMode.HALF_UP))
}