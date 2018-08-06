package com.mirza.mmusic.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.*
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mirza.mmusic.AppPreferences
import com.mirza.mmusic.MediaPlayerClasses.MediaPlayerService
import com.mirza.mmusic.R
import com.mirza.mmusic.adapter.PlayerPagerAdapter
import com.mirza.mmusic.adapter.ViewPagerAdapter
import com.mirza.mmusic.extensions.*
import com.mirza.mmusic.fragments.LyricsFragment
import com.mirza.mmusic.fragments.MusicListFragment
import com.mirza.mmusic.fragments.SongInfoFragment
import com.mirza.mmusic.interfaces.MediaPlayerControllerListener
import com.mirza.mmusic.interfaces.MusicPlayerListener
import com.mirza.mmusic.models.Audio
import com.mirza.mmusic.models.Lyrics
import com.mirza.mmusic.models.db.RealmFavAudio
import com.mirza.mmusic.models.db.RealmRecentAudio
import com.squareup.picasso.Picasso
import io.realm.Realm
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_home.*
import java.net.URL


class HomeActivity : AppCompatActivity(), MusicPlayerListener, MediaPlayerControllerListener {


    private val TAG = HomeActivity::class.java.simpleName

    private var audioList = ArrayList<Audio>()
    private var recentList: ArrayList<Audio>? = null
    private var favList: ArrayList<Audio>? = null
    private var dataList: ArrayList<String>? = null
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null

    private var resumePosition: Int = 0
    private var isRepeat: Boolean = false
    private var isFirst: Boolean = true

    private var currentIndex: Int = 0

    private var allMusicFragment: MusicListFragment? = null
    private var favMusicFragment: MusicListFragment? = null
    private var recentMusicFragment: MusicListFragment? = null
    private var songInfoFragment: SongInfoFragment? = null
    private var lyricsFragment: LyricsFragment? = null

