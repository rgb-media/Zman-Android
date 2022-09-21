package com.rgbmedia.zman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.rgbmedia.zman.adapters.MenuAdapter
import com.rgbmedia.zman.databinding.ActivityMainBinding
import com.rgbmedia.zman.models.MenuElement
import com.rgbmedia.zman.utils.Utils
import com.rgbmedia.zman.viewmodels.MainMenuElement
import com.rgbmedia.zman.viewmodels.MenuViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val menuViewModel: MenuViewModel by viewModels()

    private var menuHeight = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.zman_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        getMenu()

        setupArticleHeader()

        initWebView()

        binding.webView.loadUrl(HOMEPAGE_URL)
    }

    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.setUserAgentString(binding.webView.getSettings().getUserAgentString() + " rgbmedia-app android app")
        binding.webView.addJavascriptInterface(WebAppInterface(), "Android")

        binding.refresh.setOnRefreshListener({ binding.webView.reload() })

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (binding.refresh.isRefreshing()) {
                    binding.refresh.setRefreshing(false)
                }
            }
        }
    }

    private fun getMenu() {
        val observer = Observer<List<MainMenuElement>> { list->
            setupMenu()

            binding.menuRecyclerView.adapter = MenuAdapter(list)
        }

        menuViewModel.getMenu().observe(this, observer)
    }

    private fun setupMenu() {
        binding.menuContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.menuContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (menuHeight == -1) {
                    menuHeight = binding.menuContainer.height
                }

                binding.menuContainer.getLayoutParams().height = 0
                binding.menuContainer.requestLayout()

                binding.menuContainer.visibility = View.VISIBLE
            }
        })

        binding.menuButton.setOnClickListener {
            binding.menuButton.setImageResource(R.drawable.menu_hamburger)

            var newHeight = 0
            if (binding.menuContainer.height == 0) {
                newHeight = menuHeight

                binding.menuButton.setImageResource(R.drawable.menu_close)
            }

            Utils.slideView(binding.menuContainer, binding.menuContainer.height, newHeight)
        }
    }

    private fun setupArticleHeader() {
        binding.backButton.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                binding.webView.evaluateJavascript("rgb.closePopupArticleInApp()", null)

                binding.headerArticle.visibility = View.GONE
            }
        }

        binding.shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, binding.webView.url)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.closeButton.setOnClickListener {
            binding.webView.evaluateJavascript("rgb.closePopupArticleInApp()", null)

            binding.headerArticle.visibility = View.GONE
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun popupOpened() {
            Log.d("RGBJavascript", "popupOpened")

            runOnUiThread {
                binding.headerArticle.visibility = View.VISIBLE
            }
        }

        @JavascriptInterface
        fun popupClosed() {
            Log.d("RGBJavascript", "popupClosed")
        }
    }
}