package com.example.introduccionkotlin.ui.scheduled

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.adapters.CountryListAdapter
import com.example.introduccionkotlin.databinding.FragmentCountriesListSchedulesBinding
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.ui.home.ListViewModel
import com.example.introduccionkotlin.ui.login.LogInActivity
import com.example.introduccionkotlin.util.SearchHelper
import com.google.android.material.snackbar.Snackbar
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
class CountriesScheduledFragment : Fragment(), CountryListAdapter.OnCountryListener {

    private lateinit var binding: FragmentCountriesListSchedulesBinding
    private lateinit var mListener: OnScheduledFragmentListener
    private val countriesAdapter = CountryListAdapter(arrayListOf(), false, this)
    private val viewModel: ListViewModel by viewModels()
    private var deleted: Country? = null

    private lateinit var database: FirebaseDatabase
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        database = FirebaseDatabase.getInstance()
        // Obtener instancia de SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_user), Context.MODE_PRIVATE)
        // Obtener valor de preferencia
        email = sharedPreferences?.getString(LogInActivity.KEY_EMAIL, "").toString()
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
                removeCountryMap(deleted!!)
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
    override fun addCountryMap(country: Country) {}

    override fun removeCountryMap(country: Country) {
        val reference = database.getReference("/").child(SearchHelper.removeSpecialCharacters(email)).child("countries")
        val countryCopy = viewModel.generateCountryId(country)
        if(countryCopy != null) {
            val query = reference.child(countryCopy.countryName?:"")
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val countryJson = dataSnapshot.getValue(String::class.java)
                    val copyCountry = Gson().fromJson(countryJson, Country::class.java)
                    if (copyCountry != null) {
                        dataSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Snackbar.make(binding.root, resources.getString(R.string.sanckbar_country_remove), Snackbar.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Snackbar.make(binding.root, resources.getString(R.string.sanckbar_country_remove_error), Snackbar.LENGTH_SHORT).show()
                            }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Ocurrió un error al realizar la consulta
                }
            })
        }
    }

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