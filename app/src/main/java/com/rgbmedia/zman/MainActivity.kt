package com.rgbmedia.zman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rgbmedia.zman.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.zman_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        binding.webView.loadUrl(HOMEPAGE_URL)
    }
}