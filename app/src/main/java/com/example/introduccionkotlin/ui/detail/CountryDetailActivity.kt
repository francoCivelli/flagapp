package com.example.introduccionkotlin.ui.detail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.ActivityCountryDetailBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.home.ListViewModel
import com.example.introduccionkotlin.util.SerializableCountryFactory
import com.example.introduccionkotlin.util.getProgressDrawable
import com.example.introduccionkotlin.util.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CountryDetailActivity : AppCompatActivity(){

    private lateinit var binding : ActivityCountryDetailBinding
    private val viewModel: ListViewModel by viewModels()
    private var countryName: String = ""
    private var country: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        countryName = intent.getStringExtra(COUNTRY_NAME).toString()
        if(intent.getSerializableExtra(COUNTRY) != null)
            country = intent.getSerializableExtra(COUNTRY) as Country

        supportActionBar?.title = resources.getString(R.string.detail)
        setUpObservers()
        if(country != null)
            setUpView(country)
        else
            viewModel.getByName(countryName?:"")
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

                btnSave.visibility = if(country.id.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                btnSave.setOnClickListener {
                    country.descripcion = textDescription.text.toString()
                    viewModel.update(country)
                }
            }
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
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
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }
            is ListViewModel.CountryUIState.Update -> {
                setResult(RESULT_CANCELED)
                finish()
            }
            is ListViewModel.CountryUIState.Error -> {
                setResult(RESULT_CANCELED)
                finish()
            }
            else -> Unit
        }
        viewModel.clearState()
    }

    companion object{
        const val COUNTRY = "country"
        const val COUNTRY_NAME = "countryName"
    }
}