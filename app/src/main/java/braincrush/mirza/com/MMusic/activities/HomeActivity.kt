package braincrush.mirza.com.MMusic.activities

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import braincrush.mirza.com.MMusic.R
import android.support.v4.view.ViewPager
import braincrush.mirza.com.MMusic.adapter.ViewPagerAdapter
import braincrush.mirza.com.MMusic.fragments.MusicListFragment
import braincrush.mirza.com.MMusic.models.Audio
import kotlinx.android.synthetic.main.activity_home.*
import android.widget.Toast
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.os.Build
import android.util.Log
import android.view.View
import braincrush.mirza.com.MMusic.interfaces.ItemClickListener


class HomeActivity : AppCompatActivity(){


    private lateinit var list: ArrayList<Audio>
    private lateinit var audioList: ArrayList<Audio>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        loadAudio()
        setupViewPager(viewpager)
        tabs.setupWithViewPager(viewpager)
//        requestPermission()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(MusicListFragment.newInstance(1, audioList), "Songs")
        adapter.addFragment(MusicListFragment.newInstance(2, audioList), "Favorite")
        adapter.addFragment(MusicListFragment.newInstance(3, audioList), "Recent")
        viewPager.adapter = adapter
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
