package com.example.introduccionkotlin.ui.scheduled

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.adapters.CountryListAdapter
import com.example.introduccionkotlin.databinding.FragmentCountriesListSchedulesBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.home.ListViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CountriesScheduledFragment : Fragment(), CountryListAdapter.OnCountryListener {

    private lateinit var binding: FragmentCountriesListSchedulesBinding
    private lateinit var mListener: OnScheduledFragmentListener
    private val countriesAdapter = CountryListAdapter(arrayListOf(), false, this)
    private val viewModel: ListViewModel by viewModels()
    private var deleted: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentCountriesListSchedulesBinding.inflate(inflater)

        binding.apply {
            countriesList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = countriesAdapter
            }
        }

        setUpObservers()

        return binding.root
    }

    private fun setUpObservers() {
        viewModel.apply {
            fetchDatabaseCountries()
            lifecycleScope.launch {
                countriesUIState.collectLatest { updateCountries(it) }
            }
        }
    }

    private fun updateCountries(model: ListViewModel.CountryUIState){
        when (model) {
            is ListViewModel.CountryUIState.Success -> {
                model.countries?.let { countries ->
                    binding.countriesList.visibility = View.VISIBLE
                    if(countries.isNullOrEmpty()){
                        binding.listError.text = resources.getString(R.string.empty_list)
                        binding.listError.visibility = View.VISIBLE
                    } else
                        binding.listError.visibility = View.GONE
                    countriesAdapter.updateCountries(countries)
                }
                binding.loadingView.visibility = View.GONE
            }
            is ListViewModel.CountryUIState.Delete -> {
                viewModel.fetchDatabaseCountries()
                deleted = null
            }
            is ListViewModel.CountryUIState.Error -> {
                if(deleted != null){
                    Snackbar.make(binding.root, resources.getString(R.string.country_remove_error), Snackbar.LENGTH_SHORT).show()
                } else {
                    with(binding){
                        loadingView.visibility = View.GONE
                        listError.text = resources.getString(R.string.an_ocurred_while_loading_data)
                        listError.visibility = View.VISIBLE
                    }
                }
            }
            is ListViewModel.CountryUIState.Loading -> {
                with(binding) {
                    loadingView.visibility = View.VISIBLE
                    listError.visibility = View.GONE
                    countriesList.visibility = View.GONE
                }
            }
            else -> Unit
        }
        viewModel.clearState()
    }

    override fun countrySelected(country: Country) {
        // Abrir el fragment de detail de Country
        mListener.goDetail(country.countryName?:"")
    }

    override fun addCountry(country: Country) {}

    override fun removeCountry(country: Country) {
        deleted = country
        viewModel.removeCountry(country)
    }

    interface OnScheduledFragmentListener {
        fun goDetail(countryName: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as OnScheduledFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString()
                    + " must implement OnHomeFragmentListener")
        }
    }

}