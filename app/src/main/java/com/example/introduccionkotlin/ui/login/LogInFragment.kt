package com.example.introduccionkotlin.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.introduccionkotlin.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseUser

class LogInFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var mListener: OnLogInFragmentListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)

        with(binding){
            btnLogin.setOnClickListener{
                with(binding){
                    val email = etMail.text.toString()
                    val password = etPassword.text.toString()
                    if(!email.isNullOrEmpty() && !password.isNullOrEmpty())
                        mListener.goLogIn(email, password)
                }
            }
            btnRegister.setOnClickListener{
                val action = LogInFragmentDirections.actionLogInFragmentToRegisterFragment()
                findNavController().navigate(action)
            }
        }

        return binding.root
    }

    interface OnLogInFragmentListener {
        fun goMain(user: FirebaseUser?)
        fun goLogIn(email: String, password: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as OnLogInFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString()
                    + " must implement OnLogInFragmentListener")
        }
    }
}