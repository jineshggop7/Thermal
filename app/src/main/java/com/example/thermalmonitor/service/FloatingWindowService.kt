package com.example.thermalmonitor.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.thermalmonitor.R
import com.example.thermalmonitor.utils.TemperatureUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FloatingWindowService : Service() {
    
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private var lastX = 0f
    private var lastY = 0f
    private var initialX = 0
    private var initialY = 0
    
    override fun onBind(intent: Intent): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
        startTemperatureUpdates()
    }
    
    private fun createFloatingView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window_layout, null)
        
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        params = WindowManager.LayoutParams(
            400,
            200,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = 0
            y = 100
        }
        
        windowManager.addView(floatingView, params)
        
        floatingView?.setOnTouchListener { v, event ->
            handleTouchEvent(v, event)
        }
    }
    
    private fun handleTouchEvent(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                initialX = params?.x ?: 0
                initialY = params?.y ?: 0
                params?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = (event.rawX - lastX).toInt()
                val deltaY = (event.rawY - lastY).toInt()
                params?.x = initialX + deltaX
                params?.y = initialY + deltaY
                if (params != null) {
                    windowManager.updateViewLayout(v, params)
                }
            }
            MotionEvent.ACTION_UP -> {
                params?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
        }
        return false
    }
    
    private fun startTemperatureUpdates() {
        scope.launch {
            while (true) {
                try {
                    val cpuTemp = TemperatureUtils.readCpuTemperature()
                    val batteryTemp = TemperatureUtils.readBatteryTemperature(this@FloatingWindowService)
                    
                    updateFloatingWindow(cpuTemp, batteryTemp)
                    delay(2000) // Update every 2 seconds
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun updateFloatingWindow(cpuTemp: Float, batteryTemp: Float) {
        floatingView?.let { view ->
            val cpuTempView = view.findViewById<TextView>(R.id.floating_cpu_temp)
            val batteryTempView = view.findViewById<TextView>(R.id.floating_battery_temp)
            val container = view.findViewById<View>(R.id.floating_container)
            
            cpuTempView.text = "CPU: ${TemperatureUtils.formatTemperature(cpuTemp)}"
            batteryTempView.text = "Battery: ${TemperatureUtils.formatTemperature(batteryTemp)}"
            
            // Apply gradient color based on maximum temperature
            val maxTemp = maxOf(cpuTemp, batteryTemp)
            val gradientColor = TemperatureUtils.getGradientColor(maxTemp)
            container.setBackgroundColor(gradientColor)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager.removeView(floatingView)
        }
        serviceJob.cancel()
    }
}
