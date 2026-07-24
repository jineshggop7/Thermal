package com.example.thermalmonitor.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.thermalmonitor.R
import com.example.thermalmonitor.database.TemperatureDatabase
import com.example.thermalmonitor.utils.TemperatureUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    
    private lateinit var timeRangeDropdown: AutoCompleteTextView
    private lateinit var lineChart: LineChart
    private lateinit var refreshButton: MaterialButton
    private lateinit var temperatureDatabase: TemperatureDatabase
    
    private var selectedTimeRange = TimeRange.ONE_HOUR
    private var observationJob: Job? = null
    
    private var pulsingJob: Job? = null
    private var pulseAlpha = 255
    private var pulseDirection = -1
    
    private enum class TimeRange(val label: String, val millis: Long) {
        ONE_HOUR("Last 1 Hour", 3600000),
        ONE_DAY("Last 24 Hours", 86400000),
        ONE_WEEK("Last 7 Days", 604800000)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        temperatureDatabase = TemperatureDatabase.getDatabase(requireContext())
        
        timeRangeDropdown = view.findViewById(R.id.time_range_dropdown)
        lineChart = view.findViewById(R.id.line_chart)
        refreshButton = view.findViewById(R.id.refresh_button)
        
        setupDropdown()
        setupChart()
        
        refreshButton.setOnClickListener {
            loadChartData()
        }
        
        loadChartData()
        startPulsingAnimation()
    }
    
    private fun setupDropdown() {
        val labels = TimeRange.values().map { it.label }.toTypedArray()
        // Use a standard simple_list_item_1 or similar if spinner_item_small is causing issues
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels)
        timeRangeDropdown.setAdapter(adapter)
        
        // Force showing all options when clicked, avoiding the "one option" filtering bug
        timeRangeDropdown.setOnClickListener {
            timeRangeDropdown.showDropDown()
        }
        
        timeRangeDropdown.setText(labels[0], false)

        timeRangeDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedLabel = adapter.getItem(position)
            val index = labels.indexOf(selectedLabel)
            if (index != -1) {
                selectedTimeRange = TimeRange.values()[index]
                loadChartData()
            }
        }
    }
    
    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            setNoDataText("Analyzing thermal records...")
            setNoDataTextColor(android.graphics.Color.GRAY)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = android.graphics.Color.GRAY
                valueFormatter = object : ValueFormatter() {
                    private val mFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return mFormat.format(Date(value.toLong()))
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = android.graphics.Color.LTGRAY
                textColor = android.graphics.Color.GRAY
                setLabelCount(5, true)
            }
            
            axisRight.isEnabled = false
            legend.apply {
                form = com.github.mikephil.charting.components.Legend.LegendForm.LINE
                textColor = android.graphics.Color.GRAY
            }
        }
    }
    
    private fun loadChartData() {
        observationJob?.cancel()
        observationJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val startTime = System.currentTimeMillis() - selectedTimeRange.millis
                temperatureDatabase.temperatureDao()
                    .getTemperaturesAfter(startTime)
                    .distinctUntilChanged()
                    .collect { data ->
                        if (isAdded) {
                            updateChart(data)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPulsingAnimation() {
        pulsingJob?.cancel()
        pulsingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(100)
                pulseAlpha += 15 * pulseDirection
                if (pulseAlpha < 100) {
                    pulseAlpha = 100
                    pulseDirection = 1
                } else if (pulseAlpha > 255) {
                    pulseAlpha = 255
                    pulseDirection = -1
                }
                
                lineChart.data?.let { data ->
                    var updated = false
                    for (i in 0 until data.dataSetCount) {
                        val set = data.getDataSetByIndex(i) as LineDataSet
                        if (set.label == "CPU_TIP" || set.label == "BATTERY_TIP") {
                            val baseColor = if (set.label == "CPU_TIP") "#1E88E5" else "#43A047"
                            val color = android.graphics.Color.parseColor(baseColor)
                            set.setCircleColor(android.graphics.Color.argb(pulseAlpha, 
                                android.graphics.Color.red(color), 
                                android.graphics.Color.green(color), 
                                android.graphics.Color.blue(color)))
                            updated = true
                        }
                    }
                    if (updated) {
                        lineChart.invalidate()
                    }
                }
            }
        }
    }
    
    private fun updateChart(data: List<com.example.thermalmonitor.model.TemperatureData>) {
        if (data.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("No records found for this period")
            return
        }

        val cpuEntries = mutableListOf<Entry>()
        val batteryEntries = mutableListOf<Entry>()
        
        val sortedData = data.sortedBy { it.timestamp }
        sortedData.forEach { temp ->
            if (temp.cpuTemperature != TemperatureUtils.TEMP_NOT_AVAILABLE) {
                cpuEntries.add(Entry(temp.timestamp.toFloat(), temp.cpuTemperature))
            }
            if (temp.batteryTemperature != TemperatureUtils.TEMP_NOT_AVAILABLE) {
                batteryEntries.add(Entry(temp.timestamp.toFloat(), temp.batteryTemperature))
            }
        }
        
        val dataSets = mutableListOf<LineDataSet>()
        
        if (cpuEntries.isNotEmpty()) {
            val cpuSet = createLineDataSet(cpuEntries, "CPU", "#1E88E5")
            dataSets.add(cpuSet)
            // Add a single-point dataset for the pulsing tip
            dataSets.add(createLiveTipDataSet(cpuEntries.last(), "CPU_TIP", "#1E88E5"))
        }
        if (batteryEntries.isNotEmpty()) {
            val batSet = createLineDataSet(batteryEntries, "Battery", "#43A047")
            dataSets.add(batSet)
            dataSets.add(createLiveTipDataSet(batteryEntries.last(), "BATTERY_TIP", "#43A047"))
        }
        
        lineChart.data = LineData(dataSets.toList())
        lineChart.invalidate()
    }

    private fun createLineDataSet(entries: List<Entry>, label: String, colorHex: String): LineDataSet {
        return LineDataSet(entries, label).apply {
            color = android.graphics.Color.parseColor(colorHex)
            lineWidth = 2.5f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillAlpha = 35
            fillColor = android.graphics.Color.parseColor(colorHex)
        }
    }

    private fun createLiveTipDataSet(lastEntry: Entry, label: String, colorHex: String): LineDataSet {
        // Use a copy of the last entry to avoid reference issues
        return LineDataSet(listOf(Entry(lastEntry.x, lastEntry.y)), label).apply {
            setDrawCircles(true)
            setCircleColor(android.graphics.Color.parseColor(colorHex))
            setCircleHoleColor(android.graphics.Color.WHITE)
            setDrawCircleHole(true)
            circleRadius = 6f
            circleHoleRadius = 3f
            setDrawValues(false)
            // Important: don't draw the line for this dataset
            lineWidth = 0f
            color = android.graphics.Color.TRANSPARENT
            setForm(com.github.mikephil.charting.components.Legend.LegendForm.NONE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pulsingJob?.cancel()
        observationJob?.cancel()
    }
}
