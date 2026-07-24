package com.example.thermalmonitor.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thermalmonitor.R
import com.example.thermalmonitor.database.TemperatureDatabase
import com.example.thermalmonitor.model.AlarmSettings
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ActiveAlertsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noAlertsText: TextView
    private lateinit var database: TemperatureDatabase
    private lateinit var adapter: AlertsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_active_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = TemperatureDatabase.getDatabase(requireContext())
        
        recyclerView = view.findViewById(R.id.alerts_recycler_view)
        noAlertsText = view.findViewById(R.id.no_alerts_text)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AlertsAdapter { alert -> deleteAlert(alert) }
        recyclerView.adapter = adapter

        observeAlerts()
    }

    private fun observeAlerts() {
        viewLifecycleOwner.lifecycleScope.launch {
            database.temperatureDao().getAllAlerts().collect { alerts ->
                if (alerts.isEmpty()) {
                    noAlertsText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    noAlertsText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(alerts)
                }
            }
        }
    }

    private fun deleteAlert(alert: AlarmSettings) {
        viewLifecycleOwner.lifecycleScope.launch {
            database.temperatureDao().deleteAlert(alert)
        }
    }

    private class AlertsAdapter(private val onDelete: (AlarmSettings) -> Unit) : 
        RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {
        
        private var alerts = listOf<AlarmSettings>()

        fun submitList(newList: List<AlarmSettings>) {
            alerts = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val alert = alerts[position]
            holder.typeText.text = "Target: ${alert.alarmType}"
            holder.thresholdText.text = "Threshold: ${alert.threshold}°C"
            holder.deleteButton.setOnClickListener { onDelete(alert) }
        }

        override fun getItemCount() = alerts.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val typeText: TextView = view.findViewById(R.id.alert_type_text)
            val thresholdText: TextView = view.findViewById(R.id.alert_threshold_text)
            val deleteButton: MaterialButton = view.findViewById(R.id.delete_alert_button)
        }
    }
}
