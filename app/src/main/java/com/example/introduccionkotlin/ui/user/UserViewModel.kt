package com.example.introduccionkotlin.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.introduccionkotlin.model.User
import com.example.introduccionkotlin.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.sql.SQLException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UsersRepository) :ViewModel() {

    private val _usersState = MutableStateFlow<UserUIState>(UserUIState.Nothing)
    val usersUIState: StateFlow<UserUIState> = _usersState

    fun addUser (user: User) {
        viewModelScope.launch {
            try {
                if(user.id.isNullOrEmpty())
                    user.id = UUID.randomUUID().toString()
                userRepository.insert(user)
                _usersState.value = UserUIState.Add
            } catch (e: SQLException){
                _usersState.value = UserUIState.Error
            }
        }
    }

    fun getByEmail (email: String) {
        viewModelScope.launch {
            try {
                _usersState.value = UserUIState.Success(userRepository.getByEmail(email))
            } catch (e: Exception){
                _usersState.value = UserUIState.Error
            }
        }
    }

    fun update (user: User) {
        viewModelScope.launch {
            try {
                userRepository.update(user)
                _usersState.value = UserUIState.Update
            } catch (e: SQLException){
                _usersState.value = UserUIState.Error
            }
        }
    }

    fun existsUser (user: User){
        viewModelScope.launch {
            _usersState.value = try {
                 UserUIState.Exists(userRepository.checkUserExists(user.email) > 0)
            } catch (e: SQLException){
                UserUIState.Error
            }
        }
    }


    fun createUser (name: String, email: String, password: String, phone: String, date: String) : User {
        return User(UUID.randomUUID().toString(), name, email, password, phone, date)
    }

    fun updateUser (user: User, name: String, email: String, password: String, phone: String, date: String) : User {
        user.name = name
        user.email = email
        user.password = password
        user.phone = phone
        user.birthDate = date
        return user
    }

    fun validateImputs (name: String, email: String, password: String, phone: String, date: String) =
        !name.isNullOrEmpty() && !email.isNullOrEmpty() && !password.isNullOrEmpty() &&
                !phone.isNullOrEmpty() && !date.isNullOrEmpty()


    fun clearState () {
        _usersState.value = UserUIState.Nothing
    }

    // User states
    sealed class UserUIState {
        data class Success(val user: User) : UserUIState()
        data class Exists(val exists: Boolean) : UserUIState()
        object Error : UserUIState()
        object Add : UserUIState()
        object Update : UserUIState()
        object Nothing : UserUIState()
    }

}