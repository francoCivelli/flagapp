package com.example.introduccionkotlin.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.FragmentCountryMapBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.detail.CountryDetailActivity.Companion.COUNTRY
import com.example.introduccionkotlin.ui.home.ListViewModel
import com.example.introduccionkotlin.ui.login.LogInActivity
import com.example.introduccionkotlin.util.SearchHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CountryMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentCountryMapBinding
    private lateinit var googleMap: GoogleMap
    private var countries: ArrayList<Country> = arrayListOf()
    private val viewModel: ListViewModel by viewModels()

    private lateinit var database: FirebaseDatabase
    private var email: String = ""
    private var country: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        database = FirebaseDatabase.getInstance()

        arguments?.let{
            country = it.getSerializable(COUNTRY) as? Country?
        }
        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_user), Context.MODE_PRIVATE)
        email = sharedPreferences?.getString(LogInActivity.KEY_EMAIL, "").toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentCountryMapBinding.inflate(inflater)

        binding.apply {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@CountryMapFragment)
        }

        setUpObservers()

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

    private fun updateCountries(model: ListViewModel.CountryUIState){
        when (model) {
            is ListViewModel.CountryUIState.Success -> {
                model.countries?.let { countries ->
                    binding.apply {
                        loadingView.visibility = View.GONE
                        if(countries.isNullOrEmpty()){
                            listError.text = resources.getString(R.string.empty_list)
                            listError.visibility = View.VISIBLE
                            mapView.visibility = View.GONE
                        } else {
                            listError.visibility = View.GONE
                            mapView.visibility = View.VISIBLE
                            this@CountryMapFragment.countries.addAll(countries)
                            if (::googleMap.isInitialized) {
                                onMapReady(googleMap)
                            }
                        }
                    }
                }
            }
            is ListViewModel.CountryUIState.Error -> {
                binding.apply {
                    loadingView.visibility = View.GONE
                    listError.text = resources.getString(R.string.an_ocurred_while_loading_data)
                    listError.visibility = View.VISIBLE
                    mapView.visibility = View.GONE
                }
            }
            is ListViewModel.CountryUIState.Loading -> {
                binding.apply {
                    loadingView.visibility = View.VISIBLE
                    listError.visibility = View.GONE
                    mapView.visibility = View.GONE
                }
            }
            else -> Unit
        }
        viewModel.clearState()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun markCountryInMap (country: Country) {
        if(country.latlng.size == 2) {
            val latLng = LatLng(country.latlng[0], country.latlng[1])
            // Lo marca en el mapa
            googleMap.addMarker(MarkerOptions().position(latLng).title(country.countryName))
            // Centra el mapa al pais
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.uiSettings.isZoomControlsEnabled = true

        if(country != null){
            markCountryInMap(country!!)
        } else {
            val reference = database.getReference("/").child(SearchHelper.removeSpecialCharacters(email)).child("countries")
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val countryJson = childSnapshot.getValue(String::class.java)
                        val copyCountry = Gson().fromJson(countryJson, Country::class.java)
                        if (copyCountry != null) {
                            markCountryInMap(copyCountry)
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejo de errores
                }
            })
        }
    }
}