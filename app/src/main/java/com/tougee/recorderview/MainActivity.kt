package com.tougee.recorderview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Random
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity(), AudioRecordView.AudioRecorderCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        record_view.start()
        record_view.callback = this
    }

    override fun onResume() {
        super.onResume()
        val r = Random()
        val x = dip(15f).toInt()
        fixedRateTimer("scale", false, 0, 1000, {
            val h = r.nextInt(x).toFloat()
            record_view.addScale(h)
        })
    }
    override fun onEnd() {
        toast("onEnd")
    }

    override fun onCancel() {
        toast("onCancel")
    }
}
