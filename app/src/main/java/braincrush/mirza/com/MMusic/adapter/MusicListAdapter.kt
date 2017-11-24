package braincrush.mirza.com.MMusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.models.Audio
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import braincrush.mirza.com.MMusic.interfaces.ItemClickListener
import java.util.concurrent.TimeUnit


/**
 * Created by MIRZA on 14/11/17.
 */
class MusicListAdapter(private val type: Int, private val audioList: List<Audio>, val itemClickListener: ItemClickListener) : RecyclerView.Adapter<MusicListAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            itemClickListener.onClick(p0, adapterPosition)
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
        holder.title.text = audio.title
        holder.artist.text = audio.artist
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(audio.data)
        val data = mmr.embeddedPicture
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            holder.thumbnail.setImageBitmap(bitmap)
        }
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(audio.data!!)

        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        var seconds: Int = ((java.lang.Long.parseLong(durationStr) / 1000L) % 60).toInt()
        var minutes: Long = ((java.lang.Long.parseLong(durationStr) / 1000L) - seconds) / 60L

        holder.songTime.text = "" + minutes + ":" + seconds

    }

    override fun getItemCount(): Int {
        return audioList.size
    }
}