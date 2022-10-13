package com.rgbmedia.zman.models

import java.io.Serializable

data class SearchModel(var status:String,
                       var results:Array<SearchResult>): Serializable

data class SearchResult(var title:String,
                    var name:String,
                    var taxonomy:String,
                    var link:String): Serializable
