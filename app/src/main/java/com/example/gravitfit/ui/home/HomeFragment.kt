package com.example.gravitfit.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gravitfit.DatasetAdapter
import com.example.gravitfit.JumpsActivity
import com.example.gravitfit.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ List of athletes with actual athlete IDs (not based on position)
        val athleteList = listOf(
            "Athlete 1" to "p01",
            "Athlete 2" to "p02",
            "Athlete 3" to "p03",
            "Athlete 4" to "p04",
            "Athlete 5" to "p05",
            "Athlete 6" to "p06",
            "Athlete 7" to "p07",
            "Athlete 9" to "p09",
            "Athlete 10" to "p10"
        )

        binding.rvDatasets.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = DatasetAdapter(
                items = athleteList.map { it.first to it.second.removePrefix("p").toInt() }, // Pair(label, intId)
                onClick = { athleteIntId ->
                    val clicked = athleteList.find { it.second.removePrefix("p").toInt() == athleteIntId }
                    val athleteLabel = clicked?.first ?: "Unknown"
                    val athleteCode = clicked?.second ?: "p01"

                    Toast.makeText(
                        requireContext(),
                        "$athleteLabel clicked!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to JumpsActivity with selected athlete ID
                    Intent(requireContext(), JumpsActivity::class.java).also {
                        it.putExtra("ATHLETE_ID", athleteCode)
                        startActivity(it)
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
