package com.example.gravitfit

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.*
import com.github.mikephil.charting.components.Legend
import com.example.gravitfit.JumpsActivity
import android.content.Intent
import com.example.gravitfit.ui.home.HomeFragment
import androidx.fragment.app.activityViewModels



class OpenPoseFragment : Fragment() {


    companion object {
        fun newInstance() = OpenPoseFragment()
    }
    // Get the ViewModel scoped to the Activity
    private val openPoseViewModel: OpenPoseViewModel by activityViewModels()

    private lateinit var lineChart: LineChart
    private var animationJob: Job? = null

//    private var athlete = athleteId
//    private var exercise = jumpType

    // Define colors for different lines (add more if needed)
    private val lineColors = listOf(
        Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_open_pose, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        setupChart()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selection = openPoseViewModel.currentSelection.value // For logging or title

        openPoseViewModel.velocityData.observe(viewLifecycleOwner, Observer { velocityData ->
            if (velocityData != null) {
                Log.d("OpenPoseFragment", "Velocity data updated/loaded for ${selection?.athleteId}_${selection?.jumpType}. dt: ${velocityData.dt}")
                // Stop any existing animation
                animationJob?.cancel()
                animateChart(velocityData)
            } else {
                Log.e("OpenPoseFragment", "Failed to load velocity data.")
            }
        })

    }
    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)


        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.granularity = 1f // Adjust as needed, represents time steps or seconds

        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        // You might want to set axis min/max based on your Python plot's y-limits
        // leftAxis.axisMinimum = -10f; leftAxis.axisMaximum = 10f;

        lineChart.axisRight.isEnabled = false


        // --- LEGEND CUSTOMIZATION ---
        val legend = lineChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.yOffset = 10f
        legend.xOffset = 0f
        legend.yEntrySpace = 0f // Space between legend entries vertically (if vertical)
        legend.xEntrySpace = 10f // Increase horizontal space between entries
        legend.textSize = 12f    // Increase text size (default is often 10f)
        legend.formSize = 10f    // Increase the size of the legend form (the colored square/line)
        legend.formToTextSpace = 8f // Increase space between form and text
        legend.isWordWrapEnabled = true // Good if names are long
        // legend.form = Legend.LegendForm.LINE // Optional: set legend form


    }
    private fun animateChart(velocityData: VelocityData) {
        val dt = velocityData.dt
        val jointVelocities = velocityData.jointVelocities

        // Select a few joints to plot for simplicity or plot all
        val jointsToPlot = jointVelocities.keys.toList() // .take(3) to plot first 3

        if (jointsToPlot.isEmpty() || jointVelocities[jointsToPlot[0]].isNullOrEmpty()) {
            Log.e("OpenPoseFragment", "No data points to animate.")
            return
        }
        val dataSets = ArrayList<ILineDataSet>()
        val maxFrames = jointVelocities[jointsToPlot[0]]?.size ?: 0

        // Initialize empty datasets
        jointsToPlot.forEachIndexed { index, jointName ->
            val entries = ArrayList<Entry>()
            val dataSet = LineDataSet(entries, jointName)
            val color = lineColors.getOrElse(index) { Color.BLACK } // Cycle through colors
            dataSet.color = color
            // dataSet.setCircleColor(color)
            dataSet.lineWidth = 1.5f
            // dataSet.circleRadius = 0.5f
            dataSet.setDrawValues(false)
            dataSets.add(dataSet)
            dataSet.setDrawCircles(false)
        }
        lineChart.data = LineData(dataSets)
        lineChart.invalidate()


        animationJob = CoroutineScope(Dispatchers.Main).launch {
            for (frameIndex in 0 until maxFrames) {
                val time = (frameIndex * dt).toFloat()

                jointsToPlot.forEachIndexed { jointIndex, jointName ->
                    val velocity = jointVelocities[jointName]?.getOrNull(frameIndex)
                    // The 'isVisible' check here is often NOT needed because MPAndroidChart
                    // handles it. However, if you were doing complex dataset recreation, it might be.
                    // For addEntry, it's usually fine.
                    val dataSet = lineChart.data.getDataSetByIndex(jointIndex) as LineDataSet

                    // if (dataSet.isVisible && velocity != null) { // You could add this check if needed
                    if (velocity != null) { // Current approach is usually sufficient
                        try {
                            dataSet.addEntry(Entry(time, velocity.toFloat()))
                        } catch (e: Exception) {
                            Log.e("OpenPoseFragment", "Error adding entry: $e")
                        }
                    }
                }

                if (lineChart.data != null) {
                    lineChart.data.notifyDataChanged()
                }
                lineChart.notifyDataSetChanged()
                // ... (setVisibleXRangeMaximum, moveViewToX) ...
                lineChart.invalidate()

                delay(5) // Adjust animation speed (e.g., roughly 30fps animation)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Cancel the animation job if the fragment is stopped to prevent leaks
        animationJob?.cancel()
    }






}