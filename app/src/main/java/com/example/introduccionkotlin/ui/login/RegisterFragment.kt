package com.example.introduccionkotlin.ui.login

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.introduccionkotlin.R
import com.example.introduccionkotlin.databinding.FragmentRegisterBinding
import com.example.introduccionkotlin.model.User
import com.example.introduccionkotlin.ui.user.UserViewModel
import com.example.introduccionkotlin.util.FormaterDate
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var mListener: OnRegisterFragmentListener
    private lateinit var binding: FragmentRegisterBinding
    private var auth: FirebaseAuth? = FirebaseAuth.getInstance()
    private val viewModel: UserViewModel by viewModels()
    private var userSelected: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater)

        setUpObservers()

        with(binding){
            btnSave.setOnClickListener {
                val email = etMail.text.toString()
                val password = etPassword.text.toString()
                if(!email.isNullOrEmpty() && !password.isNullOrEmpty())
                    register(email, password)
                else
                    Snackbar.make(requireContext(), this.root, resources.getString(R.string.sanckbar_uncomplete_credentials), Snackbar.LENGTH_SHORT).show()
            }
            editDateCard.setOnClickListener{
                datePickerDialog()
            }
        }

        return binding.root
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
                    if(it)
                        Snackbar.make(requireContext(), binding.root, resources.getString(R.string.sanckbar_error_register), Snackbar.LENGTH_SHORT).show()
                    else
                        viewModel.addUser(userSelected!!)
                }
            }
            is UserViewModel.UserUIState.Add -> {
                val action = RegisterFragmentDirections.actionRegisterFragmentToLogInFragment()
                findNavController().navigate(action)
            }
            is UserViewModel.UserUIState.Error -> {
                Snackbar.make(requireContext(), binding.root, resources.getString(R.string.sanckbar_error_register), Snackbar.LENGTH_SHORT).show()
            }
            else -> Unit
        }
        viewModel.clearState()
    }


    // Edito fecha de forma individual
    private fun datePickerDialog () {
        val fecha = setSeleccionFecha()
        val datePickerDialog = DatePickerDialog(requireContext(), R.style.AppTheme_DatePickerDialog, { view, year, month, dayOfMonth ->
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

    private fun register(email: String, password: String) {
        with(binding){
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(requireContext(), binding.root, resources.getString(R.string.sanckbar_register_ok), Snackbar.LENGTH_SHORT).show()
                        addUser(etName.text.toString(), etMail.text.toString(), etPassword.text.toString(), etPhone.text.toString(), etDate.text.toString())
                    } else {
                        Snackbar.make(requireContext(), binding.root, resources.getString(R.string.sanckbar_error_register), Snackbar.LENGTH_SHORT).show()
                        mListener.goMain(null)
                    }
                }
        }
    }

    private fun addUser(name: String, email: String, password: String, phone: String, date: String) {
        userSelected = viewModel.createUser(name, email, password, phone, date)
        viewModel.existsUser(userSelected!!)
    }

    interface OnRegisterFragmentListener {
        fun goMain(user: FirebaseUser?)
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