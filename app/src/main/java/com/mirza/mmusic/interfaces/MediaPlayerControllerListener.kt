package com.mirza.mmusic.interfaces

import com.mirza.mmusic.models.Audio

/**
 * Created by mirza on 20/01/18.
 */
interface MediaPlayerControllerListener {
    fun onAudioChanged(audioIndex: Int, audio: Audio, oldAudioIndex: Int, oldAudio: Audio)
    fun onPlayPause(status: Int)
    fun onExit()
}