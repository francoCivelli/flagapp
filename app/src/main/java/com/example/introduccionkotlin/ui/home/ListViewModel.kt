package com.example.introduccionkotlin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.introduccionkotlin.model.Country
import com.example.introduccionkotlin.repository.CountriesRepository
import com.example.introduccionkotlin.util.NetworkResponse
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

    // Agrego pais a la lista de la base de datos
    fun addCountry (country: Country) {
        viewModelScope.launch {
            try {
                if(country.id.isNullOrEmpty())
                    country.id = UUID.randomUUID().toString()
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

    fun clearState () {
        _countriesState.value = CountryUIState.Nothing
    }

    // Country states
    sealed class CountryUIState {
        data class Success(val countries: List<Country>?) : CountryUIState()
        data class Exists(val exists: Boolean) : CountryUIState()
        data class GetCountry(val country: Country?) : CountryUIState()
        object Error : CountryUIState()
        object Update : CountryUIState()
        object Delete : CountryUIState()
        object Add : CountryUIState()
        object Loading : CountryUIState()
        object Nothing : CountryUIState()
    }

}