    private val permsRequestCode = 200
    private val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE/*, Manifest.permission.ACCESS_FINE_LOCATION*/)

    private var mediaPlayerService: MediaPlayerService? = null
    private var playIntent: Intent? = null
    private var musicBound: Boolean = false


    private val handler = Handler()
    private var runnable: Runnable? = null

    private var appPreferences: AppPreferences? = null
    private var musicConnection: ServiceConnection? = null
    private var isLoaded = false

    private var filteredColor = LightingColorFilter(Color.parseColor("#000000"), Color.WHITE)
    private var whiteFilteredColor = LightingColorFilter(Color.parseColor("#000000"), Color.WHITE)

    private val mDatabase by lazy {
        FirebaseDatabase.getInstance().reference
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_home)
        hideShowMainViews(View.GONE)
        tabs.addTab(tabs.newTab().setText("Songs"))
        tabs.addTab(tabs.newTab().setText("Favorite"))
        tabs.addTab(tabs.newTab().setText("Recent"))
        setAppPreferences()
        setUpBottomSheet()
        setSupportActionBar(toolbar)

        grabLyricsData()
    }

    override fun onResume() {
        if (runnable != null) {
            handler.post(runnable)
        } else
            doMediaTimeSeekChanges()
        super.onResume()
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(perms, permsRequestCode)
            } else {
                LoadData().execute()
            }

        }
    }

    override fun onDestroy() {
        if (playIntent != null)
            stopService(playIntent)
        if (mediaPlayerService != null) {
            unbindService(musicConnection)
        }
        mediaPlayerService = null
        super.onDestroy()
    }


    private fun setAppPreferences() {
        appPreferences = AppPreferences(this)
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
                maxLayout.bringToFront()
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        viewpager!!.visibility = View.GONE
                        supportActionBar!!.hide()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        miniLayout.bringToFront()
                        if (isFirst) {
                            isFirst = false
                            /*val x = viewpager.layoutParams as CoordinatorLayout.LayoutParams
                            x.bottomMargin = getDp(120)
                            viewpager.layoutParams = x*/
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
                miniLayout!!.alpha = 1 - slideOffset
                viewpager!!.alpha = (1 - slideOffset)
                appBar!!.alpha = (1 - slideOffset)
            }
        })

        miniLayout!!.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = miniLayout!!.layoutParams as RelativeLayout.LayoutParams
                mBottomSheetBehavior!!.peekHeight = params.height
                miniLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)

            }
        }
        )

        miniLayout.setOnClickListener {
            if (mBottomSheetBehavior!!.state != BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setUpPlayerPager(audio: Audio?) {
        val adapter = PlayerPagerAdapter(supportFragmentManager)
        if (audio != null) {
            songInfoFragment = SongInfoFragment.newInstance(audio)
            lyricsFragment = LyricsFragment.newInstance(audio)
        } else {
            songInfoFragment = SongInfoFragment()
            lyricsFragment = LyricsFragment()
        }
        adapter.addFragment(songInfoFragment!!, "details")
        adapter.addFragment(lyricsFragment!!, "lyrics")

        playerPager.adapter = adapter

        playerPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (currentIndex == position) {
                    if (currentIndex == 0) {
                        w_indicator_one.scaleX = 2f - positionOffset
                        w_indicator_one.scaleY = 2f - positionOffset

                        w_indicator_two.scaleX = 1f + positionOffset
                        w_indicator_two.scaleY = 1f + positionOffset
                    }
                } else if (currentIndex > position) {
                    if (currentIndex == 1) {
                        w_indicator_one.scaleX = 2f - positionOffset
                        w_indicator_one.scaleY = 2f - positionOffset

                        w_indicator_two.scaleX = 1f + positionOffset
                        w_indicator_two.scaleY = 1f + positionOffset
                    }
                }
                updateIndicator()
            }

            override fun onPageSelected(position: Int) {
                currentIndex = position
                setDotIndicator(position)
                if (position == 0) {
                    songThumbnail.visibility = View.VISIBLE
                } else {
                    songThumbnail.visibility = View.INVISIBLE
                }
            }

        })
    }

    fun setDotIndicator(position: Int) {
        when (position) {
            0 -> {
                w_indicator_one.scaleX = 2f
                w_indicator_one.scaleY = 2f

                w_indicator_two.scaleX = 1f
                w_indicator_two.scaleY = 1f
                updateIndicator()
            }
            1 -> {
                w_indicator_one.scaleX = 1f
                w_indicator_one.scaleY = 1f

                w_indicator_two.scaleX = 2f
                w_indicator_two.scaleY = 2f
                updateIndicator()
            }
        }
    }

    private fun updateIndicator() {
        w_indicator_one.invalidate()
        w_indicator_two.invalidate()
    }

    private fun setUpListeners() {
        down_arrow.setOnClickListener {
            miniLayout!!.visibility = View.VISIBLE
            viewpager.alpha = 1f
            appBar.alpha = 1f
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        play_view.setOnClickListener {
            if (mediaPlayerService!!.isPlaying()) {
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
            if (mediaPlayerService!!.isPlaying()) {
                pauseMedia()
            } else {
//                resumeMedia()
                playMedia()
            }
        }

        next_view_m.setOnClickListener {
            skipToNext()
        }
        previous_view_m.setOnClickListener {
            skipToPrevious()
        }

        play.setOnClickListener {
            if (mediaPlayerService!!.isPlaying()) {
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
            if (mediaPlayerService!!.isPlaying()) {
                pauseMedia()
            } else {
//                resumeMedia()
                playMedia()
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
                repeat_button.colorFilter = whiteFilteredColor
            } else {
                isRepeat = true
                repeat_button.colorFilter = filteredColor
            }
            mediaPlayerService!!.setIsRepeat(isRepeat)
        }
        favorite_view.setOnClickListener {
            changeFavAudio()
        }

        songList_btn.setOnClickListener {
            viewpager.currentItem = 0
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        lyrics_btn.setOnClickListener {
            playerPager.setCurrentItem(1, true)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {
                handler.removeCallbacks(runnable)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (!isLoaded) {
                    isLoaded = true
                    playMedia()
                    val runTask = Handler()
                    runTask.post(object : Runnable {
                        override fun run() {
                            if (mediaPlayerService!!.getCurrentPosition() > 1) {
                                Log.d(TAG, "Current Position : ${mediaPlayerService!!.getCurrentPosition()}")
                                mediaPlayerService!!.seekTo(p0!!.progress)
                                handler.post(runnable)
                            } else {
                                runTask.post(this)
                            }
                        }

                    })
                } else {
                    mediaPlayerService!!.seekTo(p0!!.progress)
                    handler.post(runnable)
                }
            }

        })

        /*tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabTextView = tabs.getChildAt(1) as TextView
                tabTextView.setTypeface(tabTextView.typeface, Typeface.NORMAL)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            val tabTextView = tabs.getChildAt(1) as TextView
                tabTextView.setTypeface(tabTextView.typeface, Typeface.BOLD)
            }

        })*/

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
                val time = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                var endTime: String
                val seconds: Int = ((java.lang.Long.parseLong(time) / 1000L) % 60).toInt()
                var minutes: Long = ((java.lang.Long.parseLong(time) / 1000L) - seconds) / 60L
                if (minutes >= 60) {
                    minutes %= 60
                    val hr: Int = (((java.lang.Long.parseLong(time) / 1000L) / 60) / 60).toInt()
                    endTime = "$hr:$minutes:$seconds"
                } else {
                    endTime = "$minutes:$seconds"
                }

                val newAudio = isFavAudioReturn(Audio(data, title, album, artist, endTime, java.lang.Long.parseLong(time), false, false))
                audioList.add(newAudio)
                dataList!!.add(data)
            }
            if (audioList.size > 0) {
                audioList.add(audioList.last())
                dataList!!.add(dataList!!.last())
            }
        }
        cursor!!.close()
    }

    private fun loadRecentAudio() {
        val realm = Realm.getDefaultInstance()
        val list1 = realm.where(RealmRecentAudio::class.java).findAll()
        recentList = ArrayList()

        val iterator = list1.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            if (isFileExist(item!!.data)) {
                recentList!!.add(isFavAudioReturn(Audio(item.data, item.title, item.album, item.artist, item.endTime, item.duration, false, true)))
            } else {
                realm.beginTransaction()
                item.deleteFromRealm()
                realm.commitTransaction()
            }
        }
    }

    private fun loadFavAudio() {
        val realm = Realm.getDefaultInstance()
        val list2 = realm.where(RealmFavAudio::class.java).findAll()
        favList = ArrayList()
        val iterator = list2.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (isFileExist(item.data)) {
                favList!!.add(Audio(item.data, item.title, item.album, item.artist, item.endTime, item.duration, false, true))
            } else {
                realm.beginTransaction()
                item.deleteFromRealm()
                realm.commitTransaction()
            }
        }
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
        //
        val activeAudio = mediaPlayerService!!.getActiveAudio()

        val index = dataList!!.indexOf(activeAudio.data)
        if (activeAudio.isFav) {
            activeAudio.isFav = false
            audioList[index].isFav = false
            favorite_view.setImageResource(R.drawable.ic_fav_emp)
            val favAudio = realm.where(RealmFavAudio::class.java).equalTo("data", activeAudio.data).findFirst()
            favAudio!!.deleteFromRealm()
            recentMusicFragment!!.makeFaveoOrUnFavAudio(activeAudio.data!!, false)
        } else {
            activeAudio.isFav = true
            audioList[index].isFav = true
            val newFav = RealmFavAudio()
            newFav.data = activeAudio.data!!
            newFav.title = activeAudio.title!!
            newFav.album = activeAudio.album!!
            newFav.artist = activeAudio.artist!!
            newFav.endTime = activeAudio.endTime
            newFav.duration = activeAudio.duration
            newFav.playing = false

            realm.copyToRealmOrUpdate(newFav)
            favorite_view.setImageResource(R.drawable.ic_fav_filled)
            recentMusicFragment!!.makeFaveoOrUnFavAudio(activeAudio.data!!, true)
        }
        realm.commitTransaction()
        favMusicFragment!!.addOrRemoveAudio(activeAudio)


    }

    private fun initMediaPlayer(data: String?) {
        mediaPlayerService!!.songClicked(data!!)
        startTime.text = "00:00"
    }

    private fun playMedia() {
        mediaPlayerService!!.startPlayer()
        play.setBackgroundResource(R.drawable.ic_pause_button)
        play_m.setBackgroundResource(R.drawable.ic_pause_button_small)

//        lyricsFragment!!.changeSong(mediaPlayerService!!.getActiveAudio())
        songInfoFragment!!.changeSong(mediaPlayerService!!.getActiveAudio())
    }

    private fun pauseMedia() {
        val activeAudio = mediaPlayerService!!.getActiveAudio()

        if (mediaPlayerService!!.isPlaying()) {
            mediaPlayerService!!.pausePlayer()
            resumePosition = mediaPlayerService!!.getCurrentPosition()
            play.setBackgroundResource(R.drawable.ic_play_button)
            play_m.setBackgroundResource(R.drawable.ic_play_button_small)

            allMusicFragment!!.pauseAudio(activeAudio, false)
            favMusicFragment!!.pauseAudio(activeAudio, false)
            recentMusicFragment!!.pauseAudio(activeAudio, false)
        }
    }

    private fun skipToNext() {

        val oldAudio = mediaPlayerService!!.getActiveAudio()
        mediaPlayerService!!.skipToNext()
        val activeAudio = mediaPlayerService!!.getActiveAudio()

        play.setBackgroundResource(R.drawable.ic_pause_button)
        play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        repeat_button.colorFilter = whiteFilteredColor
        isRepeat = false
        mediaPlayerService!!.setIsRepeat(isRepeat)

        updatePlayerData(activeAudio)
        allMusicFragment!!.updatePlayingStatus(activeAudio, oldAudio)
        favMusicFragment!!.updatePlayingStatus(activeAudio, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(activeAudio, oldAudio)
    }

    private fun skipToPrevious() {
        val oldAudio = mediaPlayerService!!.getActiveAudio()
        mediaPlayerService!!.skipToPrevious()
        val activeAudio = mediaPlayerService!!.getActiveAudio()

        play.setBackgroundResource(R.drawable.ic_pause_button)
        play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        repeat_button.colorFilter = whiteFilteredColor
        isRepeat = false
        mediaPlayerService!!.setIsRepeat(isRepeat)
        updatePlayerData(activeAudio)
        allMusicFragment!!.updatePlayingStatus(activeAudio, oldAudio)
        favMusicFragment!!.updatePlayingStatus(activeAudio, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(activeAudio, oldAudio)
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

        songTitle.text = audio.title
        songName.text = audio.title
        songArtist.text = audio.artist
        smallSongAartist.text = audio.artist

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(audio.data)
        val data = mediaMetadataRetriever.embeddedPicture
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val bitmapI = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
            smallThumbnail.setImageBitmap(bitmapI)
            songThumbnail.setImageBitmap(bitmap)
            changeThem(bitmap)
            Log.d(TAG, "Location ${audio.data}")
            Log.d(TAG, "B Width : ${bitmap.width} B Height : ${bitmap.height}")
            /*val blurredBitmap = BlurBuilder.blur(this@HomeActivity, bitmap, 0.4f, 20.5f)
            maxLayout.background = BitmapDrawable(resources, blurredBitmap)*/
            Blurry.with(this).from(bitmap).into(app_background)
            Blurry.with(this).from(bitmap).into(back_min)
        } else {
            Picasso.get().load(R.drawable.music).resize(100, 100).into(smallThumbnail)
            Picasso.get().load(R.drawable.music).into(songThumbnail)
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.music)
            Blurry.with(this).from(bitmap).into(app_background)
            Blurry.with(this).from(bitmap).into(back_min)
            changeThem(bitmap)
        }


//        startTime.text = "00:00"
        endTime.text = audio.endTime

//        seekBar.progress = 0
        progressBar.progress = 0
        seekBar.max = audio.duration.toInt()
        progressBar.max = audio.duration.toInt()
        if (audio.isFav) {
            favorite_view.setImageResource(R.drawable.ic_fav_filled)
        } else {
            favorite_view.setImageResource(R.drawable.ic_fav_emp)
        }

        songName.isSelected = true
        songTitle.isSelected = true


    }


    override fun onSongClick(audio: Audio, index: Int) {
        if (index != currentIndex) {
            repeat_button.colorFilter = whiteFilteredColor
            isRepeat = false
            mediaPlayerService!!.setIsRepeat(isRepeat)
        }
        val oldAudio = mediaPlayerService!!.getActiveAudio()
        mediaPlayerService!!.setActiveAudio(audio)
        mediaPlayerService!!.setAudioIndex(index)
        updatePlayerData(audio)
        if (bottom_sheet.visibility == View.GONE) {
            viewpager!!.visibility = View.GONE
//            bottom_sheet.visibility = View.VISIBLE
            maxLayout!!.alpha = 1f
            supportActionBar!!.hide()
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
        initMediaPlayer(audio.data)
        mediaPlayerService!!.callPlayerAsync()

        allMusicFragment!!.updatePlayingStatus(audio, oldAudio)
        favMusicFragment!!.updatePlayingStatus(audio, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(audio, oldAudio)
    }

    override fun onAudioChanged(audioIndex: Int, audio: Audio, oldAudioIndex: Int, oldAudio: Audio) {
        updatePlayerData(audio)
        handler.removeCallbacks(runnable)
        handler.post(runnable)
        play.setBackgroundResource(R.drawable.ic_pause_button)
        play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        lyricsFragment!!.changeSong(mediaPlayerService!!.getActiveAudio())
        songInfoFragment!!.changeSong(mediaPlayerService!!.getActiveAudio())

        allMusicFragment!!.updatePlayingStatus(audio, oldAudio)
        favMusicFragment!!.updatePlayingStatus(audio, oldAudio)
        recentMusicFragment!!.updatePlayingStatus(audio, oldAudio)


    }

    override fun onPlayPause(status: Int) {
        if (status == 1) {
            play.setBackgroundResource(R.drawable.ic_pause_button)
            play_m.setBackgroundResource(R.drawable.ic_pause_button_small)
        } else {
            play.setBackgroundResource(R.drawable.ic_play_button)
            play_m.setBackgroundResource(R.drawable.ic_play_button_small)
        }
        allMusicFragment!!.updatePausePlay(mediaPlayerService!!.getActiveAudio(), status)
        favMusicFragment!!.updatePausePlay(mediaPlayerService!!.getActiveAudio(), status)
        recentMusicFragment!!.updatePausePlay(mediaPlayerService!!.getActiveAudio(), status)
        if (mBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN) {
            mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onExit() {
        finish()
    }

    private fun doMediaTimeSeekChanges() {
        runnable = object : Runnable {
            override fun run() {
                if (mediaPlayerService != null) {
                    if (mediaPlayerService!!.getCurrentPosition() > -1) {

                        val seconds: Int = ((mediaPlayerService!!.getCurrentPosition() / 1000) % 60)
                        var minutes: Int = ((mediaPlayerService!!.getCurrentPosition() / 1000) - seconds) / 60
                        if (minutes >= 60) {
                            minutes %= 60

                            val hr: Int = (((mediaPlayerService!!.getCurrentPosition() / 1000) / 60) / 60)
                            startTime.text = "${format2LenStr(hr)}:${format2LenStr(minutes)}:${format2LenStr(seconds)}"
                        } else {
                            startTime.text = "${format2LenStr(minutes)}:${format2LenStr(seconds)}"
                        }
                        seekBar.progress = (mediaPlayerService!!.getCurrentPosition())
                        progressBar.progress = (mediaPlayerService!!.getCurrentPosition())
                    }
                    handler.postDelayed(this, 10)
                }
            }
        }
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

        override fun onPostExecute(result: Long?) {
            musicConnection = object : ServiceConnection {

                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder: MediaPlayerService.MusicBinder = service as MediaPlayerService.MusicBinder
                    mediaPlayerService = binder.getService()
                    Log.d(TAG, "Bind Service")
                    musicBound = true

                    if (audioList.size != 0) {
                        Log.d(TAG, "#Time1 : ${System.currentTimeMillis() / 1000L}")
                        setupViewPager(viewpager)
                        setUpPlayerPager(appPreferences!!.getLastAudio())
                        setUpListeners()
                        tabs.setupWithViewPager(viewpager)
                        Log.d(TAG, "Bind Service2")
                        mediaPlayerService!!.setSongList(audioList)
                        mediaPlayerService!!.setMediaPlayerControllerListener(this@HomeActivity)
                        Log.d(TAG, "#Time2 : ${System.currentTimeMillis() / 1000L}")

                        if (appPreferences!!.getLastAudio() != null) {
                            var audio = appPreferences!!.getLastAudio()
                            var index = dataList!!.indexOf(audio!!.data)

                            if (!isFileExist(audio.data!!)) {
                                index = 0
                                audio = audioList[index]
                            }

                            mediaPlayerService!!.setActiveAudio(audio!!)
                            mediaPlayerService!!.setAudioIndex(index)
                            Log.d(TAG, "#Time3 : ${System.currentTimeMillis() / 1000L}")

                            updatePlayerData(audio)
                            Log.d(TAG, "#Time4 : ${System.currentTimeMillis() / 1000L}")

                            if (bottom_sheet.visibility == View.GONE) {
                                bottom_sheet.visibility = View.VISIBLE
                                miniLayout!!.alpha = 1f
                                miniLayout.bringToFront()
                                mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                            }
                            initMediaPlayer(audio.data)
                            /*allMusicFragment!!.updatePlayingStatus(audio, audio)
                            favMusicFragment!!.updatePlayingStatus(audio, audio)
                            recentMusicFragment!!.updatePlayingStatus(audio, audio)*/
                        } else {
                            val audio = audioList[0]
                            val index = 0

                            mediaPlayerService!!.setActiveAudio(audio)
                            mediaPlayerService!!.setAudioIndex(index)
                            Log.d(TAG, "#Time3 : ${System.currentTimeMillis() / 1000L}")
                            updatePlayerData(audio)
                            Log.d(TAG, "#Time10 : ${System.currentTimeMillis() / 1000L}")
                            if (bottom_sheet.visibility == View.GONE) {
                                bottom_sheet.visibility = View.VISIBLE
                                miniLayout!!.alpha = 1f
                                miniLayout.bringToFront()

                            }
                            initMediaPlayer(audio.data)
                        }
                    } else {
                        bottom_sheet.visibility = View.GONE
                    }
//                    music_load_progress.visibility = View.GONE
                    hideShowMainViews(View.VISIBLE)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    musicBound = false
                }

            }
            playIntent = Intent(this@HomeActivity, MediaPlayerService::class.java)
            startService(playIntent)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)


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

    private fun createPaletteSync(bitmap: Bitmap): Palette {
        return Palette.from(bitmap).generate()
    }

    private fun changeThem(bitmap: Bitmap) {
        val vibrantSwatch: Palette.Swatch? = createPaletteSync(bitmap).vibrantSwatch
        val mutedSwatch: Palette.Swatch? = createPaletteSync(bitmap).mutedSwatch

        if (mutedSwatch != null) {
            val mixedColorTwo = manipulateColor(mutedSwatch.rgb + mutedSwatch.bodyTextColor + mutedSwatch.titleTextColor, 0.8f)
            val lightColor = getLighterShadeColor(mixedColorTwo)

            filteredColor = LightingColorFilter(Color.parseColor("#000000"), lightColor)

            tabs.setTabTextColors(
                    Color.WHITE,
                    lightColor
            )
            tabs.setSelectedTabIndicatorColor(lightColor)

            progressBar.progressDrawable.colorFilter = filteredColor
            seekBar.progressDrawable.colorFilter = filteredColor
            seekBar.thumb.colorFilter = filteredColor

            (previous_view.background as Drawable).colorFilter = filteredColor
            (play_view.background as Drawable).colorFilter = filteredColor
            (next_view.background as Drawable).colorFilter = filteredColor

            allMusicFragment!!.updateColor(lightColor)
            favMusicFragment!!.updateColor(lightColor)
            recentMusicFragment!!.updateColor(lightColor)

        } else if (vibrantSwatch != null) {
            val mixedColorOne = manipulateColor(vibrantSwatch.rgb + vibrantSwatch.bodyTextColor + vibrantSwatch.titleTextColor, 1.8f)
            val lightColor = getLighterShadeColor(mixedColorOne)
            filteredColor = LightingColorFilter(Color.parseColor("#000000"), lightColor)

            tabs.setTabTextColors(
                    Color.WHITE,
                    lightColor
            )
            tabs.setSelectedTabIndicatorColor(lightColor)


            progressBar.progressDrawable.colorFilter = filteredColor
            seekBar.progressDrawable.colorFilter = filteredColor
            seekBar.thumb.colorFilter = filteredColor

            (previous_view.background as Drawable).colorFilter = filteredColor
            (play_view.background as Drawable).colorFilter = filteredColor
            (next_view.background as Drawable).colorFilter = filteredColor

            allMusicFragment!!.updateColor(lightColor)
            favMusicFragment!!.updateColor(lightColor)
            recentMusicFragment!!.updateColor(lightColor)
        }
    }

    private fun hideShowMainViews(visibility: Int) {
        appBar.visibility = visibility
        viewpager.visibility = visibility
        app_background.visibility = visibility
    }

    private fun setImageSize(bitmap: Bitmap) {


        songThumbnail.post {
            val width = songThumbnail.measuredWidth
            val param = songThumbnail.layoutParams as RelativeLayout.LayoutParams
            param.width = width

            val percent = (100 * (width - bitmap.width)) / bitmap.width
            Log.d(TAG, "Percent of Increment : $percent")
            param.height = bitmap.height + ((bitmap.height / 100) * percent)
            songThumbnail.maxHeight = getDp(200)
            songThumbnail.layoutParams = param
            Log.d(TAG, "Width after : ${param.width}")
            Log.d(TAG, "Height after : ${param.height}")
        }


    }

    private fun grabLyricsData() {
        if (isNetworkAvaialable()) {
            val list = ArrayList<Lyrics>()
            mDatabase.child("lyrics").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        it.getValue<Lyrics>(Lyrics::class.java)?.let {
                            list.add(it)
                        }
                    }
                    Log.d(TAG, "Lyrics Size : ${list.size}")
                    val realm = Realm.getDefaultInstance()
                    realm.beginTransaction()
                    realm.copyToRealmOrUpdate(list)
                    realm.commitTransaction()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, "loadPost:onCancelled", databaseError.toException())

                }

            })
        }
    }

    private fun format2LenStr(num: Int): String {
        return if (num < 10) "0$num" else num.toString()
    }

}
