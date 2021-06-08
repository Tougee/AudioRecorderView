package com.tougee.demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.tougee.demo.databinding.ActivityMainBinding
import com.tougee.recorderview.AudioRecordView
import com.tougee.recorderview.toast
import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter

class MainActivity : AppCompatActivity(), AudioRecordView.Callback {

    companion object {
        const val REQUEST_CAMERA_PERMISSION_RESULT = 123
    }
    lateinit var binding: ActivityMainBinding

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

    private var audioRecord: AudioRecorder? = null
    private var audioPlayer: AudioPlayer? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fileTv.text = "path: ${file.absolutePath}\nlength: ${file.length()}"
        binding.recordView.apply {
            activity = this@MainActivity
            callback = this@MainActivity

            // micIcon = R.drawable.ic_chevron_left_gray_24dp
            // micActiveIcon = R.drawable.ic_play_arrow_black_24dp
            // micHintEnable = false
            // micHintText = "Custom hint text"
            // micHintColor = ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_light)
            // micHintBg = R.drawable.ic_launcher_background
            // blinkColor = ContextCompat.getColor(this@MainActivity, R.color.color_blue)
            // circleColor = ContextCompat.getColor(this@MainActivity, R.color.color_blink)
            // cancelIconColor = ContextCompat.getColor(this@MainActivity, R.color.color_blue)
            // slideCancelText = "Custom Slide to cancel"
            // cancelText = "Custom Cancel"
            // vibrationEnable = false
        }
        binding.recordView.setTimeoutSeconds(20)
        binding.playIv.setOnClickListener {
            if (audioPlayer != null && audioPlayer!!.isPlaying) {
                binding.playIv.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                audioPlayer!!.stop()
            } else {
                binding.playIv.setImageResource(R.drawable.ic_stop_black_24dp)
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
        toast("onRecordStart")

        clearFile(tmpFile)

        audioRecord = AudioRecorder(ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_WRITE))
        audioRecord?.start()
    }

    override fun isReady() = true

    override fun onRecordEnd() {
        toast("onEnd")
        audioRecord?.stop()

        tmpFile.copyTo(file, true)
    }

    override fun onRecordCancel() {
        toast("onCancel")
        audioRecord?.stop()
    }

    private fun clearFile(f: File) {
        PrintWriter(f).run {
            print("")
            close()
        }
    }

    private fun requestPermission() {
        @Suppress("ControlFlowWithEmptyBody")
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