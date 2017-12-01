/*
package braincrush.mirza.com.MMusic.activities

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.AdapterView
import android.provider.MediaStore
import braincrush.mirza.com.MMusic.models.Audio
import braincrush.mirza.com.MMusic.R
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {


    private val TAG = MainActivity::class.java.simpleName
    lateinit var songs: ArrayList<String>
    lateinit var audioList: ArrayList<Audio>
    var mediaPlayer: MediaPlayer? = null
    private var resumePosition: Int = 0
    private var audioIndex: Int = 0
    private var activeAudio: Audio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
        loadAudio()
        setSongsListAdapter()
        activeAudio = audioList.elementAt(audioIndex)
        initMediaPlayer(activeAudio!!.data)
    }

    fun setListeners() {
        next.setOnClickListener(this)
        previous.setOnClickListener(this)
        play.setOnClickListener(this)
    }


    override fun onClick(view: View) {
        var flag = false
        when (view) {
            play -> {
                Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show()
                if (flag) {
                    play.setBackgroundResource(R.drawable.ic_play_button)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        play.background = getDrawable(R.drawable.rounded_view)
                    }
                } else {
                    play.setBackgroundResource(R.drawable.ic_pause_button)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        play.background = getDrawable(R.drawable.rounded_view)
                    }
                }
                flag = !flag
            }
            next -> {
                skipToNext()
                Toast.makeText(this, "next", Toast.LENGTH_SHORT).show()
            }
            previous -> {
                skipToPrevious()
                Toast.makeText(this, "previous", Toast.LENGTH_SHORT).show()

            }
        }
    }

    fun setSongsListAdapter() {
        songList.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs)
        songList.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->

            songName.text = songs[position]
            initMediaPlayer(audioList.elementAt(position).data!!)
        }
    }

    private fun initMediaPlayer(data: String?) {
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer()//new MediaPlayer instance

        //Set up MediaPlayer event listeners
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        mediaPlayer!!.setOnSeekCompleteListener(this)
        mediaPlayer!!.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer!!.reset()


        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            // Set the data source to the mediaFile location
            mediaPlayer!!.setDataSource(data)
        } catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer!!.stop()

        }

        mediaPlayer!!.prepareAsync()
    }

    private fun loadAudio() {
        val contentResolver = contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.count > 0) {
            audioList = ArrayList()
            songs = ArrayList()
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))

                // Save to audioList
                audioList.add(Audio(data, title, album, artist,false))
                songs.add(title)
            }
        }
        cursor!!.close()
    }


    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    private fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }

    private fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    private fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
        }
    }

    private fun skipToNext() {

        if (audioIndex == audioList!!.size - 1) {
            //if last in playlist
            audioIndex = 0
            activeAudio = audioList!![audioIndex]
        } else {
            //get next in playlist
            activeAudio = audioList!![++audioIndex]
        }


        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data)
    }

    private fun skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList!!.size - 1
            activeAudio = audioList!![audioIndex]
        } else {
            //get previous in playlist
            activeAudio = audioList!![--audioIndex]
        }


        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer(activeAudio!!.data)
    }

    override fun onPrepared(p0: MediaPlayer?) {
        playMedia()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
*/
