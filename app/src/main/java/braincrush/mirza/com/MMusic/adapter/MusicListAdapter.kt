package braincrush.mirza.com.MMusic.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.interfaces.ItemClickListener
import braincrush.mirza.com.MMusic.models.Audio
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


/**
 * Created by MIRZA on 14/11/17.
 */
class MusicListAdapter(private val context: Context, private val type: Int, private val audioList: List<Audio>, val itemClickListener: ItemClickListener) : RecyclerView.Adapter<MusicListAdapter.MyViewHolder>() {

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
        holder.title.text = audio.title
        holder.artist.text = audio.artist
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(audio.data)
        val data = mediaMetadataRetriever.embeddedPicture
        if (data != null) {
            val bitmapImage = BitmapFactory.decodeByteArray(data, 0, data.size)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 1, ByteArrayOutputStream())
//            holder.thumbnail.setImageBitmap(bitmapImage)
            Picasso.with(context).load(R.drawable.list_back).placeholder(R.drawable.ic_letter_m_box).into(object : com.squareup.picasso.Target {
                override fun onBitmapFailed(errorDrawable: Drawable?) {

                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    holder.thumbnail.setImageBitmap(bitmapImage)

                }
            })
        } else {
            holder.thumbnail.setBackgroundResource(R.drawable.ic_letter_m_box)
        }

        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        var seconds: Int = ((java.lang.Long.parseLong(durationStr) / 1000L) % 60).toInt()
        var minutes: Long = ((java.lang.Long.parseLong(durationStr) / 1000L) - seconds) / 60L

        holder.songTime.text = "" + minutes + ":" + seconds
        if (audioList[position].playing) {
            holder.play.setBackgroundResource(R.drawable.ic_pause)
        }

    }

    override fun getItemCount(): Int {
        return audioList.size
    }

}