package com.example.thermalmonitor.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.thermalmonitor.MainActivity
import com.example.thermalmonitor.ui.fragment.HomeFragment
import com.example.thermalmonitor.ui.fragment.AlarmFragment
import com.example.thermalmonitor.ui.fragment.ActiveAlertsFragment

class TabsPagerAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> AlarmFragment()
            2 -> ActiveAlertsFragment()
            else -> HomeFragment()
        }
    }
}
