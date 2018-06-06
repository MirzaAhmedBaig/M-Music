package com.mirza.mmusic.extensions

import android.graphics.Color

/**
 * Created by mirzaahmed on 18/3/18.
 */
fun manipulateColor(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = Math.round(Color.red(color) * factor)
    val g = Math.round(Color.green(color) * factor)
    val b = Math.round(Color.blue(color) * factor)
    return Color.argb(a,
            Math.min(r, 255),
            Math.min(g, 255),
            Math.min(b, 255))
}

/**
 * @param color input color
 * @param offset float value from 0 to lighten color more offset more light color
 */
fun getLighterShadeColor(color: Int, offset: Float = 2f): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] *= offset
    return Color.HSVToColor(hsv)
}