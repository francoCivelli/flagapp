package com.example.introduccionkotlin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.repository.CountriesRepository
import com.example.introduccionkotlin.util.NetworkResponse
import com.example.introduccionkotlin.util.SearchHelper
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.sql.SQLException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(private val repository: CountriesRepository) : ViewModel() {

    private val _countriesState = MutableStateFlow<CountryUIState>(CountryUIState.Nothing)
    val countriesUIState: StateFlow<CountryUIState> = _countriesState

    var countries : List<Country> = arrayListOf()
    var error : Boolean = false

    fun refresh () {
        fetchCountries()
    }

    private fun fetchCountries () {
        viewModelScope.launch {
            _countriesState.value = CountryUIState.Loading
            when(val result = repository.getCountries()) {
                is NetworkResponse.Success -> {
                    _countriesState.value = CountryUIState.Success(result.data.body())
                    countries = result.data.body()!!
                    error = false
                }
                else -> {
                    _countriesState.value = CountryUIState.Error
                    error = true
                }
            }
        }
    }

    fun fetchDatabaseCountries () {
        viewModelScope.launch {
            try {
                _countriesState.value = CountryUIState.Success(repository.getAll())
            } catch (e:SQLException){
                _countriesState.value = CountryUIState.Error
            }
        }
    }

    fun update (country: Country) {
        viewModelScope.launch {
            try {
                repository.update(country)
                _countriesState.value = CountryUIState.Update
            } catch (e:SQLException){
                _countriesState.value = CountryUIState.Error
            }
        }
    }


    fun getByName (countryName: String) {
        viewModelScope.launch {
            try {
                _countriesState.value = CountryUIState.GetCountry(repository.getByName(countryName))
            } catch (e:SQLException){
                _countriesState.value = CountryUIState.Error
            }
        }
    }


    // Chequeo si existe el pais en mi lista.
    fun checkCountryExists (country: Country) {
        viewModelScope.launch {
            try {
                _countriesState.value =
                    CountryUIState.Exists(repository.checkCountryExists(country.countryName) > 0)
            } catch (e: SQLException) {
                _countriesState.value = CountryUIState.Error
            }
        }
    }

    fun generateCountryId (country: Country?) : Country? {
        if(country?.id.isNullOrEmpty())
            country?.id = UUID.randomUUID().toString()
        return country
    }

    // Agrego pais a la lista de la base de datos
    fun addCountry (country: Country) {
        viewModelScope.launch {
            try {
                repository.insert(country)
                _countriesState.value = CountryUIState.Add
            } catch (e: SQLException){
                _countriesState.value = CountryUIState.Error
            }
        }
    }

    fun removeCountry (country: Country) {
        viewModelScope.launch {
            try {
                if(country.id.isNullOrEmpty())
                    country.id = UUID.randomUUID().toString()
                repository.delete(country)
                _countriesState.value = CountryUIState.Delete
            } catch (e: SQLException){
                _countriesState.value = CountryUIState.Error
            }
        }
    }

    fun checkCountriesScheduled (database: FirebaseDatabase, email: String) {
        viewModelScope.launch {
            val reference = database.getReference(INICIO).child(SearchHelper.removeSpecialCharacters(email)).child(COUNTRIES)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (country in countries) {
                        val existe = dataSnapshot.children.any {
                            val countryJson = it.getValue(String::class.java)
                            val copyCountry = Gson().fromJson(countryJson, Country::class.java)
                            copyCountry.countryName == country.countryName
                        }
                        if(existe)
                            country.selectedMap = true
                    }
                    _countriesState.value = CountryUIState.UpdateList(countries)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejo de errores
                }
            })
        }
    }

    fun addCountryMap (country: Country, database: FirebaseDatabase, email: String) {
        viewModelScope.launch {
            val reference = database.getReference(INICIO).child(SearchHelper.removeSpecialCharacters(email)).child(COUNTRIES)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val existe = dataSnapshot.children.any {
                        val countryJson = it.getValue(String::class.java)
                        val copyCountry = Gson().fromJson(countryJson, Country::class.java)
                        copyCountry.countryName == country.countryName
                    }
                    if (!existe) {
                        val copyCountry = generateCountryId(country)
                        val countryJson = Gson().toJson(copyCountry)
                        reference.child(copyCountry?.countryName ?: "").setValue(countryJson)
                        countries.find { copyCountry?.countryName == it.countryName }?.selectedMap =
                            true
                        _countriesState.value = CountryUIState.Add
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejo de errores
                }
            })
        }
    }

    fun removeCountryMap (country: Country, database: FirebaseDatabase, email: String) {
        viewModelScope.launch {
            val reference = database.getReference(INICIO).child(SearchHelper.removeSpecialCharacters(email)).child(COUNTRIES)
            val countryCopy = generateCountryId(country)
            if(countryCopy != null) {
                val query = reference.child(countryCopy.countryName?:"")
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val countryJson = dataSnapshot.getValue(String::class.java)
                        val copyCountry = Gson().fromJson(countryJson, Country::class.java)
                        if (copyCountry != null) {
                            dataSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    countries.find { country.countryName == it.countryName }?.selectedMap = false
                                    _countriesState.value = CountryUIState.Delete
                                }
                                .addOnFailureListener {
                                    _countriesState.value = CountryUIState.Error
                                }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        _countriesState.value = CountryUIState.Error
                    }
                })
            }
        }
    }

    fun clearState () {
        _countriesState.value = CountryUIState.Nothing
    }

    // Country states
    sealed class CountryUIState {
        data class Success(val countries: List<Country>?) : CountryUIState()
        data class UpdateList(val countries: List<Country>?) : CountryUIState()
        data class Exists(val exists: Boolean) : CountryUIState()
        data class GetCountry(val country: Country?) : CountryUIState()
        object Error : CountryUIState()
        object Update : CountryUIState()
        object Delete : CountryUIState()
        object Add : CountryUIState()
        object Loading : CountryUIState()
        object Nothing : CountryUIState()
    }

    companion object{
        private const val COUNTRIES = "countries"
        private const val INICIO = "/"
    }

}