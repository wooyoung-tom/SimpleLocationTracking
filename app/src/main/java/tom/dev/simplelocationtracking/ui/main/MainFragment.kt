package tom.dev.simplelocationtracking.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tom.dev.simplelocationtracking.R
import tom.dev.simplelocationtracking.service.SimpleLocationService
import tom.dev.simplelocationtracking.service.SimpleService

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val startServiceButton = view.findViewById<Button>(R.id.btn_start_service)
        val startLocationButton = view.findViewById<Button>(R.id.btn_start_location_service)
        val stopLocationButton = view.findViewById<Button>(R.id.btn_stop_location_service)

        startServiceButton.setOnClickListener {
            Intent(requireContext(), SimpleService::class.java).also {
                requireActivity().startService(it)
            }
        }

        startLocationButton.setOnClickListener {
            Intent(requireContext(), SimpleLocationService::class.java).also {
                requireActivity().startService(it)
            }
        }

        stopLocationButton.setOnClickListener {
            requireActivity().stopService()
        }

    }

}