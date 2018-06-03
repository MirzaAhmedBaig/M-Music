package com.mirza.mmusic.MediaPlayerClasses

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
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
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener, HardButtonReceiver.HardButtonListener {
    override fun onPrevButtonPress() {
        skipToPrevious()
    }

    override fun onNextButtonPress() {
        skipToNext()
    }

    override fun onPlayPauseButtonPress() {
        if (mediaPlayer!!.isPlaying) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    private val TAG: String = MediaPlayerService::class.java.simpleName
    private var mediaPlayer: MediaPlayer? = null
    private var audioList: ArrayList<Audio>? = null
    private var audioIndex: Int = 0
    private var oldAudioIndex: Int = 0
    private val musicBind = MusicBinder()
    private var isRepeat: Boolean = false
    private var isPlaying: Boolean = false
    private var activeAudio: Audio? = null
    private var oldAudio: Audio? = null
    private var songTitle: String = ""
    private val NOTIFY_ID: Int = 1
    private var mediaPlayerControllerListener: MediaPlayerControllerListener? = null

    private var views: RemoteViews? = null
    private var bigViews: RemoteViews? = null

    private var appPreferences: AppPreferences? = null

    private var isAsyncCalled: Boolean = false
    private var soundLevel: Float = 0f

    override fun onCreate() {
        super.onCreate()
        callStateListener()
        registerBecomingNoisyReceiver()
        setHeadphoneReceiver()
        setHeadphoneReceiver21()

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
        removeAudioFocus()
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
        unregisterReceiver(mButtonReceiver)
        unregisterReceiver(becomingNoisyReceiver)
    }

    fun setSongList(audioList: ArrayList<Audio>) {
        this.audioList = audioList
        activeAudio = audioList[0]
        songTitle = activeAudio!!.title!!
        audioIndex = 0
    }

    fun setSong(audio: Audio, index: Int) {
        activeAudio = audio
        songTitle = activeAudio!!.title!!
        audioIndex = index
    }

    fun songClicked(data: String) {
        oldAudio = activeAudio
        oldAudioIndex = audioIndex
        initMediaPlayer(data)
    }

    fun initMediaPlayer(data: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSession!!.isActive = true
        }

        mediaPlayer!!.reset()
        Log.d(TAG, "Data to init : $data")
        try {
            mediaPlayer!!.setDataSource(data)
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer!!.stop()
        }
    }

    fun skipToNext() {
        Log.d(TAG, "skipToNext : $audioIndex")
        if (audioList == null)
            return
        oldAudio = activeAudio
        oldAudioIndex = audioIndex
        if (audioIndex == audioList!!.size - 2) {
            audioIndex = 0
            activeAudio = audioList!![audioIndex]
        } else {
            activeAudio = audioList!![++audioIndex]
        }
        stopMedia()
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data!!)
        mediaPlayer!!.prepareAsync()
        isAsyncCalled = true
    }

    fun skipToPrevious() {
        oldAudio = activeAudio
        oldAudioIndex = audioIndex
        if (audioIndex == 0) {
            audioIndex = audioList!!.size - 2
            activeAudio = audioList!![audioIndex]
        } else {
            activeAudio = audioList!![--audioIndex]
        }
        stopMedia()
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data!!)
        mediaPlayer!!.prepareAsync()
        isAsyncCalled = true
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSession!!.release()
        }
        mediaPlayer!!.stop()
        mediaPlayer!!.release()
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null)
            return START_STICKY
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }

        /*if (intent!!.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            showNotification()
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()

        } else */if (intent.action == Constants.ACTION.PREV_ACTION) {
            skipToPrevious()
            Log.i(TAG, "Clicked Previous")
        } else if (intent.action == Constants.ACTION.PLAY_ACTION) {
            if (mediaPlayer!!.isPlaying) {
                pausePlayer()
            } else {
                startPlayer()
            }
            Log.i(TAG, "Clicked Play")

        } else if (intent.action == Constants.ACTION.NEXT_ACTION) {
            skipToNext()
            Log.i(TAG, "Clicked Next")
        } else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            Log.i(TAG, "Received Stop Foreground Intent")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val style = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Notification.MediaStyle()
            } else {
                null
            }
            style!!.setMediaSession(mediaSession!!.sessionToken)
            notificationBuilder.setStyle(style)
        }
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

    override fun onAudioFocusChange(focusState: Int) {

        //Invoked when the audio focus of the system is updated.
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (isPlaying) {
                    startPlayer()
                    mediaPlayer!!.setVolume(1.0f, 1.0f)
                    isPlaying = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer!!.isPlaying) {
                    /* mediaPlayer!!.stop()*/
                    pausePlayer()
                    isPlaying = true
                }
                /*mediaPlayer!!.release()
                mediaPlayer = null*/
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying) {
                    isPlaying = true
                    pausePlayer()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(0.1f, 0.1f)
            }
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
        Log.d(TAG, "Playing Data : ${activeAudio!!.data}")
        if (!mediaPlayer!!.isPlaying) {
            if (isAsyncCalled) {
                mediaPlayer!!.start()
                updateNotificationView()
                mediaPlayerControllerListener!!.onPlayPause(1)
            } else {
                callPlayerAsync()
                mediaPlayerControllerListener!!.onPlayPause(1)
            }
        }
    }

    fun callPlayerAsync() {
        mediaPlayer!!.prepareAsync()
        isAsyncCalled = true
    }

    fun getActiveAudio(): Audio {
        return activeAudio!!
    }

    fun setAudioIndex(index: Int) {
        Log.d(TAG, "setAudioIndex1 : $audioIndex")
        audioIndex = index
        Log.d(TAG, "setAudioIndex2 : $audioIndex")
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

    /**
     * Handling other apps and telephony events
     */

    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    //AudioFocus
    private var audioManager: AudioManager? = null

    //Headphone buttons receiver
    private var mButtonReceiver: HardButtonReceiver? = null

    private var mediaSession: MediaSession? = null

    private fun setHeadphoneReceiver() {
        mButtonReceiver = HardButtonReceiver(this)

        // Create the intent filter the button receiver will handle
        val iF = IntentFilter(Intent.ACTION_MEDIA_BUTTON)
        iF.priority = IntentFilter.SYSTEM_HIGH_PRIORITY + 1

        // register the receiver
        registerReceiver(mButtonReceiver, iF)
        Log.v(TAG, "HeadsetExample: The Button Receiver has been registered")
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setHeadphoneReceiver21() {
        val callback = object : MediaSession.Callback() {
            override fun onPlay() {
                // Handle the play button
                super.onPlay()
                Log.d(TAG, "I am here1")
            }

            override fun onPause() {
                super.onPause()
                Log.d(TAG, "I am here1")
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                Log.d(TAG, "I am here2")
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                Log.d(TAG, "I am here3")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mediaSession = MediaSession(this@MediaPlayerService, TAG)
            mediaSession!!.setFlags(
                    MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            mediaSession!!.setCallback(callback)

            val state = PlaybackState.Builder()
            state.setActions(PlaybackState.ACTION_PLAY_PAUSE)
            state.setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
            val ps = state.build()
            mediaSession!!.setPlaybackState(ps)


        }
    }

    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                //if at least one call exists or the phone is ringing
                //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pausePlayer()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                startPlayer()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager!!.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE)
    }

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Headphone Action : ${intent.action}")
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pausePlayer()
//            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true
        }
        //Could not gain focus
        return false
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager!!.abandonAudioFocus(this)
    }
}