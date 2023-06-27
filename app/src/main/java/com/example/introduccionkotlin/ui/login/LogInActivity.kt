package com.example.introduccionkotlin.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.ActivityLoginBinding
import com.example.introduccionkotlin.model.User
import com.example.introduccionkotlin.ui.MainActivity
import com.example.introduccionkotlin.ui.user.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogInActivity : AppCompatActivity() , LogInFragment.OnLogInFragmentListener,
    RegisterFragment.OnRegisterFragmentListener {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: UserViewModel by viewModels()
    private var auth: FirebaseAuth? = null
    private var userSelected: User? = null
    private var logIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        binding.loadingView.visibility = ProgressBar.GONE
        // Configurar el nav controller (controlador de todos los fragments) con el nav_host_fragment (contenedor de fragments)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        // Configurar el bottom_navigation_menu con el nav controller

        // Agregar listener para cambiar el título del Toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (navController.currentDestination?.id) {
                    R.id.logInFragment -> {
                        finish()
                    }
                    else -> {
                        navController.navigate(R.id.logInFragment)
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setUpObservers()
    }

    override fun goLogIn (email: String, password: String) {
        binding.loadingView.visibility = ProgressBar.VISIBLE
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(this) { task ->
                binding.loadingView.visibility = ProgressBar.GONE
                if (task.isSuccessful) {
                    val prefs = getSharedPreferences(getString(R.string.prefs_user), Context.MODE_PRIVATE).edit()
                    prefs.putString(KEY_EMAIL, email)
                    prefs.putString(KEY_PASSWORD, password)
                    prefs.apply()
                    userSelected = viewModel.createUser("", email, password, "", "")
                    viewModel.existsUser(userSelected!!)
                } else {
                    Snackbar.make(this, binding.root, resources.getString(R.string.sanckbar_error_login), Snackbar.LENGTH_SHORT).show()
                    goMain(null)
                }
            }
    }

    private fun setUpObservers () {
        viewModel.apply {
            lifecycleScope.launch {
                usersUIState.collectLatest { updateUsers(it) }
            }
        }
    }

    private fun updateUsers(model: UserViewModel.UserUIState){
        when (model) {
            is UserViewModel.UserUIState.Exists -> {
                model.exists.let {
                    if(!it)
                        viewModel.addUser(userSelected!!)
                    else {
                        val user = auth?.currentUser
                        goMain(user)
                    }
                }
            }
            is UserViewModel.UserUIState.Add -> {
                val user = auth?.currentUser
                goMain(user)
            }
            is UserViewModel.UserUIState.Error -> {
            }
            else -> Unit
        }
        viewModel.clearState()
    }

    @SuppressLint("CommitPrefEdits")
    override fun goMain (user : FirebaseUser?) {
        if(user != null){
            logIn = true
            val intent = Intent(this, MainActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        auth.let{
            val currentUser = it?.currentUser
            if(currentUser != null && !logIn)
                goMain(currentUser)
        }
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_FIRST_USER) {
            auth?.signOut()
            logIn = false
            navController.navigate(R.id.logInFragment)
        }
    }

    companion object{
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
    }

}