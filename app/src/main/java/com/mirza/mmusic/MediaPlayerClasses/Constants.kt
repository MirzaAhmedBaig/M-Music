package com.mirza.mmusic.MediaPlayerClasses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.mirza.mmusic.R


/**
 * Created by mirza on 20/01/18.
 */
object Constants {
    interface ACTION {
        companion object {
            val MAIN_ACTION = "com.mirza.mmusic.action.main"
            val INIT_ACTION = "com.mirza.mmusic.action.init"
            val PREV_ACTION = "com.mirza.mmusic.action.prev"
            val PLAY_ACTION = "com.mirza.mmusic.action.play"
            val NEXT_ACTION = "com.mirza.mmusic.action.next"
            val STARTFOREGROUND_ACTION = "com.mirza.mmusic.action.startforeground"
            val STOPFOREGROUND_ACTION = "com.mirza.mmusic.action.stopforeground"
        }

    }

    fun getDefaultAlbumArt(context: Context, data: String): Bitmap? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(data)
        val picData = mediaMetadataRetriever.embeddedPicture
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bitmapI: Bitmap?
        if (picData != null) {
            var bitmapImage = BitmapFactory.decodeByteArray(picData, 0, picData.size)
            bitmapI = Bitmap.createScaledBitmap(bitmapImage, 300, 300, true)
        } else {
            bitmapI = BitmapFactory.decodeResource(context.resources,
                    R.drawable.music, options)
        }
        return bitmapI

    }

}