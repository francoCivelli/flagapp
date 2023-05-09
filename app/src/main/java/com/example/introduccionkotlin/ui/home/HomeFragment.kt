package com.example.introduccionkotlin.ui.home

import android.content.Context
import android.inputmethodservice.Keyboard
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.adapters.CountryListAdapter
import com.example.introduccionkotlin.databinding.FragmentHomeBinding
import com.example.introduccionkotlin.model.Country
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_card_buscador.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), CountryListAdapter.OnCountryListener {

    private val viewModel: ListViewModel by activityViewModels()
    private val countriesAdapter = CountryListAdapter(arrayListOf(), true, this)
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mListener: OnHomeFragmentListener
    private var countrySelected: Country? = null
    private var countries: ArrayList<Country> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater)

        binding.apply {
            countriesList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = countriesAdapter
            }

            homeFragment.setOnRefreshListener {
                homeFragment.isRefreshing = false
                viewModel.refresh()
            }
        }

        setUpObservers()
        setUpSearchView()

        return binding.root
    }

    private fun setUpObservers () {
        viewModel.apply {
            refresh()
            lifecycleScope.launch {
                countriesUIState.collectLatest { updateCountries(it) }
            }
        }
    }

    private fun setUpSearchView () {
        with(binding){
            layoutCardBuscador.search_edit_text.addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    checkEditText(s.toString())
                }
            })
            layoutCardBuscador.image_search_text.setOnClickListener {
                cleanSearchText()
            }
        }
    }

    private fun cleanSearchText () {
        with(binding){
            if(layoutCardBuscador.search_edit_text.text.toString().isNotEmpty()){
                layoutCardBuscador.search_edit_text.text.clear()
                mostrarListado(layoutCardBuscador.search_edit_text.text.toString(), true)
            }
            layoutCardBuscador.image_search_text.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_search_company, null))
        }
    }

    private fun checkEditText (s: String) {
        var texto = s
        with(binding){
            if(texto.isNotEmpty() && texto.substring(s.length - 1) == "\n"){
                texto = texto.substring(0, s.length - 1)
                mostrarListado(texto, true)
                layoutCardBuscador.search_edit_text.setText(texto)
                layoutCardBuscador.search_edit_text.setSelection(layoutCardBuscador.search_edit_text.text.toString().length)
            } else if (texto.isEmpty()){
                mostrarListado(texto, false)
                layoutCardBuscador.search_edit_text.setSelection(layoutCardBuscador.search_edit_text.text.toString().length)
            } else {
                mostrarListado(texto, true)
            }
            layoutCardBuscador.image_search_text.setImageDrawable(
                if(texto.isEmpty()) ResourcesCompat.getDrawable(resources, R.drawable.ic_search_company, null)
                else ResourcesCompat.getDrawable(resources, R.drawable.ic_clean_calendar, null))
        }
    }

    private fun mostrarListado ( texto: String,  onClick: Boolean) {
        if (onClick && texto.isNotEmpty()) {
            val countrieslist = countries.filter { country -> country.countryName!!.contains(texto) }
            countriesAdapter.updateCountries(countrieslist)
        } else if (texto.isEmpty()) {
            countriesAdapter.updateCountries(countries)
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
                    this.countries.addAll(countries)
                    countriesAdapter.updateCountries(countries)
                }
                binding.loadingView.visibility = View.GONE
            }
            is ListViewModel.CountryUIState.Exists -> {
                model.exists.let {
                    if(it) {
                        countrySelected = null
                        Snackbar.make(binding.root, resources.getString(R.string.country_add_exists), Snackbar.LENGTH_SHORT).show()
                    } else
                        viewModel.addCountry(countrySelected!!)
                }

            }
            is ListViewModel.CountryUIState.Add -> {
                countrySelected = null
                Snackbar.make(binding.root, resources.getString(R.string.country_add_success), Snackbar.LENGTH_SHORT).show()
            }
            is ListViewModel.CountryUIState.Error -> {
                if(countrySelected != null){
                    countrySelected = null
                    Snackbar.make(binding.root, resources.getString(R.string.country_add_error), Snackbar.LENGTH_SHORT).show()
                } else {
                    binding.loadingView.visibility = View.GONE
                    binding.listError.text = resources.getString(R.string.an_ocurred_while_loading_data)
                    binding.listError.visibility = View.VISIBLE
                }
            }
            is ListViewModel.CountryUIState.Loading -> {
                binding.apply {
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
        mListener.goDetail(country)
    }

    override fun addCountry(country: Country) {
        // Armo mensaje para el usuario
        countrySelected = country
        viewModel.checkCountryExists(country)
    }

    override fun removeCountry(country: Country) {}

    interface OnHomeFragmentListener {
        fun goDetail(country: Country)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as OnHomeFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString()
                    + " must implement OnHomeFragmentListener")
        }
    }
}