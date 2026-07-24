package com.example.thermalmonitor

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.thermalmonitor.database.TemperatureDatabase
import com.example.thermalmonitor.service.AlarmNotificationManager
import com.example.thermalmonitor.service.AlarmService
import com.example.thermalmonitor.ui.adapter.TabsPagerAdapter
import com.example.thermalmonitor.utils.TemperatureUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var temperatureDatabase: TemperatureDatabase
    private lateinit var alarmService: AlarmService
    
    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private val tabTitles = arrayOf("Home", "Set Alert", "Active")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        try {
            // Initialize services
            temperatureDatabase = TemperatureDatabase.getDatabase(this)
            alarmService = AlarmService(this)
            
            // Create notification channels
            AlarmNotificationManager.createNotificationChannels(this)
            
            // Setup UI
            viewPager = findViewById(R.id.view_pager)
            tabLayout = findViewById(R.id.tab_layout)
            
            viewPager.adapter = TabsPagerAdapter(this)
            viewPager.offscreenPageLimit = 1
            
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()
            
            // Request notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (this.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        100
                    )
                }
            }
            
            // Start temperature monitoring and alarm checking
            startTemperatureMonitoring()
            
            // Handle intent extras for alarm notifications
            val showAlarm = intent.getBooleanExtra("show_alarm", false)
            if (showAlarm) {
                viewPager.post { viewPager.currentItem = 2 }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Thermal")
            .setMessage("Engineered for purpose.\nNothing more. Nothing less.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun startTemperatureMonitoring() {
        scope.launch {
            while (true) {
                try {
                    val (cpuTemp, batteryTemp) = withContext(Dispatchers.IO) {
                        val cpu = TemperatureUtils.readCpuTemperature()
                        val battery = TemperatureUtils.readBatteryTemperature(this@MainActivity)
                        Pair(cpu, battery)
                    }
                    
                    withContext(Dispatchers.IO) {
                        val temperatureData = com.example.thermalmonitor.model.TemperatureData(
                            cpuTemperature = cpuTemp,
                            batteryTemperature = batteryTemp
                        )
                        temperatureDatabase.temperatureDao().insertTemperature(temperatureData)
                    }
                    
                    alarmService.checkAndTriggerAlarm(cpuTemp, batteryTemp)
                    
                    // Refresh CPU temperature every 10 seconds as requested
                    delay(10000)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Monitoring error", e)
                    delay(10000)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
