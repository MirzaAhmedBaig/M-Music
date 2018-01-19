package com.mirza.mmusic.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by MIRZA on 19/10/17.
 */
class Audio(var data: String?, var title: String?, var album: String?, var artist: String?, var endTime: String, var duration: Long, var playing: Boolean, var isFav: Boolean) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt() == 1,
            parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(data)
        parcel.writeString(title)
        parcel.writeString(album)
        parcel.writeString(artist)
        parcel.writeString(endTime)
        parcel.writeLong(duration)
        parcel.writeByte(if (playing) 1 else 0)
        parcel.writeByte(if (isFav) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Audio> {
        override fun createFromParcel(parcel: Parcel): Audio {
            return Audio(parcel)
        }

        override fun newArray(size: Int): Array<Audio?> {
            return arrayOfNulls(size)
        }
    }
}