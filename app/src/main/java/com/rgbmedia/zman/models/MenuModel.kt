package com.rgbmedia.zman.models

import java.io.Serializable

data class MenuElement(var title:String,
                       var link:String,
                       var type:String,
                       var icon:String,
                       var items:Array<MenuItem>): Serializable

data class MenuItem(var title:String,
                    var link:String,
                    var type:String,
                    var icon:String): Serializable