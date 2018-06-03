package com.mirza.mmusic.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mirza.mmusic.R
import com.mirza.mmusic.models.Audio
import kotlinx.android.synthetic.main.fragment_lyrics.view.*


class LyricsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lyrics, container, false)
        if (arguments != null) {
            val audio = arguments!!.getParcelable<Audio>("data")
            view.songName.text = audio.title
            view.songName.isSelected = true
        }
        return view
    }

    fun changeSong(audio: Audio) {
        view!!.songName.text = audio.title
    }

    companion object {

        fun newInstance(audio: Audio): LyricsFragment {
            val fragment = LyricsFragment()
            val args = Bundle()
            args.putParcelable("data", audio)
            fragment.arguments = args
            return fragment
        }
    }
}