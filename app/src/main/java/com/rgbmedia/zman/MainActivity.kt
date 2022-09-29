package com.rgbmedia.zman

import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rgbmedia.zman.adapters.MenuAdapter
import com.rgbmedia.zman.databinding.ActivityMainBinding
import com.rgbmedia.zman.network.MenuRepository
import com.rgbmedia.zman.network.MenuService
import com.rgbmedia.zman.utils.Utils
import com.rgbmedia.zman.viewmodels.MainViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    class MenuViewModelFactory(private val api: MenuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(api) as T
        }
    }

    private lateinit var binding: ActivityMainBinding

    private val api by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(MENU_URL)
            .build()

        val api = retrofit.create(MenuService::class.java)

        MenuRepository(api)
    }
    private val mainViewModel: MainViewModel by viewModels { MenuViewModelFactory(api) }

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

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.userAgentString = binding.webView.settings.userAgentString + " rgbmedia-app android app"
        binding.webView.addJavascriptInterface(WebAppInterface(), "Android")

        binding.refresh.setOnRefreshListener { binding.webView.reload() }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (binding.refresh.isRefreshing) {
                    binding.refresh.isRefreshing = false
                }
            }
        }

        mainViewModel.getWebviewUrlString().observe(this) {
            binding.webView.loadUrl(it)
        }
    }

    private fun getMenu() {
        mainViewModel.getMenu().observe(this) {
            setupMenu()

            binding.menuRecyclerView.adapter = MenuAdapter(it, mainViewModel)
        }
    }

    private fun setupMenu() {
        mainViewModel.getShowMenu().observe(this) {
            binding.menuButton.setImageResource(R.drawable.menu_hamburger)

            var newHeight = 0
            if (it) {
                binding.menuContainer.visibility = View.VISIBLE

                newHeight = menuHeight

                binding.menuButton.setImageResource(R.drawable.menu_close)
            }

            Utils.slideView(binding.menuContainer, binding.menuContainer.height, newHeight)
        }

        mainViewModel.getSelectedMenuItem().observe(this) {
            val oldViewHolder = binding.menuRecyclerView.findViewHolderForAdapterPosition(mainViewModel.getOldSelectedMenuItem().first)
            if (oldViewHolder != null) {
                val oldMenuViewHolder = oldViewHolder as MenuAdapter.ViewHolder
                oldMenuViewHolder.itemsRecyclerView.adapter?.notifyItemChanged(mainViewModel.getOldSelectedMenuItem().second)
            }

            val newViewHolder = binding.menuRecyclerView.findViewHolderForAdapterPosition(it.first)
            if (newViewHolder != null) {
                val newMenuViewHolder = newViewHolder as MenuAdapter.ViewHolder
                newMenuViewHolder.itemsRecyclerView.adapter?.notifyItemChanged(it.second)
            }

            mainViewModel.setOldSelectedMenuItem(it)
        }

        binding.menuContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.menuContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (menuHeight == -1) {
                    menuHeight = binding.menuContainer.height
                }

                binding.menuContainer.layoutParams.height = 0
                binding.menuContainer.requestLayout()
            }
        })

        binding.menuButton.setOnClickListener {
            mainViewModel.setShowMenu(!mainViewModel.getShowMenu().value!!)
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