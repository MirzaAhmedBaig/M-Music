package braincrush.mirza.com.MMusic.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager


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