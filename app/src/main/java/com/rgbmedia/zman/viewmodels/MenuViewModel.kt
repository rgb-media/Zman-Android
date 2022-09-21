package com.rgbmedia.zman.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rgbmedia.zman.models.MenuElement
import com.rgbmedia.zman.network.MenuRepository

class MenuViewModel : ViewModel() {

    private var liveData = MutableLiveData<List<MainMenuElement>>()

    fun getMenu(): LiveData<List<MainMenuElement>> {
        return liveData
    }

    init {
        MenuRepository.getMenu {
            val mainMenuElements = ArrayList<MainMenuElement>()

            it?.forEach {
                val element = MainMenuElement(it, false)

                mainMenuElements.add(element)
            }

            liveData.value = mainMenuElements
        }
    }
}

data class MainMenuElement(var item: MenuElement,
                           var expanded: Boolean)