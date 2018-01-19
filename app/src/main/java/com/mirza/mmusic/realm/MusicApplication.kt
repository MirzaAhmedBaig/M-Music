package com.mirza.mmusic.realm

import android.app.Application
import io.realm.Realm

/**
 * Created by mirza on 16/01/18.
 */
class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}