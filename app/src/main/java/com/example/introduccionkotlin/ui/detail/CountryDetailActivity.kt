package com.example.introduccionkotlin.ui.detail

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.ActivityCountryDetailBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.home.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryDetailActivity : AppCompatActivity(), CountryDetailFragment.OnDetailCountryFragmentListener {

    private lateinit var binding : ActivityCountryDetailBinding
    private var countryName: String = ""
    private var country: Country? = null
    private val viewModel: ListViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el nav controller (controlador de todos los fragments) con el nav_host_fragment (contenedor de fragments)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        // Configurar el bottom_navigation_menu con el nav controller

        // Agregar listener para cambiar el título del Toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (navController.currentDestination?.id) {
                    R.id.countryDetailFragment -> {
                        finish()
                    }
                    else -> {
                        irDetialFragment()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        countryName = intent.getStringExtra(COUNTRY_NAME).toString()
        if(intent.getSerializableExtra(COUNTRY) != null)
            country = intent.getSerializableExtra(COUNTRY) as Country

        irDetialFragment()
    }

    private fun irDetialFragment () {
        val args = Bundle()
        if(country?.id == null)
            country?.id = ""
        args.putSerializable(COUNTRY, country)
        args.putString(COUNTRY_NAME, countryName)
        navController.navigate(R.id.countryDetailFragment, args)
    }

    override fun goMap(country: Country) {
        val args = Bundle()
        args.putSerializable(COUNTRY, country)
        navController.navigate(R.id.countryMapFragment, args)
    }

    override fun goHome() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object{
        const val COUNTRY = "country"
        const val COUNTRY_NAME = "countryName"
    }

}