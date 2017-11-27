package braincrush.mirza.com.MMusic.activities

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
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


class HomeActivity : AppCompatActivity(), MusicPlayerListener {


    private lateinit var audioList: ArrayList<Audio>
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        loadAudio()
        setupViewPager(viewpager)
        setUpBottomSheet()
        setUpListeners()
        tabs.setupWithViewPager(viewpager)
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
    }

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
                audioList.add(Audio(data, title, album, artist))
            }
        }
        cursor!!.close()
    }


    override fun onSongClick(audio: Audio) {

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
        }
        if (bottom_sheet.visibility == View.GONE) {
            viewpager!!.visibility = View.GONE
            bottom_sheet.visibility = View.VISIBLE
            maxLayout!!.alpha = 1f
            supportActionBar!!.hide()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
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
