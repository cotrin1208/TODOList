package com.cotrin.todolist.weather

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class WeatherXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer,
    private val context: Context
): XAxisRenderer(viewPortHandler, xAxis, trans) {
    override fun drawLabel(
        c: Canvas?,
        formattedLabel: String?,
        x: Float,
        y: Float,
        anchor: MPPointF?,
        angleDegrees: Float
    ) {
        val labels = formattedLabel!!.split("\n")
        super.drawLabel(c, labels[0], x, y, anchor, angleDegrees)
        Utils.drawXAxisValue(c, labels[1], x, y - 45, mAxisLabelPaint, anchor, angleDegrees)
        val drawable = ContextCompat.getDrawable(context, labels[2].toInt())
        Utils.drawImage(c, drawable, x.toInt(), y.toInt() - 70, 48, 48)
    }
}