package com.mirza.mmusic.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import com.mirza.mmusic.R
import com.mirza.mmusic.adapter.ViewPagerAdapter
import com.mirza.mmusic.fragments.MusicListFragment
import com.mirza.mmusic.getDp
import com.mirza.mmusic.interfaces.MusicPlayerListener
import com.mirza.mmusic.isFileExist
import com.mirza.mmusic.models.Audio
import com.mirza.mmusic.models.db.RealmFavAudio
import com.mirza.mmusic.models.db.RealmRecentAudio
import com.squareup.picasso.Picasso
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException
import java.net.URL


class HomeActivity : AppCompatActivity(), MusicPlayerListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {


    private var audioList = ArrayList<Audio>()
    private var recentList: ArrayList<Audio>? = null
    private var favList: ArrayList<Audio>? = null
    private var dataList: ArrayList<String>? = null
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null
    private val TAG = HomeActivity::class.java.simpleName

    var mediaPlayer: MediaPlayer? = null
    private var resumePosition: Int = 0
    private var audioIndex: Int = 0
    private var activeAudio: Audio? = null
    private var isRepeat: Boolean = false
    private var isFirst: Boolean = true

    private var allMusicFragment: MusicListFragment? = null
    private var favMusicFragment: MusicListFragment? = null
    private var recentMusicFragment: MusicListFragment? = null

