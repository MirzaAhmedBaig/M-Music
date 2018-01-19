package com.mirza.mmusic

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


/**
 * Created by avantari on 12/1/17.
 */
fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
    return Uri.parse(path)
}

fun createImageFile(activity: Activity): File? {
    val imageFileName = "JPEG_"
    var mFileTemp: File? = null
    val root = activity.getDir("my_sub_dir", Context.MODE_PRIVATE).getAbsolutePath()
    val myDir = File(root + "/Img")
    if (!myDir.exists()) {
        myDir.mkdirs()
    }
    try {
        mFileTemp = File.createTempFile(imageFileName, ".jpg", myDir.absoluteFile)
    } catch (e1: IOException) {
        e1.printStackTrace()
    }

    return mFileTemp
}

fun getDp(size: Int): Int {
    return ((size * Resources.getSystem().displayMetrics.density).toInt())
}

fun isFileExist(path: String): Boolean {
    return File(path).exists()
}