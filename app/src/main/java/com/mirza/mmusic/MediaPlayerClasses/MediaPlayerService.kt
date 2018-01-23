package com.mirza.mmusic.MediaPlayerClasses

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import com.mirza.mmusic.AppPreferences
import com.mirza.mmusic.R
import com.mirza.mmusic.activities.HomeActivity
import com.mirza.mmusic.interfaces.MediaPlayerControllerListener
import com.mirza.mmusic.models.Audio
import java.io.IOException


/**
 * Created by mirza on 19/01/18.
 */
class MediaPlayerService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private val LOG_TAG: String = MediaPlayerService::class.java.simpleName
    private var mediaPlayer: MediaPlayer? = null
    private var audioList: ArrayList<Audio>? = null
    private var audioIndex: Int = 0
    private var oldAudioIndex: Int = 0
    private val musicBind = MusicBinder()
    private var isRepeat: Boolean = false
    private var activeAudio: Audio? = null
    private var oldAudio: Audio? = null
    private var songTitle: String = ""
    private val NOTIFY_ID: Int = 1
    private var mediaPlayerControllerListener: MediaPlayerControllerListener? = null

    private var views: RemoteViews? = null
    private var bigViews: RemoteViews? = null

    private var appPreferences: AppPreferences? = null


    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(applicationContext)
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setWakeMode(applicationContext,
                PowerManager.PARTIAL_WAKE_LOCK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
        } else {
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    fun setSongList(audioList: ArrayList<Audio>) {
        this.audioList = audioList
        activeAudio = audioList[0]
        songTitle = activeAudio!!.title!!
        audioIndex = 0
    }

    fun songClicked(data: String) {
        oldAudio = activeAudio
        oldAudioIndex = audioIndex
        initMediaPlayer(data)
    }

    fun initMediaPlayer(data: String) {
        mediaPlayer!!.reset()
        try {
            mediaPlayer!!.setDataSource(data)
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer!!.stop()
        }
    }

    fun skipToNext() {
        if (audioList == null)
            return
        oldAudio = activeAudio
        oldAudioIndex = audioIndex
        if (audioIndex == audioList!!.size - 1) {
            audioIndex = 0
            activeAudio = audioList!![audioIndex]
        } else {
            activeAudio = audioList!![++audioIndex]
        }
        stopMedia()
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data!!)
        mediaPlayer!!.prepareAsync()
    }

    fun skipToPrevious() {
        oldAudio = activeAudio
        oldAudioIndex = audioIndex
        if (audioIndex == 0) {
            audioIndex = audioList!!.size - 1
            activeAudio = audioList!![audioIndex]
        } else {
            activeAudio = audioList!![--audioIndex]
        }
        stopMedia()
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data!!)
        mediaPlayer!!.prepareAsync()
    }

    fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /*if (intent!!.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            showNotification()
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()

        } else */if (intent!!.action == Constants.ACTION.PREV_ACTION) {
            skipToPrevious()
            Log.i(LOG_TAG, "Clicked Previous")
        } else if (intent.action == Constants.ACTION.PLAY_ACTION) {
            if (mediaPlayer!!.isPlaying)
                pausePlayer()
            else
                startPlayer()
            Log.i(LOG_TAG, "Clicked Play")
        } else if (intent.action == Constants.ACTION.NEXT_ACTION) {
            skipToNext()
            Log.i(LOG_TAG, "Clicked Next")
        } else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent")
            stopForeground(true)
            stopSelf()
            mediaPlayerControllerListener!!.onExit()

        }
        return START_STICKY
    }

    override fun onPrepared(mp: MediaPlayer?) {
        startPlayer()

        mediaPlayerControllerListener!!.onAudioChanged(audioIndex, activeAudio!!, oldAudioIndex, oldAudio!!)
        songTitle = activeAudio!!.title!!

        views = RemoteViews(packageName,
                R.layout.status_bar)
        bigViews = RemoteViews(packageName,
                R.layout.status_bar_expanded)
        updateNotificationView()
        appPreferences!!.saveLastAudio(activeAudio!!)

    }

    private fun updateNotificationView() {
        if (views == null) {
            views = RemoteViews(packageName,
                    R.layout.status_bar)
            bigViews = RemoteViews(packageName,
                    R.layout.status_bar_expanded)
        }
        bigViews!!.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this, activeAudio!!.data!!))
        views!!.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this, activeAudio!!.data!!))

        val notificationIntent = Intent(this, HomeActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        notificationIntent.action = Constants.ACTION.MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val previousIntent = Intent(this, MediaPlayerService::class.java)
        previousIntent.action = Constants.ACTION.PREV_ACTION
        val ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0)

        val playIntent = Intent(this, MediaPlayerService::class.java)
        playIntent.action = Constants.ACTION.PLAY_ACTION
        val pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0)

        val nextIntent = Intent(this, MediaPlayerService::class.java)
        nextIntent.action = Constants.ACTION.NEXT_ACTION
        val pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0)

        val closeIntent = Intent(this, MediaPlayerService::class.java)
        closeIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        val pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0)

        views!!.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)
        bigViews!!.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)

        views!!.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)
        bigViews!!.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)

        views!!.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)
        bigViews!!.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)

        views!!.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)
        bigViews!!.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)
        if (mediaPlayer!!.isPlaying) {
            views!!.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_n_pause)
            bigViews!!.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_n_pause)
        } else {
            views!!.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_n_play)
            bigViews!!.setImageViewResource(R.id.status_bar_play,
                    R.drawable.ic_n_play)

        }

        views!!.setTextViewText(R.id.status_bar_track_name, activeAudio!!.title)
        bigViews!!.setTextViewText(R.id.status_bar_track_name, activeAudio!!.title)

        views!!.setTextViewText(R.id.status_bar_artist_name, activeAudio!!.artist)
        bigViews!!.setTextViewText(R.id.status_bar_artist_name, activeAudio!!.artist)

        bigViews!!.setTextViewText(R.id.status_bar_album_name, activeAudio!!.album)


        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        notificationBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_letter_m)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.setCustomContentView(views)
            notificationBuilder.setCustomBigContentView(bigViews)
        }

        val notification: Notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_ONGOING_EVENT

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notification.contentView = views
            notification.bigContentView = bigViews
        }

        startForeground(NOTIFY_ID, notification)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (isRepeat) {
            startPlayer()
        } else {
            skipToNext()
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MediaPlayerService {
            return this@MediaPlayerService
        }
    }

    fun setIsRepeat(isRepeat: Boolean) {
        this.isRepeat = isRepeat
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer!!.currentPosition
    }

    fun isPlaying(): Boolean {
        return mediaPlayer!!.isPlaying
    }

    fun pausePlayer() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            updateNotificationView()
            mediaPlayerControllerListener!!.onPlayPause(0)
        }
    }

    fun seekTo(posn: Int) {
        mediaPlayer!!.seekTo(posn)
    }

    fun startPlayer() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            updateNotificationView()
            mediaPlayerControllerListener!!.onPlayPause(1)
        }
    }

    fun callPlayerAsync() {
        mediaPlayer!!.prepareAsync()
    }

    fun getActiveAudio(): Audio {
        return activeAudio!!
    }

    fun setAudioIndex(index: Int) {
        audioIndex = index
    }

    fun setActiveAudio(audio: Audio) {
        activeAudio = audio
    }

    fun getDuration(): Int {
        return mediaPlayer!!.duration
    }

    fun resetPlayer() {
        mediaPlayer!!.reset()
    }

    fun getAudioIndex(): Int {
        return audioIndex
    }

    fun setMediaPlayerControllerListener(mediaPlayerControllerListener: MediaPlayerControllerListener) {
        this.mediaPlayerControllerListener = mediaPlayerControllerListener

    }
}