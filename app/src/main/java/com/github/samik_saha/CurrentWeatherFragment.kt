package com.github.samik_saha

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.samik_saha.databinding.CurrentWeatherFragmentBinding
import java.util.*

const val TAG_WEATHER_FRAGMENT="WeatherFragment"

class WeatherFragment : Fragment() {

    companion object {
        fun newInstance() = WeatherFragment()
    }

    /**
     * Lazily initialize our [WeatherViewModel].
     */
    private val viewModel: WeatherViewModel by lazy {
        ViewModelProvider(this,
            WeatherViewModelFactory(UserPreferencesRepository.getInstance(this.requireContext()),
            Geocoder(this.requireContext(), Locale.getDefault())
            ))
            .get(WeatherViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG_WEATHER_FRAGMENT, "onCreateView called")
        val binding = CurrentWeatherFragmentBinding.inflate(inflater)
        binding.viewModel=viewModel
        binding.lifecycleOwner=this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG_WEATHER_FRAGMENT,"onActivityCreated called")
        //viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        // TODO: Use the ViewModel
    }

}