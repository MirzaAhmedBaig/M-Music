package com.mirza.mmusic.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mirza.mmusic.R
import com.mirza.mmusic.interfaces.ItemClickListener
import com.mirza.mmusic.models.Audio
import com.squareup.picasso.Picasso


/**
 * Created by MIRZA on 14/11/17.
 */
class MusicListAdapter(private val activity: Activity, private val type: Int, private val audioList: List<Audio>, val itemClickListener: ItemClickListener) : RecyclerView.Adapter<MusicListAdapter.MyViewHolder>() {

    private val TAG: String = MusicListAdapter::class.java.simpleName

//    private val bitmapList: ArrayList<BitmapMapModel> = ArrayList()

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    // Use 1/8th of the available memory for this memory cache.
    private val cacheSize: Int = maxMemory / 8

    private var bitmapCache: LruCache<Int, Bitmap> = LruCache(cacheSize)


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            view.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            if (adapterPosition >= 0)
                itemClickListener.onClick(view, adapterPosition)
        }

        var title: TextView = view.findViewById(R.id.title)
        var artist: TextView = view.findViewById(R.id.artist)
        var songTime: TextView = view.findViewById(R.id.songTime)
        var play: ImageView = view.findViewById(R.id.playIcon)
        var thumbnail: ImageView = view.findViewById(R.id.thumbnail)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.audio_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val audio = audioList[position]
        Log.d(TAG, "Position is : $position")
        holder.title.text = audio.title
        holder.artist.text = audio.artist
        holder.songTime.text = audio.endTime

        if (getBitmapFromMemCache(position) != null) {
            holder.thumbnail.setImageBitmap(getBitmapFromMemCache(position))
        } else {
            Picasso.with(activity.baseContext).load(R.drawable.music).into(holder.thumbnail)
            /*AsyncTask.execute({
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(audio.data)
                val picData = mediaMetadataRetriever.embeddedPicture
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                if (picData != null) {
                    var bitmapImage = BitmapFactory.decodeByteArray(picData, 0, picData.size)
                    var bitmapI = Bitmap.createScaledBitmap(bitmapImage, 90, 90, true)
                    activity.runOnUiThread({
                        holder.thumbnail.setImageBitmap(bitmapI)
                    })

                    val newBtimap = BitmapMapModel()
                    newBtimap.bitmap = bitmapI
                    newBtimap.position = position
                    addBitmapToMemoryCache(position, bitmapI)

                } else {
                    activity.runOnUiThread({
                        Picasso.with(activity.baseContext).load(R.drawable.music).into(holder.thumbnail)
                    })
                }

            })*/

        }

        if (audioList[position].playing) {
            holder.play.setBackgroundResource(R.drawable.ic_pause)
        } else {
            holder.play.setBackgroundResource(R.drawable.ic_play_button_filled)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    fun addBitmapToMemoryCache(index: Int, bitmap: Bitmap?) {
        if (getBitmapFromMemCache(index) == null) {
            bitmapCache.put(index, bitmap)
        }
    }

    private fun getBitmapFromMemCache(index: Int): Bitmap? {
        return bitmapCache.get(index)
    }

    fun loadAllBitmap() {
        AsyncTask.execute({
            for (i in 0 until audioList.size) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(audioList[i].data)
                val picData = mediaMetadataRetriever.embeddedPicture
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                if (picData != null) {
                    var bitmapImage = BitmapFactory.decodeByteArray(picData, 0, picData.size)
                    var bitmapI = Bitmap.createScaledBitmap(bitmapImage, 90, 90, true)

                    addBitmapToMemoryCache(i, bitmapI)
                    activity.runOnUiThread({
                        notifyItemChanged(i)
                    })

                }
            }
        })
    }
}