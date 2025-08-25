package com.example.gencidevtest.presentation.common.converter

import java.text.NumberFormat
import java.util.*

object PriceConverter {
    fun format(price: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.GERMANY)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(price)
    }
}
