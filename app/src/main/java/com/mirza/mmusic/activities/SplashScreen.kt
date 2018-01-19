package com.mirza.mmusic.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


/**
 * Created by MIRZA on 25/10/17.
 */
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}