package com.example.utils

import java.text.DecimalFormat

object CurrencyUtils {
    fun formatAmount(amount: Double, symbol: String = "$"): String {
        val df = DecimalFormat("#,##0.00")
        return "$symbol ${df.format(amount)}"
    }
}
