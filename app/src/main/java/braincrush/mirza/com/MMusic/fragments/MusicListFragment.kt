package braincrush.mirza.com.MMusic.fragments

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.adapter.MusicListAdapter
import braincrush.mirza.com.MMusic.models.Audio
import kotlinx.android.synthetic.main.fragment_music_list.*
import kotlinx.android.synthetic.main.fragment_music_list.view.*
import android.support.v7.widget.DividerItemDecoration
import android.transition.*
import android.util.Log
import android.widget.LinearLayout.VERTICAL
import braincrush.mirza.com.MMusic.interfaces.ItemClickListener
import android.view.animation.AnticipateOvershootInterpolator
import braincrush.mirza.com.MMusic.ExplodeFadeOut


class MusicListFragment : Fragment(), ItemClickListener {
    override fun onClick(view: View?, index: Int) {
        Log.d("TAG", "Clicked:")


    }


    private var type: Int? = null
    private lateinit var list: ArrayList<Audio>


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.enterTransition=ExplodeFadeOut()
        }
        val view: View = inflater!!.inflate(R.layout.fragment_music_list, container, false)

        if (arguments != null) {
            type = arguments.getInt(TYPE)
            list = (arguments.getSerializable(LIST) as ArrayList<Audio>?)!!


            view.recyclerview.layoutManager = LinearLayoutManager(activity)
            view.recyclerview.adapter = MusicListAdapter(type!!, list, this)

            val decoration = DividerItemDecoration(activity, VERTICAL)
            view.recyclerview.addItemDecoration(decoration)
        }
        return view
    }

    companion object {
        private val TYPE = "param1"
        private val LIST = "param2"

        fun newInstance(type: Int, list: ArrayList<Audio>): MusicListFragment {
            val fragment = MusicListFragment()
            val args = Bundle()
            args.putInt(TYPE, type)
            args.putSerializable(LIST, list)
            fragment.arguments = args
            return fragment
        }
    }

}
