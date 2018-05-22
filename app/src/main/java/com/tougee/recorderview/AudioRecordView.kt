package com.tougee.recorderview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_audio_record.view.*

class AudioRecordView: RelativeLayout {

    private val blinkingDrawable: BlinkingDrawable
    private var timeValue = 0

    var callback: AudioRecorderCallback? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_audio_record, this, true)

        var blinkColor = ContextCompat.getColor(context, R.color.color_blink)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AudioRecordView)
        ta?.let {
            if (ta.hasValue(R.styleable.AudioRecordView_blink_color)) {
                blinkColor = ta.getColor(R.styleable.AudioRecordView_blink_color,
                    ContextCompat.getColor(context, R.color.color_blink))
            }

            ta.recycle()
        }

        val blinkSize = context.resources.getDimensionPixelSize(R.dimen.blink_size)
        blinkingDrawable = BlinkingDrawable(blinkColor).apply {
            setBounds(0, 0, blinkSize, blinkSize)
        }
        time_tv.setCompoundDrawables(blinkingDrawable, null, null, null)
        mic_view.callback = micCallback
    }

    fun addScale(h: Float) {
        audio_scale.addScale(h)

        if (timeValue++ > 60) return
        post {
            time_tv.text = context.getString(R.string.time, timeValue)
        }
    }

    fun start() {
        blinkingDrawable.blinking()
    }

    private val micCallback: MicImageView.MicImageCallback = object : MicImageView.MicImageCallback {

        override fun onStart(): Int {
            context.vibrate(longArrayOf(0, 30))
            bottom_rl.visibility = View.VISIBLE
            bottom_rl.translationX = (bottom_rl.width).toFloat()
            bottom_rl.animate().apply {
                translationX(0f)
                interpolator = DecelerateInterpolator()
                duration = 200
            }.start()

            return slide_ll.width
        }

        override fun onSlide(x: Float) {
            val preX = slide_ll.scrollX
            if (preX + x < 0) {
                slide_ll.scrollBy(0, 0)
            } else {
                slide_ll.scrollBy(x.toInt(), 0)
            }
            val alpha = slide_ll.scrollX.toFloat() / slide_ll.width
            slide_ll.alpha = 1 - alpha
        }

        override fun onEnd(cancel: Boolean) {
            context.vibrate(longArrayOf(0, 30))
             bottom_rl.animate().apply {
                translationX(bottom_rl.width.toFloat())
                interpolator = AccelerateInterpolator()
                duration = 200
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        handleEnd(cancel)
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        handleEnd(cancel)
                    }
                })
            }.start()
        }
    }

    private fun handleEnd(cancel: Boolean) {
        slide_ll.scrollX = 0
        slide_ll.alpha = 1f
        if (cancel) {
            callback?.onCancel()
        } else {
            callback?.onEnd()
        }
    }

    interface AudioRecorderCallback {
        fun onEnd()
        fun onCancel()
    }
}