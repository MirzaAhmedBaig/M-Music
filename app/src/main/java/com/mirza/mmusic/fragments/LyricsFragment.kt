package com.mirza.mmusic.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mirza.mmusic.R
import com.mirza.mmusic.models.Audio
import com.mirza.mmusic.models.Lyrics
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_lyrics.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class LyricsFragment : Fragment() {

    private val TAG = LyricsFragment::class.java.simpleName


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_lyrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            val audio = arguments!!.getParcelable<Audio>("data")
            getLyricsURL(audio)

        }

    }

    fun changeSong(audio: Audio) {
        if (view != null) {
            lyrics_text.text = ""
            Log.d(TAG, "#Call to method")
            getLyricsURL(audio)
        }
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

    private fun getLyricsURL(info: Audio) {
        lyrics_text.visibility = View.GONE
        not_found_text.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        AsyncTask.execute {
            val realm = Realm.getDefaultInstance()
            val list = realm.where(Lyrics::class.java).findAll()
            Log.d(TAG, "Title : ${info.title}")
            Log.d(TAG, "Artist : ${info.artist}")
            Log.d(TAG, "#Call to method2")

            val artistArray = if (info.artist!!.contains(",")) {
                info.artist!!.split(",")
            } else {
                info.artist!!.split(" ")
            }.map { it.toLowerCase().trim() }

            artistArray.forEach {
                Log.d(TAG, "Srtring : $it")
            }

            val lyricsData = if (list.isNotEmpty()) {
                val artistList = list.filter { artistArray.contains(it.artists.toLowerCase()) }
                Log.d(TAG, "List Size : ${artistList.size}")
                if (artistList.isNotEmpty()) {
                    val titlesList = artistList.filter { it.title == info.title }
                    if (titlesList.isNotEmpty())
                        titlesList[0]
                    else
                        null
                } else {
                    null
                }
            } else {
                null
            }
            Log.d(TAG, "#getLyricsURL $lyricsData")
            if (lyricsData != null) {
                getLyrics(lyricsData.url)

            } else {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    not_found_text.visibility = View.VISIBLE
                }
            }
        }


    }

    private fun getLyrics(url: String) {
        try {
            Log.d(TAG, "URL : ${url}")
            val doc = Jsoup.connect(url).get()
            doc.outputSettings(Document.OutputSettings().prettyPrint(false))//makes html() preserve linebreaks and spacing
            doc.select("br").append("\\n")
            val lyricsData = doc.select("p#lyrics_text")
            val new = lyricsData.text().replace("\\n", System.getProperty("line.separator"))
            Log.d(TAG, "Lyrics for3  : $new")
            activity?.runOnUiThread {
                lyrics_text.text = new
                progressBar.visibility = View.GONE
                lyrics_text.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}