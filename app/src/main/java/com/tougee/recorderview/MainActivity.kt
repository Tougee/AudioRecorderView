package com.tougee.recorderview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Random
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        record_view.start()
    }

    override fun onResume() {
        super.onResume()
        val r = Random()
        val x = dip(15f).toInt()
        fixedRateTimer("scale", false, 0, 1000, {
            val h = r.nextInt(x).toFloat()
            scale_view.addScale(h)
            record_view.addScale(h)
        })
    }
}
