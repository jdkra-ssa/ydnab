package com.ydnab.ydnad.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ydnab.ydnad.databinding.FragmentChartsBinding
import kotlinx.coroutines.launch

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by activityViewModels()

    private val chartColors = listOf(
        Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), Color.rgb(217, 184, 162),
        Color.rgb(191, 134, 134), Color.rgb(179, 48, 80), Color.rgb(100, 149, 237),
        Color.rgb(255, 165, 0), Color.rgb(34, 139, 34), Color.rgb(148, 0, 211)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBarChart()
        setupPieChart()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthlyTotals.collect { totals ->
                if (totals.isNotEmpty()) {
                    val entries = totals.reversed().mapIndexed { i, mt ->
                        BarEntry(i.toFloat(), mt.total.toFloat())
                    }
                    val labels = totals.reversed().map { it.month }
                    val dataSet = BarDataSet(entries, "Monthly Spending").apply {
                        color = Color.rgb(64, 89, 128)
                        valueTextSize = 10f
                    }
                    binding.barChart.data = BarData(dataSet)
                    binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    binding.barChart.xAxis.labelCount = labels.size
                    binding.barChart.invalidate()
                    binding.barChart.visibility = View.VISIBLE
                } else {
                    binding.barChart.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryTotals.collect { totals ->
                if (totals.isNotEmpty()) {
                    val entries = totals.mapIndexed { i, ct ->
                        PieEntry(ct.total.toFloat(), ct.category)
                    }
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = chartColors.take(entries.size).ifEmpty { listOf(Color.GRAY) }
                        valueTextSize = 12f
                        valueTextColor = Color.WHITE
                    }
                    binding.pieChart.data = PieData(dataSet)
                    binding.pieChart.invalidate()
                    binding.pieChart.visibility = View.VISIBLE
                } else {
                    binding.pieChart.visibility = View.GONE
                }
            }
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setFitBars(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            legend.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
