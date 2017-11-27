/*
package braincrush.mirza.com.MMusic.fragments

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import braincrush.mirza.com.MMusic.R
import braincrush.mirza.com.MMusic.models.Audio
import kotlinx.android.synthetic.main.fragment_music_player.*
import kotlinx.android.synthetic.main.fragment_music_player.view.*


class MusicPlayerFragment : Fragment() {
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var data: Audio



    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =inflater!!.inflate(R.layout.fragment_music_player, container, false)
        if (arguments != null) {
            data = arguments.getSerializable(ARG_DATA) as Audio
        }

        view.maxLayout.alpha = 0f
        view.bottom_sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mBottomSheetBehavior = BottomSheetBehavior.from(view.bottom_sheet)
        mBottomSheetBehavior.isHideable = false
        mBottomSheetBehavior.skipCollapsed = false
        mBottomSheetBehavior.peekHeight = 200
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    view.miniLayout!!.visibility = View.VISIBLE
//                    mainLayout.alpha = 1f
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d(TAG, "Slide Offset :" + slideOffset)
                view.maxLayout!!.alpha = slideOffset
//                mainLayout!!.alpha = (1 - slideOffset)
            }
        })

        view.miniLayout!!.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = view.miniLayout!!.layoutParams as RelativeLayout.LayoutParams
                mBottomSheetBehavior.peekHeight = params.height
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.miniLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    view.miniLayout!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }

            }
        }
        )


        return view
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
*/
