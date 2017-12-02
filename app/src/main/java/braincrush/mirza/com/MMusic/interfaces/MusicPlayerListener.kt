package braincrush.mirza.com.MMusic.interfaces

import braincrush.mirza.com.MMusic.models.Audio

/**
 * Created by avantari on 11/27/17.
 */
interface MusicPlayerListener {
    fun onSongClick(audio: Audio, index: Int)
}