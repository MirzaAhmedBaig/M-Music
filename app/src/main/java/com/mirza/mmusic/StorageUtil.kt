package com.mirza.mmusic

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mirza.mmusic.models.Audio


/**
 * Created by MIRZA on 19/10/17.
 */
class StorageUtil(private val context: Context) {

    private val STORAGE = " com.mirza.mmusic.STORAGE"
    private var preferences: SharedPreferences? = null

    fun storeAudio(arrayList: ArrayList<Audio>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)

        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("audioArrayList", json)
        editor.apply()
    }

    fun loadAudio(): ArrayList<Audio> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences!!.getString("audioArrayList", null)
        val type = object : TypeToken<ArrayList<Audio>>() {

        }.type
        return gson.fromJson(json, type)
    }

    fun storeAudioIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.putInt("audioIndex", index)
        editor.apply()
    }

    fun loadAudioIndex(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences!!.getInt("audioIndex", -1)//return -1 if no data found
    }

    fun clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.clear()
        editor.commit()
    }
}