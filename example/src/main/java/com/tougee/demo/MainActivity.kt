package com.tougee.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter
import java.util.Random
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity(), AudioRecordView.AudioRecorderCallback {
    companion object {
        const val REQUEST_CAMERA_PERMISSION_RESULT = 123
    }

    private val file: File by lazy {
        val f = File("$externalCacheDir${File.separator}audio.pcm")
        if (!f.exists()) {
            f.createNewFile()
        }
        f
    }

    private val tmpFile: File by lazy {
        val f = File("$externalCacheDir${File.separator}tmp.pcm")
        if (!f.exists()) {
            f.createNewFile()
        }
        f
    }

    private lateinit var audioRecord: AudioRecorder
    private var audioPlayer: AudioPlayer? = null

    private var timer: Timer?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        file_tv.text = "path: ${file.absolutePath}\nlength: ${file.length()}"
        record_view.callback = this
        play_iv.setOnClickListener {
            if (audioPlayer != null && audioPlayer!!.isPlaying) {
                play_iv.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                audioPlayer!!.stop()
            } else {
                play_iv.setImageResource(R.drawable.ic_stop_black_24dp)
                audioPlayer = AudioPlayer(FileInputStream(file))
                audioPlayer!!.start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermission()
    }

    override fun onRecordStart() {
        super.onStart()
        toast("onRecordStart")

        clearFile(tmpFile)

        audioRecord = AudioRecorder(ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_WRITE))
        record_view.start()
        audioRecord.start()

        val r = Random()
        val x = dip(15f).toInt()
        timer = fixedRateTimer("scale", false, 0, 1000, {
            val h = r.nextInt(x).toFloat()
            record_view.addScale(h)
        })
    }

    override fun onEnd() {
        toast("onEnd")
        audioRecord.stop()
        timer?.cancel()

        tmpFile.copyTo(file, true)
    }

    override fun onCancel() {
        toast("onCancel")
        audioRecord.stop()
        timer?.cancel()
    }

    private fun clearFile(f: File) {
        PrintWriter(f).run {
            print("")
            close()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this,
                        "App required access to audio", Toast.LENGTH_SHORT).show()
                }
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CAMERA_PERMISSION_RESULT)
            }
        } else {
            // put your code for Version < Marshmallow
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext,
                    "Application will not have audio on record", Toast.LENGTH_SHORT).show()
            }
        }
    }
}