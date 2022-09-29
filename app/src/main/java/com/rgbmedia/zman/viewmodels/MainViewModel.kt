package com.rgbmedia.zman.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rgbmedia.zman.HOMEPAGE_URL
import com.rgbmedia.zman.models.MenuElement
import com.rgbmedia.zman.network.MenuRepository
import kotlinx.coroutines.launch

class MainViewModel(private val menuRepository: MenuRepository) : ViewModel() {

    private var menuLiveData = MutableLiveData<List<MainMenuElement>>()
    private var showMenu = MutableLiveData(false)
    private var webviewUrlString = MutableLiveData(HOMEPAGE_URL)
    private var selectedMenuItem = MutableLiveData(Pair(0, 0))
    private var oldSelectedMenuItem = Pair(0, 0)

    fun getMenu(): LiveData<List<MainMenuElement>> {
        return menuLiveData
    }

    fun getShowMenu(): LiveData<Boolean> {
        return showMenu
    }

    fun setShowMenu(show: Boolean) {
        showMenu.value = show
    }

    fun getWebviewUrlString(): LiveData<String> {
        return webviewUrlString
    }

    fun setWebviewUrlString(urlString: String) {
        webviewUrlString.value = urlString
    }

    fun getSelectedMenuItem() : LiveData<Pair<Int, Int>> {
        return selectedMenuItem
    }

    fun setSelectedMenuItem(pair: Pair<Int, Int>) {
        selectedMenuItem.value = pair
    }

    fun getOldSelectedMenuItem() : Pair<Int, Int> {
        return oldSelectedMenuItem
    }

    fun setOldSelectedMenuItem(pair: Pair<Int, Int>) {
        oldSelectedMenuItem = pair
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