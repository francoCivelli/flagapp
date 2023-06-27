package com.example.introduccionkotlin.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.ActivityMainBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.detail.CountryDetailActivity
import com.example.introduccionkotlin.ui.detail.CountryDetailActivity.Companion.COUNTRY
import com.example.introduccionkotlin.ui.detail.CountryDetailActivity.Companion.COUNTRY_NAME
import com.example.introduccionkotlin.ui.home.HomeFragment
import com.example.introduccionkotlin.ui.scheduled.CountriesScheduledFragment
import com.example.introduccionkotlin.ui.user.UserDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UserDetailFragment.OnRegisterFragmentListener,
    HomeFragment.OnHomeFragmentListener, CountriesScheduledFragment.OnScheduledFragmentListener {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el nav controller (controlador de todos los fragments) con el nav_host_fragment (contenedor de fragments)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        // Configurar el bottom_navigation_menu con el nav controller
        binding.bottomBar.setupWithNavController(navController)

        // Agregar listener para cambiar el título del Toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
        }

        NavigationUI.setupWithNavController(binding.bottomBar, navHostFragment.navController)
    }

    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                goLogout()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun goLogout() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(resources.getString(R.string.title_log_out))
        alertDialogBuilder.setMessage(resources.getString(R.string.message_log_out))
        alertDialogBuilder.setPositiveButton(resources.getString(R.string.success)) { dialog, _ ->
            setResult(RESULT_FIRST_USER)
            finish()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton(resources.getString(R.string.negative)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun goDetail(countryName: String) {
        val intent = Intent(this, CountryDetailActivity::class.java)
        intent.putExtra(COUNTRY_NAME, countryName)
        startActivityForResult(intent, GO_DETAIL)
    }

    override fun goDetail(country: Country) {
        val intent = Intent(this, CountryDetailActivity::class.java)
        intent.putExtra(COUNTRY, country)
        startActivityForResult(intent, GO_DETAIL)
    }

    companion object{
        const val GO_DETAIL = 1015
    }
}