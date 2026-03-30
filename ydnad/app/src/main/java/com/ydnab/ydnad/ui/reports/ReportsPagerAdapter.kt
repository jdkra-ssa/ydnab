package com.ydnab.ydnad.ui.reports

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReportsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MonthlyFragment()
        1 -> CategoryFragment()
        2 -> ChartsFragment()
        else -> MonthlyFragment()
    }
}
