package com.mirza.mmusic

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mirza.mmusic.models.Audio

/**
 * Created by mirza on 20/01/18.
 */
class AppPreferences(context: Context) {
    private var _sharedPrefs: SharedPreferences
    private var _prefsEditor: SharedPreferences.Editor

    private val APP_SHARED_PREFS: String = "com.mirza.mmusic.preferences"

    private val LAST_AUDIO = "com.mirza.mmusic.lastmusic"


    private val TAG = AppPreferences::class.java.simpleName

    init {
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,
                Activity.MODE_PRIVATE)
        this._prefsEditor = _sharedPrefs.edit()
        this._prefsEditor.apply()
    }

    fun saveLastAudio(audio: Audio) {
        val userString = Gson().toJson(audio)
        _prefsEditor.putString(LAST_AUDIO, userString)
        _prefsEditor.commit()
    }

    fun getLastAudio(): Audio? {
        val userData = _sharedPrefs.getString(LAST_AUDIO, null)
        if (userData != null)
            return Gson().fromJson<Audio>(userData, Audio::class.java)
        else
            return null
    }


}