package braincrush.mirza.com.MMusic.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.widget.Toast
import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.adapter.ViewPagerAdapter
import braincrush.mirza.com.MMusic.fragments.MusicListFragment
import braincrush.mirza.com.MMusic.interfaces.MusicPlayerListener
import braincrush.mirza.com.MMusic.models.Audio
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException


class HomeActivity : AppCompatActivity(), MusicPlayerListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {


    private var audioList = ArrayList<Audio>()
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private val TAG = HomeActivity::class.java.simpleName

    var mediaPlayer: MediaPlayer? = null
    private var resumePosition: Int = 0
    private var audioIndex: Int = 0
    private var activeAudio: Audio? = null
    private var isRepeat: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        loadAudio()
        if (audioList.size != 0) {
            setupViewPager(viewpager)
            setUpBottomSheet()
            setUpListeners()
            tabs.setupWithViewPager(viewpager)

            activeAudio = audioList.elementAt(audioIndex)
            initMediaPlayer(activeAudio!!.data)
        } else {
            bottom_sheet.visibility = View.GONE
            noSong_text.visibility = View.VISIBLE
            tabs.addTab(tabs.newTab().setText("Songs"))
            tabs.addTab(tabs.newTab().setText("Favorite"))
            tabs.addTab(tabs.newTab().setText("Recent"))
        }

//        requestPermission()
    }

    override fun onBackPressed() {
        if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            supportActionBar!!.show()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        val musicListFragment1 = MusicListFragment.newInstance(1, audioList)
        musicListFragment1.setUpMusicListener(this)
        val musicListFragment2 = MusicListFragment.newInstance(2, audioList)
        musicListFragment2.setUpMusicListener(this)
        val musicListFragment3 = MusicListFragment.newInstance(3, audioList)
        musicListFragment3.setUpMusicListener(this)

        adapter.addFragment(musicListFragment1, "Songs")
        adapter.addFragment(musicListFragment2, "Favorite")
        adapter.addFragment(musicListFragment3, "Recent")
        viewPager.adapter = adapter

    }

    private fun setUpBottomSheet() {
        down_arrow.bringToFront()
        maxLayout.alpha = 0f
        bottom_sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        mBottomSheetBehavior.isHideable = false
        mBottomSheetBehavior.skipCollapsed = false
        mBottomSheetBehavior.peekHeight = 200
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottom_sheet.visibility = View.GONE


        mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    viewpager!!.visibility = View.GONE
                    supportActionBar!!.hide()
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    miniLayout!!.visibility = View.VISIBLE
                    viewpager.alpha = 1f
                    appBar.alpha = 1f
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                supportActionBar!!.show()
                viewpager!!.visibility = View.VISIBLE
                Log.d(ContentValues.TAG, "Slide Offset :" + slideOffset)
                maxLayout!!.alpha = slideOffset
                viewpager!!.alpha = (1 - slideOffset)
                appBar!!.alpha = (1 - slideOffset)


            }
        })

        miniLayout!!.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = miniLayout!!.layoutParams as RelativeLayout.LayoutParams
                mBottomSheetBehavior.peekHeight = params.height
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    miniLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    miniLayout!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }

            }
        }
        )
    }

    private fun setUpListeners() {
        down_arrow.setOnClickListener {
            miniLayout!!.visibility = View.VISIBLE
            viewpager.alpha = 1f
            appBar.alpha = 1f
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        play_view.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pauseMedia()

            } else {
                playMedia()
            }

        }
        next_view.setOnClickListener {
            skipToNext()
        }
        previous_view.setOnClickListener {
            skipToPrevious()
        }

        play_view_m.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pauseMedia()

            } else {
                resumeMedia()
            }

        }
        next_view_m.setOnClickListener {
            skipToNext()
        }
        previous_view_m.setOnClickListener {
            skipToPrevious()
        }


        play.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pauseMedia()

            } else {
                playMedia()
            }

        }
        next.setOnClickListener {
            skipToNext()
        }
        previous.setOnClickListener {
            skipToPrevious()
        }

        play_m.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pauseMedia()

            } else {
                resumeMedia()
            }

        }
        next_m.setOnClickListener {
            skipToNext()
        }
        previous_m.setOnClickListener {
            skipToPrevious()
        }

        repeat_button.setOnClickListener {
            if (isRepeat) {
                isRepeat = false
                repeat_button.setBackgroundResource(R.drawable.ic_repeat_disabled)
            } else {
                isRepeat = true
                repeat_button.setBackgroundResource(R.drawable.ic_repeat)
            }
        }
        favorite_view.setOnClickListener {
            if (isRepeat) {
                favorite_view.setBackgroundResource(R.drawable.ic_fav_filled)
            } else {
                favorite_view.setBackgroundResource(R.drawable.ic_fav_emp)
            }
        }
    }

    @SuppressLint("Recycle")
    private fun loadAudio() {
        val contentResolver = contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.count > 0) {
            audioList = ArrayList()
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))

                // Save to audioList
                audioList.add(Audio(data, title, album, artist, false))
            }
        }
        cursor!!.close()
    }

    private fun initMediaPlayer(data: String?) {
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer()//new MediaPlayer instance

        //Set up MediaPlayer event listeners
        mediaPlayer!!.setOnCompletionListener(this)
//        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
//        mediaPlayer!!.setOnBufferingUpdateListener(this)
//        mediaPlayer!!.setOnSeekCompleteListener(this)
//        mediaPlayer!!.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer!!.reset()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
        } else {
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        try {
            // Set the data source to the mediaFile location
            mediaPlayer!!.setDataSource(data)
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer!!.stop()

        }

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(data)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        var seconds: Int = ((java.lang.Long.parseLong(durationStr) / 1000L) % 60).toInt()
        var minutes: Long = ((java.lang.Long.parseLong(durationStr) / 1000L) - seconds) / 60L
        if (minutes >= 60) {
            minutes %= 60
            var hr: Int = (((java.lang.Long.parseLong(durationStr) / 1000L) / 60) / 60).toInt()
            startTime.text = "00:00:00"
            endTime.text = "$hr:$minutes:$seconds"
        } else {
            startTime.text = "00:00"
            endTime.text = "$minutes:$seconds"
        }

//        mediaPlayer!!.prepareAsync()
    }

    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            play.setBackgroundResource(R.drawable.ic_pause_button)
            play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
            val handler = Handler()
            val runnable = object : Runnable {
                override fun run() {
                    if (mediaPlayer!!.currentPosition > 0) {

                        var seconds: Int = ((mediaPlayer!!.currentPosition / 1000) % 60)
                        var minutes: Int = ((mediaPlayer!!.currentPosition / 1000) - seconds) / 60
                        if (minutes >= 60) {
                            minutes %= 60
                            var hr: Int = (((mediaPlayer!!.currentPosition / 1000) / 60) / 60)
                            startTime.text = "$hr:$minutes:$seconds"
                        } else {
                            startTime.text = "$minutes:$seconds"
                        }

                        seekBar.progress = (mediaPlayer!!.currentPosition)
                        progressBar.progress = mediaPlayer!!.currentPosition
                        Log.d(TAG, "Current Position : ${seekBar.progress}")
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(runnable)
        }
    }

    private fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            play.setBackgroundResource(R.drawable.ic_play_button)
            play_m.setBackgroundResource(R.drawable.ic_play_button_small)
        }
    }

    private fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
            play.setBackgroundResource(R.drawable.ic_play_button)
            play_m.setBackgroundResource(R.drawable.ic_play_button_small)
        }
    }

    private fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
            play.setBackgroundResource(R.drawable.ic_pause_button)
            play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        }
    }

    private fun skipToNext() {

        if (audioIndex == audioList.size - 1) {
            //if last in playlist
            audioIndex = 0
            activeAudio = audioList[audioIndex]
        } else {
            //get next in playlist
            activeAudio = audioList[++audioIndex]
        }


        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data)
        mediaPlayer!!.prepareAsync()
        play.setBackgroundResource(R.drawable.ic_pause_button)
        play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        updatePlayerData(activeAudio!!)
    }

    private fun skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size - 1
            activeAudio = audioList[audioIndex]
        } else {
            //get previous in playlist
            activeAudio = audioList[--audioIndex]
        }


        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data)
        mediaPlayer!!.prepareAsync()
        play.setBackgroundResource(R.drawable.ic_pause_button)
        play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        updatePlayerData(activeAudio!!)
    }

    private fun updatePlayerData(audio: Audio) {
        songName.text = audio.title
        songTitle.text = audio.title
        smallSongAartist.text = audio.artist

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(audio.data)
        val data = mediaMetadataRetriever.embeddedPicture
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            smallThumbnail.setImageBitmap(bitmap)
            songThumbnail.setImageBitmap(bitmap)
        } else {
            smallThumbnail.setBackgroundResource(R.drawable.music)
            songThumbnail.setBackgroundResource(R.drawable.music)
        }
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        var seconds: Int = ((java.lang.Long.parseLong(durationStr) / 1000L) % 60).toInt()
        var minutes: Long = ((java.lang.Long.parseLong(durationStr) / 1000L) - seconds) / 60L
        if (minutes >= 60) {
            minutes %= 60
            var hr: Int = (((java.lang.Long.parseLong(durationStr) / 1000L) / 60) / 60).toInt()
            startTime.text = "00:00:00"
            endTime.text = "$hr:$minutes:$seconds"
        } else {
            startTime.text = "00:00"
            endTime.text = "$minutes:$seconds"
        }
        seekBar.max = mediaPlayer!!.duration
        progressBar.max = mediaPlayer!!.duration
        Log.d(TAG, "Max duration : ${seekBar.max}")

    }

    override fun onPrepared(p0: MediaPlayer?) {
        playMedia()


    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (isRepeat) {
            playMedia()
        } else {
            skipToNext()
        }
    }


    override fun onSongClick(audio: Audio) {

        updatePlayerData(audio)
        if (bottom_sheet.visibility == View.GONE) {
            viewpager!!.visibility = View.GONE
            bottom_sheet.visibility = View.VISIBLE
            maxLayout!!.alpha = 1f
            supportActionBar!!.hide()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        initMediaPlayer(audio.data)
        mediaPlayer!!.prepareAsync()
    }


    private fun requestPermission() {
        Log.d("In Request permission", "asdasdasd")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, Array(1) { Manifest.permission.READ_EXTERNAL_STORAGE }
                        , 23)
            }

        } else {
            loadAudio()
            setupViewPager(viewpager)
            tabs.setupWithViewPager(viewpager)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.d("Request:", "number3")


        //If permission is granted
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAudio()
            setupViewPager(viewpager)
            tabs.setupWithViewPager(viewpager)
        } else {
            Toast.makeText(this, "We cannot continue without permission", Toast.LENGTH_LONG).show();
        }
    }
}
