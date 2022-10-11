package com.rgbmedia.zman

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rgbmedia.zman.adapters.MenuAdapter
import com.rgbmedia.zman.databinding.ActivityMainBinding
import com.rgbmedia.zman.network.MenuRepository
import com.rgbmedia.zman.network.MenuService
import com.rgbmedia.zman.network.NewsletterRepository
import com.rgbmedia.zman.network.NewsletterService
import com.rgbmedia.zman.utils.Utils
import com.rgbmedia.zman.viewmodels.MainViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainActivity : AppCompatActivity() {

    class MenuViewModelFactory(private val menuApi: MenuRepository,
                               private val newsletterApi: NewsletterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(menuApi, newsletterApi) as T
        }
    }

    private lateinit var binding: ActivityMainBinding

    private val menuApi by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(MENU_URL)
            .build()

        val api = retrofit.create(MenuService::class.java)

        MenuRepository(api)
    }
    private val newsletterApi by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(SENDGRID_URL)
            .build()

        val api = retrofit.create(NewsletterService::class.java)

        NewsletterRepository(api)
    }
    private val mainViewModel: MainViewModel by viewModels { MenuViewModelFactory(menuApi, newsletterApi) }

    private var menuHeight = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.zman_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        getMenu()

        setupNewsletter()

        setupArticleHeader()

        initWebView()

        binding.webView.loadUrl(HOMEPAGE_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.settings.setSupportMultipleWindows(true)
        binding.webView.settings.userAgentString = binding.webView.settings.userAgentString + " rgbmedia-app android app"
        binding.webView.addJavascriptInterface(WebAppInterface(), "Android")

        binding.refresh.setOnRefreshListener { binding.webView.reload() }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                if (url.startsWith("mailto:")) {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))

                    return true
                }

                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (binding.refresh.isRefreshing) {
                    binding.refresh.isRefreshing = false
                }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                val newWebView = WebView(this@MainActivity)
                binding.refresh.addView(newWebView)

                val transport = resultMsg!!.obj as WebViewTransport
                transport.webView = newWebView
                resultMsg!!.sendToTarget()

                return true
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

    private fun setupNewsletter() {
        mainViewModel.getNewsletterResponse().observe(this) {
            if (it.lowercase() == "success") {
                val viewHolder = binding.menuRecyclerView.findViewHolderForAdapterPosition(mainViewModel.getNewsletterPosition().first)
                if (viewHolder != null) {
                    val menuViewHolder = viewHolder as MenuAdapter.ViewHolder
                    menuViewHolder.itemsRecyclerView.adapter?.notifyItemChanged(mainViewModel.getNewsletterPosition().second)
                }
            } else if (it.isNotEmpty()) {
                Utils.showSimpleAlert(this, TEXT_GENERIC_ERROR, it)
            }
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