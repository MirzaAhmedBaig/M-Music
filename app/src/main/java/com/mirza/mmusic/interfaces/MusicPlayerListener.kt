package com.mirza.mmusic.interfaces

import com.mirza.mmusic.models.Audio

/**
 * Created by avantari on 11/27/17.
 */
interface MusicPlayerListener {
    fun onSongClick(audio: Audio, index: Int)
}