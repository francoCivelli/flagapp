package com.example.introduccionkotlin.ui.splashScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.introduccionkotlin.databinding.ActivitySplashScreenBinding
import com.example.introduccionkotlin.ui.login.LogInActivity
import com.google.android.gms.common.util.IOUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity()  {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        with(binding){
            splashScreenProgresBar.visibility = ProgressBar.VISIBLE
            try {
                val inputStream: InputStream = this@SplashScreenActivity.assets.open("flag_splash.gif")
                val bytes: ByteArray = IOUtils.toByteArray(inputStream)
                gifSplashScreen.setBytes(bytes)
                gifSplashScreen.startAnimation()
            } catch (ex: IOException) {
            }
        }
        lifecycleScope.launch {
            delay(3000)
            val intent = Intent(this@SplashScreenActivity, LogInActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        finish()
    }
}