package com.rgbmedia.zman.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rgbmedia.zman.HOMEPAGE_URL
import com.rgbmedia.zman.models.LoginData
import com.rgbmedia.zman.models.LoginModel
import com.rgbmedia.zman.models.MenuElement
import com.rgbmedia.zman.models.SearchResult
import com.rgbmedia.zman.network.LoginRepository
import com.rgbmedia.zman.network.MenuRepository
import com.rgbmedia.zman.network.NewsletterRepository
import com.rgbmedia.zman.network.SearchRepository
import com.rgbmedia.zman.utils.LoginStatus
import kotlinx.coroutines.launch

class MainViewModel(private val menuRepository: MenuRepository,
                    private val newsletterRepository: NewsletterRepository,
                    private val searchRepository: SearchRepository,
                    private val loginRepository: LoginRepository) : ViewModel() {

    private var menuLiveData = MutableLiveData<List<MainMenuElement>>()
    private var showMenu = MutableLiveData(false)
    private var showLogin = MutableLiveData(false)
    private var showLogout = MutableLiveData(false)
    private var showLoading = MutableLiveData(false)
    private var webviewUrlString = MutableLiveData(HOMEPAGE_URL)
    private var selectedMenuItem = MutableLiveData(Pair(0, 0))
    private var oldSelectedMenuItem = Pair(0, 0)
    private var newsletterResponse = MutableLiveData("")
    private var newsletterPosition = Pair(0, 0)
    private var searchResponse = MutableLiveData(arrayOf<SearchResult>())
    private var searchPosition = Pair(0, 0)
    private var searchResultsVisible = MutableLiveData(false)
    private var loginData = MutableLiveData(LoginModel(LoginStatus.notLoggedIn, "", false, LoginData("", "", "")))
    private var loginError = MutableLiveData("")

    fun getLoginData() = loginData

    fun setLoginData(data: LoginModel) {
        loginData.value = data
    }

    fun getLoginError() = loginError

    fun setLoginError(error: String) {
        loginError.value = error
    }

    fun getMenu(): LiveData<List<MainMenuElement>> {
        return menuLiveData
    }

    fun getShowMenu(): LiveData<Boolean> = showMenu

    fun setShowMenu(show: Boolean) {
        showMenu.value = show
    }

    fun getShowLogin(): LiveData<Boolean> = showLogin

    fun setShowLogin(show: Boolean) {
        showLogin.value = show
    }

    fun getShowLogout() = showLogout

    fun setShowLogout(show: Boolean) {
        showLogout.value = show
    }

    fun getShowLoading() = showLoading

    fun setShowLoading(show: Boolean) {
        showLoading.value = show
    }

    fun getWebviewUrlString(): LiveData<String> = webviewUrlString

    fun setWebviewUrlString(urlString: String) {
        webviewUrlString.value = urlString
    }

    fun getSelectedMenuItem() : LiveData<Pair<Int, Int>> = selectedMenuItem

    fun setSelectedMenuItem(pair: Pair<Int, Int>) {
        selectedMenuItem.value = pair
    }

    fun getOldSelectedMenuItem() : Pair<Int, Int> {
        return oldSelectedMenuItem
    }

    fun setOldSelectedMenuItem(pair: Pair<Int, Int>) {
        oldSelectedMenuItem = pair
    }

    fun getNewsletterPosition(): Pair<Int, Int> = newsletterPosition

    fun setNewsletterPosition(position: Pair<Int, Int>) {
        newsletterPosition = position
    }

    fun getNewsletterResponse(): LiveData<String> {
        return newsletterResponse
    }

    fun getSearchPosition(): Pair<Int, Int> = searchPosition

    fun setSearchPosition(position: Pair<Int, Int>) {
        searchPosition = position
    }

    fun getSearchResponse(): LiveData<Array<SearchResult>> {
        return searchResponse
    }

    fun getSearchResultsVisible(): LiveData<Boolean> = searchResultsVisible

    fun setSearchResultsVisible(visible: Boolean) {
        searchResultsVisible.value = visible
    }

    fun sendEmail(email: String) {
        viewModelScope.launch {
            val response = newsletterRepository.sendEmail(email)

            newsletterResponse.value = response
        }
    }

    fun search(text: String) {
        viewModelScope.launch {
            val response = searchRepository.search(text)

            searchResponse.value = response.results
        }
    }

    fun loginWithFb(userId: String, userName: String, userEmail: String, userPicture: String, accessToken: String) {
        viewModelScope.launch {
            val response = loginRepository.loginWithFb(userId, userName, userEmail, userPicture, accessToken)

            if (response.status == "logged_in") {
                response.type = LoginStatus.loggedInWithFb
                loginData.value = response
            }
        }
    }

    fun loginWithTwitter(userEmail: String, screenName: String) {
        viewModelScope.launch {
            val response = loginRepository.loginWithTwitter(userEmail, screenName)

            if (response.status == "logged_in") {
                response.type = LoginStatus.loggedInWithTwitter
                loginData.value = response
            }
        }
    }

    init {
        viewModelScope.launch {
            val list = menuRepository.getMenu()

            val mainMenuElements = ArrayList<MainMenuElement>()

            list.forEach {
                val element = MainMenuElement(it, false)

                mainMenuElements.add(element)
            }

            menuLiveData.value = mainMenuElements
        }
    }
}

data class MainMenuElement(var item: MenuElement,
                           var expanded: Boolean)