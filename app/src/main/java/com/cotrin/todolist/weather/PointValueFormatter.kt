package com.cotrin.todolist.weather

import com.github.mikephil.charting.formatter.ValueFormatter

class PointValueFormatter: ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${value.toInt()}°"
    }
}