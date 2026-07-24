package com.example.thermalmonitor.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thermalmonitor.R
import com.example.thermalmonitor.model.AlarmType
import com.example.thermalmonitor.service.AlarmService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AlarmFragment : Fragment() {
    
    private lateinit var alarmService: AlarmService
    private lateinit var alarmTypeDropdown: AutoCompleteTextView
    private lateinit var thresholdInput: TextInputEditText
    private lateinit var setAlarmButton: MaterialButton
    
    private var selectedAlarmType = AlarmType.BOTH
    private val alarmOptions = arrayOf("CPU", "Battery", "Both")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        alarmService = AlarmService(requireContext())
        
        alarmTypeDropdown = view.findViewById(R.id.alarm_type_dropdown)
        thresholdInput = view.findViewById(R.id.threshold_input)
        setAlarmButton = view.findViewById(R.id.set_alarm_button)
        
        setupDropdown()
        
        // Initial default threshold if empty, but don't overwrite if user typed something
        if (thresholdInput.text.isNullOrEmpty()) {
            thresholdInput.setText("60.0")
        }
        
        setAlarmButton.setOnClickListener {
            setAlert()
        }
    }
    
    private fun setupDropdown() {
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_small, alarmOptions)
        alarmTypeDropdown.setAdapter(adapter)
        
        // Default selection is "Both" (index 2)
        alarmTypeDropdown.setText(alarmOptions[2], false)
        selectedAlarmType = AlarmType.BOTH

        alarmTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedAlarmType = when (position) {
                0 -> AlarmType.CPU
                1 -> AlarmType.BATTERY
                else -> AlarmType.BOTH
            }
            // Logic to NOT reset thresholdInput is already handled by simply not calling any update here
        }
    }

    private fun setAlert() {
        val thresholdText = thresholdInput.text.toString()
        
        if (thresholdText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a temperature threshold", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val threshold = thresholdText.toFloat()
            
            if (threshold < 0 || threshold > 100) {
                Toast.makeText(requireContext(), "Temperature must be between 0-100°C", Toast.LENGTH_SHORT).show()
                return
            }
            
            alarmService.setAlarm(selectedAlarmType, threshold)
            Toast.makeText(
                requireContext(),
                "Alert set for ${selectedAlarmType.name} at ${String.format("%.1f", threshold)}°C",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Please enter a valid temperature", Toast.LENGTH_SHORT).show()
        }
    }
}
