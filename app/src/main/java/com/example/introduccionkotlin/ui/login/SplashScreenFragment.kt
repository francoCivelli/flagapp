package com.example.introduccionkotlin.ui.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.introduccionkotlin.databinding.FragmentSplashScreenBinding
import com.google.android.gms.common.util.IOUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashScreenBinding.inflate(inflater)

        with(binding){
            splashScreenProgresBar.visibility = ProgressBar.VISIBLE

            try {
                val inputStream: InputStream = requireContext().assets.open("flag_splash.gif")
                val bytes: ByteArray = IOUtils.toByteArray(inputStream)
                gifSplashScreen.setBytes(bytes)
                gifSplashScreen.startAnimation()
            } catch (ex: IOException) {
            }

            lifecycleScope.launch {
                delay(3000)
                val action = SplashScreenFragmentDirections.actionSplashScreenFragmentToLogInFragment()
                findNavController().navigate(action)
            }
        }

        return binding.root
    }
}