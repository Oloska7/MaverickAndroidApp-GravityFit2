package com.example.gravitfit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class EmgAnalysisFragment : Fragment() {

    private lateinit var textCa: TextView
    private lateinit var textGl: TextView
    private lateinit var textVm: TextView
    private lateinit var textVl: TextView

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emg_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textCa = view.findViewById(R.id.textCa)
        textGl = view.findViewById(R.id.textGl)
        textVm = view.findViewById(R.id.textVm)
        textVl = view.findViewById(R.id.textVl)

        viewModel.selectedAthleteId.observe(viewLifecycleOwner) { id ->
            id?.let {
                val mvc = MvcRepository.getMvcData(it)
                mvc?.let { data ->
                    updateTextViews(data)
                }
            }
        }
    }

    private fun updateTextViews(data: MvcData) {
        textCa.text = "CA: ${data.ca} mV"
        textGl.text = "GL: ${data.gl} mV"
        textVm.text = "VM: ${data.vm} mV"
        textVl.text = "VL: ${data.vl} mV"
    }
}
