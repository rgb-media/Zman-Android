package com.rgbmedia.zman

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.facebook.CallbackManager
import com.rgbmedia.zman.adapters.MenuAdapter
import com.rgbmedia.zman.adapters.SearchAdapter
import com.rgbmedia.zman.databinding.ActivityMainBinding
import com.rgbmedia.zman.network.*
import com.rgbmedia.zman.utils.LoginState
import com.rgbmedia.zman.utils.LoginStatus
import com.rgbmedia.zman.utils.Utils
import com.rgbmedia.zman.viewmodels.FacebookLoginManager
import com.rgbmedia.zman.viewmodels.MainViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class MainActivity : AppCompatActivity() {

    class MenuViewModelFactory(private val menuApi: MenuRepository,
                               private val newsletterApi: NewsletterRepository,
                               private val searchApi: SearchRepository,
                               private val loginApi: LoginRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(menuApi, newsletterApi, searchApi, loginApi) as T
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
    private val searchApi by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(SEARCH_URL)
            .build()

        val api = retrofit.create(SearchService::class.java)

        SearchRepository(api)
    }
    private val loginApi by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(LOGIN_URL)
            .client(client)
            .build()

        val api = retrofit.create(LoginService::class.java)

        LoginRepository(api)
    }
    private val mainViewModel: MainViewModel by viewModels { MenuViewModelFactory(menuApi, newsletterApi, searchApi, loginApi) }

    private var menuHeight = -1

    private val callbackManager = CallbackManager.Factory.create()

    private val facebookLoginManager by lazy { FacebookLoginManager(mainViewModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.zman_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        getMenu()

        setupNewsletter()

        setupSearch()

        setupLogin()

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
                resultMsg.sendToTarget()

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

    private fun setupSearch() {
        binding.menuRecyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            positionSearchResultsView()
        }

        mainViewModel.getSearchResultsVisible().observe(this) {
            if (it) {
                binding.searchResultsRV.visibility = View.VISIBLE
            } else {
                binding.searchResultsRV.visibility = View.GONE
            }
        }

        mainViewModel.getSearchResponse().observe(this) {
            if (it != null && it.isNotEmpty()) {
                positionSearchResultsView()

                binding.searchResultsRV.adapter = SearchAdapter(it, mainViewModel)

                mainViewModel.setSearchResultsVisible(true)
            } else {
                mainViewModel.setSearchResultsVisible(false)
            }
        }
    }

    private fun setupLogin() {
        mainViewModel.getShowLogin().observe(this) {
            if (it) {
                binding.loginView.visibility = View.VISIBLE
            } else {
                binding.loginView.visibility = View.GONE
            }

        }

        binding.loginClose.setOnClickListener {
            mainViewModel.setShowLogin(false)
        }

        binding.loginFb.setOnClickListener {
            facebookLoginManager.login(this, callbackManager)
        }

        mainViewModel.getLoginError().observe(this) {
            if (it.isNotEmpty()) {
                Utils.showSimpleAlert(this, getString(R.string.fb_error), it)
            }
        }

        mainViewModel.getLoginData().observe(this) {
            if (it.status.isNotEmpty()) {
                Utils.showSimpleAlert(this, getString(R.string.info), getString(R.string.fb_success)) { _, _ ->
                    mainViewModel.setShowLogin(false)
                    mainViewModel.setShowMenu(false)

                    binding.menuRecyclerView.adapter?.notifyItemChanged(binding.menuRecyclerView.adapter!!.itemCount - 1)
                }

                val dataString = Utils.getDataStringFromLoginModel(it)

                LoginState.setLoginStatus(LoginStatus.loggedInWithFb)
                LoginState.setLoginData(dataString)

                val cookie = Utils.getCookieFromLoginModel(it)
                CookieManager.getInstance().setCookie(".zman.co.il", cookie) {
                    Log.d("Cookies", "User data cookie set: $it")
                }
            }
        }

        mainViewModel.getShowLogout().observe(this) {
            if (it) {
                Utils.showSimpleAlert(this, getString(R.string.info), getString(R.string.logout))

                binding.menuRecyclerView.adapter?.notifyItemChanged(binding.menuRecyclerView.adapter!!.itemCount - 1)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
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

    private fun positionSearchResultsView() {
        val view = binding.menuRecyclerView.getChildAt(mainViewModel.getSearchPosition().first)

        if (view != null) {
            val layoutParams = binding.searchResultsRV.layoutParams as RelativeLayout.LayoutParams
            layoutParams.topMargin = (view.y + view.height + 66).toInt()
            binding.searchResultsRV.layoutParams = layoutParams
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