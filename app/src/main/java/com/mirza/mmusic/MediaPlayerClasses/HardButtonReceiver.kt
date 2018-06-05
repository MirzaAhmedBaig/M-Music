package com.mirza.mmusic.MediaPlayerClasses

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.KeyEvent


/**
 * Created by mirza on 24/01/18.
 */
class HardButtonReceiver(private val mButtonListener: HardButtonListener?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "HardButtonReceiver: Button press received")
        if (mButtonListener != null) {
            /**
             * We abort the broadcast to prevent the event being passed down
             * to other apps (i.e. the Music app)
             */
            abortBroadcast()

            // Pull out the KeyEvent from the intent
            val key = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent

            // This is just some example logic, you may want to change this for different behaviour
            if (key.action == KeyEvent.ACTION_UP) {
                val keycode = key.keyCode

                // These are examples for detecting key presses on a Nexus One headset
                if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    mButtonListener.onNextButtonPress()
                } else if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    mButtonListener.onPrevButtonPress()
                } else if (keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                    mButtonListener.onPlayPauseButtonPress()
                }
            }
        }
    }

    interface HardButtonListener {
        fun onPrevButtonPress()
        fun onNextButtonPress()
        fun onPlayPauseButtonPress()
    }

    companion object {

        private val TAG = "gauntface"
    }
}