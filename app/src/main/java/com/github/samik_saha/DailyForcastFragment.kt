package com.github.samik_saha

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.samik_saha.databinding.DailyForcastFragmentBinding
import java.util.*

class DailyForcastFragment : Fragment() {

    companion object {
        fun newInstance() = DailyForcastFragment()
    }

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
        val binding = DailyForcastFragmentBinding.inflate(inflater)
        binding.viewModel=viewModel
        binding.lifecycleOwner=this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}