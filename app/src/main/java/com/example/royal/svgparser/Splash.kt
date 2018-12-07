package com.example.royal.svgparser

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler = Handler()

        val runnable = Runnable {
            gotoMain()
        }
        handler.postDelayed({
            runOnUiThread(runnable)
        }, 10000)


        start.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            gotoMain()
        }
    }


    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
