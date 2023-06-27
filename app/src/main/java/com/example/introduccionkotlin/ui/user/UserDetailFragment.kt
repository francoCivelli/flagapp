package com.example.introduccionkotlin.ui.user

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.FragmentUserDetailBinding
import com.example.introduccionkotlin.model.User
import com.example.introduccionkotlin.ui.login.LogInActivity.Companion.KEY_EMAIL
import com.example.introduccionkotlin.ui.login.LogInActivity.Companion.KEY_PASSWORD
import com.example.introduccionkotlin.util.FormaterDate
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private lateinit var mListener: OnRegisterFragmentListener
    private lateinit var binding: FragmentUserDetailBinding
    private val viewModel: UserViewModel by viewModels()
    private val user = FirebaseAuth.getInstance().currentUser
    private var email = ""
    private var password = ""
    private var btnSaveSelected = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentUserDetailBinding.inflate(inflater)

        setUpObservers()

        return binding.root
    }

    private fun setUpView (user : User?) {
        val oldPassword = user?.password
        with(binding) {
            etMail.isEnabled = false
            if (user != null) {
                etName.setText(user.name?:"")
                etMail.setText(user.email ?: "")
                etPassword.setText(user.password ?: "")
                etCellphone.setText(user.phone ?: "")
                etDate.text = user.birthDate ?: ""
            } else {
                etMail.setText(email)
                etPassword.setText(password)
            }
            btnLogout.setOnClickListener {
                mListener.goLogout()
            }
            btnSave.setOnClickListener {
                if (viewModel.validateImputs(
                        etName.text.toString(),
                        etMail.text.toString(), etPassword.text.toString(),
                        etCellphone.text.toString(), etDate.text.toString()
                    )
                ) {
                    if (oldPassword != etPassword.text.toString()) {
                        resetPassword()
                    }

                    viewModel.update(if(user == null)
                        viewModel.createUser(etName.text.toString(),
                        etMail.text.toString(), etPassword.text.toString(),
                        etCellphone.text.toString(), etDate.text.toString())
                    else
                        viewModel.updateUser(
                        user, etName.text.toString(),
                        etMail.text.toString(), etPassword.text.toString(),
                        etCellphone.text.toString(), etDate.text.toString())
                    )
                    btnSaveSelected = true
                } else {
                    Snackbar.make(
                        binding.root,
                        resources.getString(R.string.sanckbar_uncomplete_credentials),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            editDateCard.setOnClickListener {
                datePickerDialog()
            }
        }
    }

    private fun setUpObservers () {
        viewModel.apply {
            // Obtener instancia de SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_user), Context.MODE_PRIVATE)
            // Obtener valor de preferencia
            email = sharedPreferences?.getString(KEY_EMAIL, "").toString()
            password = sharedPreferences?.getString(KEY_PASSWORD, "").toString()
            getByEmail(email?:"")
            lifecycleScope.launch {
                usersUIState.collectLatest { updateUsers(it) }
            }
        }
    }

    private fun updateUsers(model: UserViewModel.UserUIState){
        when (model) {
            is UserViewModel.UserUIState.Success -> {
                model.user.let {
                    if(it == null) {
                        if (btnSaveSelected)
                            btnSaveSelected = false
                        Snackbar.make(
                            binding.root,
                            resources.getString(R.string.sanckbar_error_user),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        if (btnSaveSelected) {
                            btnSaveSelected = false
                            Snackbar.make(
                                binding.root,
                                resources.getString(R.string.sanckbar_data_user_save),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    setUpView(it)
                }
            }
            is UserViewModel.UserUIState.Error -> {
                Snackbar.make(requireContext(), binding.root, resources.getString(R.string.sanckbar_error_user), Snackbar.LENGTH_SHORT).show()
                setUpView(null)
            }
            else -> Unit
        }
        viewModel.clearState()
    }

    // Edito fecha de forma individual
    private fun datePickerDialog () {
        val fecha = setSeleccionFecha()
        val datePickerDialog = DatePickerDialog(requireContext(), R.style.MyDatePickerTheme, { view, year, month, dayOfMonth ->
            val fechaElegida = FormaterDate.formatDigitDate(dayOfMonth) + "/" + FormaterDate.formatDigitDate(month + 1) + "/" + year
            binding.etDate.text = fechaElegida
        }, fecha[2].toInt(), fecha[1].toInt() -1, fecha[0].toInt()) // le seteo por defecto la fecha actual
        datePickerDialog.show()
    }

    // Muestro en mi datePicker la fecha que tengo cargada o la fecha actual como seleccion inicial.
    private fun setSeleccionFecha () : List<String> {
        return if(binding.etDate.text.isNotEmpty()){
            binding.etDate.text.toString().split("/")
        } else
            FormaterDate.getFechaActual()!!.split("/")
    }

    private fun resetPassword () {
        val newPassword = binding.etPassword.text.toString()
        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Contraseña cambiada exitosamente
                Snackbar.make(binding.root, resources.getString(R.string.sanckbar_password_ok), Snackbar.LENGTH_SHORT).show()
            } else {
                // Error al cambiar la contraseña
                Snackbar.make(binding.root, resources.getString(R.string.sanckbar_password_error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    interface OnRegisterFragmentListener {
        fun goLogout()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as OnRegisterFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString()
                    + " must implement OnRegisterFragmentListener")
        }
    }
}