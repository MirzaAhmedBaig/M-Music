package braincrush.mirza.com.MMusic.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.models.Audio


class MusicPlayerFragment : Fragment() {

    private lateinit var data: Audio


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (arguments != null) {
            data = arguments.getSerializable(ARG_DATA) as Audio
        }
        return inflater!!.inflate(R.layout.fragment_music_player, container, false)
    }


    companion object {
        private val ARG_DATA = "param1"


        fun newInstance(data: Audio): MusicPlayerFragment {
            val fragment = MusicPlayerFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }
}
