package com.mirza.mmusic.fragments

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import com.mirza.mmusic.ExplodeFadeOut
import com.mirza.mmusic.R
import com.mirza.mmusic.adapter.MusicListAdapter
import com.mirza.mmusic.interfaces.ItemClickListener
import com.mirza.mmusic.interfaces.MusicPlayerListener
import com.mirza.mmusic.models.Audio
import kotlinx.android.synthetic.main.fragment_music_list.view.*


class MusicListFragment : Fragment(), ItemClickListener {

    private val TAG: String = MusicListFragment::class.java.simpleName
    private var type: Int? = null
    private var list: ArrayList<Audio>? = null
    private var globalView: View? = null
    private var recyclerView: RecyclerView? = null
    private var musicPlayerListener: MusicPlayerListener? = null
    private var adapter: MusicListAdapter? = null

    private var dataList: ArrayList<String>? = null

    fun setUpMusicListener(musicPlayerListener: MusicPlayerListener) {
        this.musicPlayerListener = musicPlayerListener
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity!!.window.enterTransition = ExplodeFadeOut()
        }
        val view: View = inflater.inflate(R.layout.fragment_music_list, container, false)
        if (arguments != null) {
            type = arguments!!.getInt(MusicListFragment.TYPE)
            list = (arguments!!.getSerializable(MusicListFragment.LIST) as ArrayList<Audio>?)!!

            dataList = ArrayList()
            for (i in 0 until list!!.size) {
                dataList!!.add(list!![i].data!!)
            }

            recyclerView = view.findViewById(R.id.recyclerview)

            if (list!!.size < 1) {
                view.emptySong_text.visibility = View.VISIBLE
            } else {
                view.emptySong_text.visibility = View.GONE
            }


            view.recyclerview.layoutManager = LinearLayoutManager(activity)
            adapter = MusicListAdapter(activity!!, type!!, list!!, this)
            view.recyclerview.adapter = adapter
            adapter!!.loadAllBitmap()

            val decoration = DividerItemDecoration(activity, VERTICAL)
            view.recyclerview.addItemDecoration(decoration)
        }
        globalView = view
        recyclerView!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                return list!!.size < 1
            }

        })
        Log.d(TAG, "Frag type :$type")
        return view
    }

    override fun onClick(view: View?, index: Int) {
        /*list!![index].playing = true
        (0 until list!!.size)
                .filter { it != index }
                .forEach { list!![it].playing = false }
        adapter!!.notifyDataSetChanged()*/
        if (musicPlayerListener != null) {
            musicPlayerListener!!.onSongClick(list!![index], index)
        }
    }

    fun updatePlayingStatus(audio: Audio, oldAudio: Audio) {
        if (dataList!!.contains(oldAudio.data)) {
            val index = dataList!!.indexOf(oldAudio.data)
            list!![index].playing = false
            adapter!!.notifyItemChanged(index)
        }
        if (dataList!!.contains(audio.data)) {
            val index = dataList!!.indexOf(audio.data)
            list!![index].playing = true
            adapter!!.notifyItemChanged(index)
        }
        /*if(dataList!!.contains(audio.data)){
            val index=dataList!!.indexOf(audio.data)
            list!![index].playing = true
            (0 until list!!.size)
                    .filter { it != index }
                    .forEach { list!![it].playing = false }
//            adapter!!.notifyDataSetChanged()

            adapter!!.notifyItemChanged(index)
        }*/
    }

    fun pauseAudio(audio: Audio, isPlaying: Boolean) {
        if (dataList!!.contains(audio.data)) {
            val index = dataList!!.indexOf(audio.data)
            list!![index].playing = isPlaying
            adapter!!.notifyItemChanged(index)
        }

    }

    fun updateAudio(audio: Audio) {
        if (!dataList!!.contains(audio.data)) {
            list!!.add(audio)
            dataList!!.add(audio.data!!)
            adapter!!.notifyItemInserted(list!!.size - 1)
        }
        if (list!!.size > 0) {
            view!!.emptySong_text.visibility = View.GONE
        } else {
            view!!.emptySong_text.visibility = View.VISIBLE
        }
    }

    fun addOrRemoveAudio(audio: Audio) {
        Log.d(TAG, "addOrRemoveAudio : ${audio.isFav}")
        if (audio.isFav) {
            list!!.add(audio)
            dataList!!.add(audio.data!!)
            adapter!!.notifyItemInserted(list!!.size - 1)
        } else {
            val index = dataList!!.indexOf(audio.data)
            list!!.removeAt(index)
            dataList!!.removeAt(index)
            adapter!!.notifyItemRemoved(index)
        }

        if (list!!.size > 0) {
            view!!.emptySong_text.visibility = View.GONE
        } else {
            view!!.emptySong_text.visibility = View.VISIBLE
        }
    }

    fun makeFaveoOrUnFavAudio(data: String, isFav: Boolean) {
        Log.d(TAG, "makeFaveoOrUnFavAudio : ${isFav}")
        val index = dataList!!.indexOf(data)
        if (index > -1) {
            list!![index].isFav = isFav
            adapter!!.notifyItemChanged(index)
        }

    }


    companion object {
        val TYPE = "param1"
        val LIST = "param2"

        fun newInstance(type: Int, list: ArrayList<Audio>): MusicListFragment {
            val fragment = MusicListFragment()
            val args = Bundle()
            args.putInt(TYPE, type)
            args.putParcelableArrayList(LIST, list)
            fragment.arguments = args
            return fragment
        }
    }
}