    private val permsRequestCode = 200
    private val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE/*, Manifest.permission.ACCESS_FINE_LOCATION*/)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setUpBottomSheet()
        setSupportActionBar(toolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode)
        } else {
            LoadData().execute()
        }
    }


    override fun onBackPressed() {
        if (mBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            supportActionBar!!.show()
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.offscreenPageLimit = 2
        val adapter = ViewPagerAdapter(supportFragmentManager)
        allMusicFragment = MusicListFragment.newInstance(1, audioList)
        allMusicFragment!!.setUpMusicListener(this)
        favMusicFragment = MusicListFragment.newInstance(2, favList!!)
        favMusicFragment!!.setUpMusicListener(this)
        recentMusicFragment = MusicListFragment.newInstance(3, recentList!!)
        recentMusicFragment!!.setUpMusicListener(this)

        adapter.addFragment(allMusicFragment!!, "Songs")
        adapter.addFragment(favMusicFragment!!, "Favorite")
        adapter.addFragment(recentMusicFragment!!, "Recent")
        viewPager.adapter = adapter

    }

    private fun setUpBottomSheet() {
        down_arrow.bringToFront()
        maxLayout.alpha = 0f
        bottom_sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        mBottomSheetBehavior!!.isHideable = false
        mBottomSheetBehavior!!.skipCollapsed = false
        mBottomSheetBehavior!!.peekHeight = 200
        mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
        bottom_sheet.visibility = View.GONE


        mBottomSheetBehavior!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        viewpager!!.visibility = View.GONE
                        supportActionBar!!.hide()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        if (isFirst) {
                            isFirst = false
                            val x = viewpager.layoutParams as CoordinatorLayout.LayoutParams
                            x.bottomMargin = getDp(120)
                            viewpager.layoutParams = x
                        }
                        miniLayout!!.visibility = View.VISIBLE
                        viewpager.alpha = 1f
                        appBar.alpha = 1f

                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                supportActionBar!!.show()
                viewpager!!.visibility = View.VISIBLE
                maxLayout!!.alpha = slideOffset
                viewpager!!.alpha = (1 - slideOffset)
                appBar!!.alpha = (1 - slideOffset)


            }
        })

        miniLayout!!.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = miniLayout!!.layoutParams as RelativeLayout.LayoutParams
                mBottomSheetBehavior!!.peekHeight = params.height
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
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
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
                repeat_button.setImageResource(R.drawable.ic_repeat_disabled)
            } else {
                isRepeat = true
                repeat_button.setImageResource(R.drawable.ic_repeat)
            }
        }
        favorite_view.setOnClickListener {
            changeFavAudio()
        }

        songList_btn.setOnClickListener {
            viewpager.currentItem = 0
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED

        }

        lyrics_btn.setOnClickListener {

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mediaPlayer!!.seekTo(p0!!.progress)
            }

        })
    }

    @SuppressLint("Recycle")
    private fun loadAudio() {
        loadRecentFavAudio()

        val contentResolver = contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.count > 0) {
            audioList = ArrayList()
            dataList = ArrayList()
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                var time = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                var endTime: String
                var seconds: Int = ((java.lang.Long.parseLong(time) / 1000L) % 60).toInt()
                var minutes: Long = ((java.lang.Long.parseLong(time) / 1000L) - seconds) / 60L
                if (minutes >= 60) {
                    minutes %= 60
                    var hr: Int = (((java.lang.Long.parseLong(time) / 1000L) / 60) / 60).toInt()
                    endTime = "$hr:$minutes:$seconds"
                } else {
                    endTime = "$minutes:$seconds"
                }

                val newAudio = isFavAudioReturn(Audio(data, title, album, artist, endTime, java.lang.Long.parseLong(time), false, false))
                audioList.add(newAudio)
                dataList!!.add(data)
            }
        }
        cursor!!.close()
    }

    private fun loadRecentAudio() {
        val realm = Realm.getDefaultInstance()
        val list1 = realm.where(RealmRecentAudio::class.java).findAll()
        recentList = ArrayList()
        /*(0 until list1.size)
                .map {
                    isFavAudioReturn(Audio(list1[it]!!.data, list1[it]!!.title, list1[it]!!.album, list1[it]!!.artist, list1[it]!!.endTime, list1[it]!!.duration, false, true))
                }
                .forEach { recentList!!.add(it) }*/

        for (i in 0 until list1.size) {
            if (isFileExist(list1[i]!!.data)) {
                recentList!!.add(isFavAudioReturn(Audio(list1[i]!!.data, list1[i]!!.title, list1[i]!!.album, list1[i]!!.artist, list1[i]!!.endTime, list1[i]!!.duration, false, true)))
            } else {
                realm.beginTransaction()
                list1[i]!!.deleteFromRealm()
                realm.commitTransaction()
            }
        }
    }

    private fun loadFavAudio() {
        val realm = Realm.getDefaultInstance()
        val list2 = realm.where(RealmFavAudio::class.java).findAll()
        favList = ArrayList()
        for (i in 0 until list2.size) {
            if (isFileExist(list2[i]!!.data)) {
                favList!!.add(Audio(list2[i]!!.data, list2[i]!!.title, list2[i]!!.album, list2[i]!!.artist, list2[i]!!.endTime, list2[i]!!.duration, false, true))
            } else {
                realm.beginTransaction()
                list2[i]!!.deleteFromRealm()
                realm.commitTransaction()
            }
        }
        /*(0 until list2.size)
                .map {
                    if (isFileExist(list2[it]!!.data))
                        Audio(list2[it]!!.data, list2[it]!!.title, list2[it]!!.album, list2[it]!!.artist, list2[it]!!.endTime, list2[it]!!.duration, false, true)
                    else
                        return
                }
                .forEach {
                    favList!!.add(it)

                }*/

    }

    private fun loadRecentFavAudio() {
        loadFavAudio()
        loadRecentAudio()
    }

    private fun isFavAudioReturn(audio: Audio): Audio {
        for (i in 0 until favList!!.size) {
            if (audio.data == favList!![i].data) {
                audio.isFav = true
                break
            }
        }
        return audio
    }

    private fun changeFavAudio() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val index = dataList!!.indexOf(activeAudio!!.data)
        if (activeAudio!!.isFav) {
            activeAudio!!.isFav = false
            audioList[index].isFav = false
            favorite_view.setImageResource(R.drawable.ic_fav_emp)
            val favAudio = realm.where(RealmFavAudio::class.java).equalTo("data", activeAudio!!.data).findFirst()
            favAudio!!.deleteFromRealm()
            recentMusicFragment!!.makeFaveoOrUnFavAudio(activeAudio!!.data!!, false)
        } else {
            activeAudio!!.isFav = true
            audioList[index].isFav = true
            val newFav = RealmFavAudio()
            newFav.data = activeAudio!!.data!!
            newFav.title = activeAudio!!.title!!
            newFav.album = activeAudio!!.album!!
            newFav.artist = activeAudio!!.artist!!
            newFav.endTime = activeAudio!!.endTime
            newFav.duration = activeAudio!!.duration
            newFav.playing = false

            realm.copyToRealmOrUpdate(newFav)
            favorite_view.setImageResource(R.drawable.ic_fav_filled)
            recentMusicFragment!!.makeFaveoOrUnFavAudio(activeAudio!!.data!!, true)
        }
        realm.commitTransaction()
        favMusicFragment!!.addOrRemoveAudio(activeAudio!!)


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
        startTime.text = "00:00"
//        endTime.text = activeAudio!!.endTime

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

