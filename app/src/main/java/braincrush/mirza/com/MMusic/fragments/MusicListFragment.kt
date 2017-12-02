package braincrush.mirza.com.MMusic.fragments

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import braincrush.mirza.com.MMusic.ExplodeFadeOut
import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.adapter.MusicListAdapter
import braincrush.mirza.com.MMusic.interfaces.ItemClickListener
import braincrush.mirza.com.MMusic.interfaces.MusicPlayerListener
import braincrush.mirza.com.MMusic.models.Audio
import kotlinx.android.synthetic.main.fragment_music_list.view.*


class MusicListFragment : Fragment(), ItemClickListener {


    private var type: Int? = null
    private lateinit var list: ArrayList<Audio>
    private lateinit var globalView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicPlayerListener: MusicPlayerListener
    private lateinit var adapter: MusicListAdapter

    fun setUpMusicListener(musicPlayerListener: MusicPlayerListener) {
        this.musicPlayerListener = musicPlayerListener
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.enterTransition = ExplodeFadeOut()
        }
        val view: View = inflater!!.inflate(R.layout.fragment_music_list, container, false)
        if (arguments != null) {
            type = arguments.getInt(TYPE)
            list = (arguments.getSerializable(LIST) as ArrayList<Audio>?)!!

            recyclerView = view.findViewById(R.id.recyclerview)
            view.recyclerview.layoutManager = LinearLayoutManager(activity)
            adapter = MusicListAdapter(context, type!!, list, this)
            view.recyclerview.adapter = adapter

            val decoration = DividerItemDecoration(activity, VERTICAL)
            view.recyclerview.addItemDecoration(decoration)
        }
        globalView = view
        return view
    }


    override fun onClick(view: View?, index: Int) {
        list[index].playing = true
        (0 until list.size)
                .filter { it != index }
                .forEach { list[it].playing = false }
        adapter.notifyDataSetChanged()
        if (musicPlayerListener != null) {
            musicPlayerListener.onSongClick(list[index], index)
        }
    }


    companion object {
        private val TYPE = "param1"
        private val LIST = "param2"

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
