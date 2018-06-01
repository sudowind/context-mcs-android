package com.example.wangfeng.csapp.dummy

import android.util.Log
import com.example.wangfeng.csapp.MainAppliaction
import com.example.wangfeng.csapp.Utils
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.ArrayList

object QuestionContent {

    /**
     * An array of sample (question) items.
     */
    var ITEMS:MutableList<QuestionItem> = ArrayList<QuestionItem>()

    /**
     * A map of sample (question) items, by ID.
     */
    var ITEM_MAP:MutableMap<String, QuestionItem> = HashMap<String, QuestionItem>()

    init{
        // Add some sample items.
    }

    fun addItem(item:QuestionItem) {
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }

    /**
     * A question item representing a piece of content.
     */
    class QuestionItem( val id:String,  val name:String, val deadline:String,
                     val uqId:String, val status:String, val type:String) {

        override fun toString():String {
            return name
        }
    }

}
