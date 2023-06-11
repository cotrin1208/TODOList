package com.cotrin.todolist.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cotrin.todolist.R
import com.cotrin.todolist.databinding.FragmentWeatherBinding
import com.cotrin.todolist.utils.getDayOfWeekText
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.time.LocalDate

class WeatherFragment : Fragment() {
    private lateinit var binding: FragmentWeatherBinding
    private val weatherViewModel by lazy {
        ViewModelProvider(requireActivity())[WeatherViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        //DataBinding処理
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_weather, container, false)
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = weatherViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //位置情報を更新
        weatherViewModel.updateLocation()

        //天気情報が更新されたらViewに反映する
        weatherViewModel.weatherLiveData.observe(requireActivity()) {
            //nullなら早期リターン
            it ?: return@observe
            //都市名
            weatherViewModel.cityName.value = it.city.name
            //グラフの作成
            val dataSet = LineDataSet(weatherViewModel.getEntries(), "weather").apply {
                //データセットの属性設定
                mode = LineDataSet.Mode.CUBIC_BEZIER
                valueFormatter = PointValueFormatter()
                valueTextSize = 10f
                valueTextColor = ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text)
            }
            val lineData = LineData(dataSet)
            binding.weatherChart.apply {
                //データをセット
                data = lineData
                //一画面の情報を8個に指定
                setVisibleXRangeMaximum(8f)
                //データが無いときのテキスト設定
                setNoDataText("天気情報をロード中")
                //背景グリッドを設定
                setDrawGridBackground(false)
                //破線をリセット
                xAxis.removeAllLimitLines()
                //日付をまたぐ箇所に破線を表示
                for (i in 1 .. 5) {
                    val limitLineText = LocalDate.now().plusDays(i.toLong()).getDayOfWeekText()
                    val limitLineIndex = (24 * i - weatherViewModel.getStartTime()) / 3f
                    val limitLine = LimitLine(limitLineIndex, limitLineText).apply {
                        lineColor = ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text)
                        textColor = ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text)
                        lineWidth = 0.5f
                        enableDashedLine(10f, 10f, 0f)
                    }
                    xAxis.addLimitLine(limitLine)
                }
                //ハイライトを非表示
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false
                //X軸を下に設定
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                //X軸の罫線を非表示
                xAxis.setDrawGridLines(false)
                //X軸のラベルを設定
                xAxis.valueFormatter = XAxisValueFormatter(weatherViewModel.getStartTime(), weatherViewModel.getPops(), weatherViewModel.getWeatherIcons())
                //X軸のラベルレンダーを設定
                val renderer = WeatherXAxisRenderer(viewPortHandler, xAxis, getTransformer(YAxis.AxisDependency.LEFT), requireContext())
                setXAxisRenderer(renderer)
                //Y軸を不可視に設定
                axisRight.isEnabled = false
                axisLeft.isEnabled = false
                //Y軸に余白を追加
                axisLeft.axisMaximum += 5
                axisLeft.axisMinimum -= 12
                //詳細を非表示
                description.isEnabled = false
                //凡例を非表示
                legend.isEnabled = false
                //ズームを禁止する
                setScaleEnabled(false)
                setPinchZoom(false)
                //色を設定
                xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.weatherIconColor)
                axisRight.textColor = ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text)
                axisLeft.textColor = ContextCompat.getColor(requireContext(), android.R.color.tab_indicator_text)
                //再描画
                invalidate()
                //アニメーション
                animateY(1500, Easing.EaseOutSine)
            }
        }
    }
}