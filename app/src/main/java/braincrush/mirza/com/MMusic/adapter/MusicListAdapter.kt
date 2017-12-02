package braincrush.mirza.com.MMusic.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        holder.songTime.text = audio.endTime

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(audio.data)
        val picData = mediaMetadataRetriever.embeddedPicture
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        if (picData != null) {
            var bitmapImage = BitmapFactory.decodeByteArray(picData, 0, picData.size)
            var bitmapI = Bitmap.createScaledBitmap(bitmapImage, 100, 100, true)
            holder.thumbnail.setImageBitmap(bitmapI)

            /*val target = object : com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                }

            }
            Picasso.with(context).load(R.drawable.dummy).into(target)
            holder.thumbnail.tag = target*/
        } else {
            holder.thumbnail.setBackgroundResource(R.drawable.ic_compact_disc)
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

    /*override fun getItemViewType(position: Int): Int {
        return if (audioList[position].playing) 1 else 0
    }*/

}