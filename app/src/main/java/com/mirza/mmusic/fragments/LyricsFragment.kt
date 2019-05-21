package com.mirza.mmusic.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mirza.mmusic.ACRCloudClasses.ACRCloudRecognizer
import com.mirza.mmusic.R
import com.mirza.mmusic.models.Artists
import com.mirza.mmusic.models.Audio
import kotlinx.android.synthetic.main.fragment_lyrics.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.util.HashMap
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.set


class LyricsFragment : Fragment() {

    private val TAG = LyricsFragment::class.java.simpleName
    private var mResult = ""
    private var audio: Audio? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_lyrics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            audio = arguments!!.getParcelable("data")
            getLyricsURL(audio!!)

        }

    }

    fun changeSong(audio: Audio) {
        this.audio = audio
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
        /*lyrics_text.visibility = View.GONE
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
        }*/

        RecThread().start()


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


    //ACR Audio Cloud Code

    private val mHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: android.os.Message) {
            when (msg.what) {
                1 -> {
                    if (JSONObject(JSONObject(msg.obj.toString()).getString("status")).getString("msg") == "Success") {
                        val data = JSONObject(JSONArray(JSONObject(JSONObject(msg.obj.toString()).getString("metadata")).getString("music"))[0].toString())
                        var title = data.getString("title")
                        Log.d(TAG, "Title : $title")

                        title = removeGarbageFrom(title)
                        val artistArra: ArrayList<Artists> = Gson().fromJson(data.getString("artists"), object : TypeToken<ArrayList<Artists>>() {}.type)
                                ?: ArrayList()
                        artistArra.forEach {
                            title = title.plus("\n").plus(it.name)
                        }
                        mResult = title
                    } else {
                        mResult = "${audio!!.title} \n not found"
                    }
                    Log.d(TAG, "Lyrics Data : $mResult")
                }

                else -> {
                }
            }
        }
    }

    internal inner class RecThread : Thread() {

        override fun run() {
            val config = HashMap<String, Any>()
            config["access_key"] = "45e004f0594a83b3877151b4f251577f"
            config["access_secret"] = "0Fsre8aD9P8beEvIPyxPJw2Bk2sgot79uUFNdI5Q"
            config["host"] = "identify-ap-southeast-1.acrcloud.com"
            config["debug"] = false
            config["timeout"] = 5

            val path = audio!!.data

            val re = ACRCloudRecognizer(config)
            Log.d(TAG, "Got song path :$path")
            if (File(path).exists()) {
                Log.d(TAG, "#File exist")
            } else {
                Log.d(TAG, "#File not exist")
            }

            val result = re.recognizeByFile("$path", 20)

            println(result)

            try {
                val msg = Message()
                msg.obj = result

                msg.what = 1
                mHandler.sendMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun removeGarbageFrom(string: String): String {
        var commentStr = string
        if (commentStr.contains("(")) {
            commentStr = commentStr.replaceRange(commentStr.indexOf("("), commentStr.indexOf(")") + 1, "")
        }
        if (commentStr.contains("[")) {
            commentStr = commentStr.replaceRange(commentStr.indexOf("["), commentStr.indexOf("]") + 1, "")
        }
        val urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
        val p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE)
        val m = p.matcher(commentStr)
        var i = 0
        while (m.find()) {
            commentStr = commentStr.replace(m.group(i).toRegex(), "").trim { it <= ' ' }
            i++
        }
        return commentStr
    }

}