package braincrush.mirza.com.MMusic.adapter

import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import braincrush.mirza.com.MMusic.models.Audio
import braincrush.mirza.com.MMusic.R
import java.util.*
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView


/**
 * Created by MIRZA on 19/10/17.
 */
class SongAdapter(list: List<Audio>, internal var context: Context) : RecyclerView.Adapter<ViewHolder>() {

    internal var list = Collections.emptyList<Audio>()

    init {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate the layout, initialize the View Holder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        val holder = ViewHolder(v)
        return holder

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.text = list[position].title
    }

    override fun getItemCount(): Int {
        //returns the number of elements the RecyclerView will display
        return list.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var title: TextView = itemView.findViewById(R.id.title)
    var play_pause: ImageView = itemView.findViewById(R.id.play_pause)

}