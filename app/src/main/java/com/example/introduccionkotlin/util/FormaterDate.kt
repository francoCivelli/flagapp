package com.example.introduccionkotlin.util

import java.text.SimpleDateFormat
import java.util.*

object FormaterDate {
    fun getFechaActual(): String? {
        val date = Date()
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return simpleDateFormat.format(date)
    }

    fun formatDigitDate(digit: Int): String? {
        return if (digit < 10) "0$digit" else digit.toString()
    }
}