package com.example.introduccionkotlin.ui.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.introduccionkotlin.databinding.FragmentCountryDetailBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.detail.CountryDetailActivity.Companion.COUNTRY
import com.example.introduccionkotlin.ui.detail.CountryDetailActivity.Companion.COUNTRY_NAME
import com.example.introduccionkotlin.ui.home.ListViewModel
import com.example.introduccionkotlin.util.getProgressDrawable
import com.example.introduccionkotlin.util.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CountryDetailFragment : Fragment() {

    private lateinit var binding: FragmentCountryDetailBinding
    private var countryName : String = ""
    private val viewModel: ListViewModel by viewModels()
    private lateinit var listener : OnDetailCountryFragmentListener
    private var country : Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            countryName = it.getString(COUNTRY_NAME).toString()
            if(it.getSerializable(COUNTRY) != null)
                country = it.getSerializable(COUNTRY) as Country
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentCountryDetailBinding.inflate(inflater)

        setUpObservers()
        if(country != null)
            setUpView(country)
        else
            viewModel.getByName(countryName?:"")

        return binding.root
    }

    private fun setUpView (country: Country?) {
        if(country != null) {
            val progressDrawable = getProgressDrawable(binding.root.context)
            with(binding) {
                textCountryName.text = country.countryName
                textCapital.text = country.capital
                textDescription.setText(country.descripcion)
                textPopulation.text = country.population.toString()
                textRegion.text = country.region
                imageCountry.loadImage(country.flag, progressDrawable)

                btnSave.visibility = if(country.id.isNullOrEmpty()) View.GONE else View.VISIBLE
                btnSave.setOnClickListener {
                    country.descripcion = textDescription.text.toString()
                    viewModel.update(country)
                }
                btnMap.setOnClickListener {
                    listener.goMap(country)
                }
            }
            this.country = country
        } else
            listener.goHome()
    }

    private fun setUpObservers () {
        viewModel.apply {
            lifecycleScope.launch {
                countriesUIState.collectLatest { updateCountries(it) }
            }
        }
    }

    private fun updateCountries(model: ListViewModel.CountryUIState){
        when (model) {
            is ListViewModel.CountryUIState.GetCountry -> {
                model.country.let {
                    if(it != null) {
                        setUpView(it)
                    } else {
                        listener.goHome()
                    }
                }
            }
            is ListViewModel.CountryUIState.Update -> {
                listener.goHome()
            }
            is ListViewModel.CountryUIState.Error -> {
                listener.goHome()
            }
            else -> Unit
        }
        viewModel.clearState()
    }

    interface OnDetailCountryFragmentListener {
        fun goMap(country: Country)
        fun goHome()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as OnDetailCountryFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString()
                    + " must implement OnDetailCountryFragmentListener")
        }
    }

}