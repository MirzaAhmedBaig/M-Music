package com.mirza.mmusic.models.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by mirza on 17/01/18.
 */
open class RealmRecentAudio : RealmObject() {
    @PrimaryKey
    var data: String = ""
    var title: String = ""
    var album: String = ""
    var artist: String = ""
    var endTime: String = ""
    var duration: Long = 0
    var playing: Boolean = false
}