package com.cotrin.todolist.weather

import com.github.mikephil.charting.formatter.ValueFormatter

class XAxisValueFormatter(
    private val startTime: Int,
    private val pops: List<String>,
    private val icons: List<Int>
    ): ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if (value.toInt() < 0) return ""
        for (i in 5 downTo 1) {
            if (startTime + 3 * value.toInt() >= 24 * i) {
                return "${startTime + 3 * value.toInt() - 24 * i}時\n${pops[value.toInt()]}\n${icons[value.toInt()]}"
            }
        }
        return "${startTime + 3 * value.toInt()}時\n${pops[value.toInt()]}\n${icons[value.toInt()]}"
    }
}