//                        seekBar.progress = (mediaPlayer!!.currentPosition)
//                        progressBar.progress = mediaPlayer!!.currentPosition
                        animateSeek((mediaPlayer!!.currentPosition))
                        animateProgress((mediaPlayer!!.currentPosition))
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

            allMusicFragment!!.pauseAudio(activeAudio!!, false)
            favMusicFragment!!.pauseAudio(activeAudio!!, false)
            recentMusicFragment!!.pauseAudio(activeAudio!!, false)
        }
    }

    private fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
            play.setBackgroundResource(R.drawable.ic_pause_button)
            play_m.setBackgroundResource(R.drawable.ic_pause_button_small)

            allMusicFragment!!.pauseAudio(activeAudio!!, true)
            favMusicFragment!!.pauseAudio(activeAudio!!, true)
            recentMusicFragment!!.pauseAudio(activeAudio!!, true)
        }
    }

    private fun skipToNext() {
        val oldAudio = activeAudio!!
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
        allMusicFragment!!.updatePlayingStatus(activeAudio!!, oldAudio)
        favMusicFragment!!.updatePlayingStatus(activeAudio!!, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(activeAudio!!, oldAudio)
    }

    private fun skipToPrevious() {
        val oldAudio = activeAudio!!
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
        allMusicFragment!!.updatePlayingStatus(activeAudio!!, oldAudio)
        favMusicFragment!!.updatePlayingStatus(activeAudio!!, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(activeAudio!!, oldAudio)
    }

    private fun updatePlayerData(audio: Audio) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        val recentAudio = RealmRecentAudio()
        recentAudio.data = audio.data!!
        recentAudio.title = audio.title!!
        recentAudio.album = audio.album!!
        recentAudio.artist = audio.artist!!
        recentAudio.endTime = audio.endTime
        recentAudio.duration = audio.duration
        recentAudio.playing = audio.playing

        realm.copyToRealmOrUpdate(recentAudio)
        realm.commitTransaction()

        recentMusicFragment!!.updateAudio(audio)


        songName.text = audio.title
        songTitle.text = audio.title
        smallSongAartist.text = audio.artist

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(audio.data)
        val data = mediaMetadataRetriever.embeddedPicture
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            var bitmapI = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
            smallThumbnail.setImageBitmap(bitmapI)
            songThumbnail.setImageBitmap(bitmap)
        } else {
            Log.d(TAG, "Audio don't has image :")
            Picasso.with(this).load(R.drawable.music).resize(100, 100).into(smallThumbnail)
            Picasso.with(this).load(R.drawable.music).into(songThumbnail)
        }

        startTime.text = "00:00"
        endTime.text = audio.endTime

        seekBar.progress = 0
        progressBar.progress = 0
        seekBar.max = audio.duration.toInt()
        progressBar.max = audio.duration.toInt()
        activeAudio = audio
        Log.d(TAG, "Audio.fav is : ${audio.isFav}")
        if (audio.isFav) {
            favorite_view.setImageResource(R.drawable.ic_fav_filled)
        } else {
            favorite_view.setImageResource(R.drawable.ic_fav_emp)
        }
//        isFav = audio.isFav

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


    override fun onSongClick(audio: Audio, index: Int) {
        val oldAudio = activeAudio!!
        audioIndex = index
        updatePlayerData(audio)
        if (bottom_sheet.visibility == View.GONE) {
            viewpager!!.visibility = View.GONE
            bottom_sheet.visibility = View.VISIBLE
            maxLayout!!.alpha = 1f
            supportActionBar!!.hide()
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        initMediaPlayer(audio.data)
        mediaPlayer!!.prepareAsync()
        allMusicFragment!!.updatePlayingStatus(audio, oldAudio)
        favMusicFragment!!.updatePlayingStatus(audio, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(audio, oldAudio)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            200 -> {
                val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                val cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (storageAccepted) {
                    LoadData().execute()
                } else {
                    Toast.makeText(this, "We cannot continue without permission", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    open inner class LoadData : AsyncTask<URL, Int, Long>() {
        override fun doInBackground(vararg p0: URL?): Long {
            loadAudio()
            return 0
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Long?) {
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
            music_load_progress.visibility = View.GONE
        }
    }

    private fun animateProgress(value: Int) {
        val anim = ObjectAnimator.ofInt(progressBar, "progress", value)
        anim.duration = 1000
        anim.start()
    }

    private fun animateSeek(value: Int) {
        val anim = ObjectAnimator.ofInt(seekBar, "progress", value)
        anim.duration = 1000
        anim.start()
    }

}