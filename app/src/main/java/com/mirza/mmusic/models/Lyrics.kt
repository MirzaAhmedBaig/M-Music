package com.mirza.mmusic.models

import com.google.firebase.database.IgnoreExtraProperties
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


@IgnoreExtraProperties
open class Lyrics : RealmObject {
    @PrimaryKey
    var title: String = ""
    var artists: String = ""
    var url: String = ""

    constructor() {
    }

    constructor(title: String, artists: String, url: String) {
        this.title = title
        this.artists = artists
        this.url = url
    }

}