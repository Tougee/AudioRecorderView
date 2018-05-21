package com.tougee.recorderview

import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_audio_record.view.*

class AudioRecordView: LinearLayout {

    private val blinkingDrawable: BlinkingDrawable
    private var timeValue = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_audio_record, this, true)

        var blinkColor = ContextCompat.getColor(context, R.color.color_blink)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AudioRecordView)
        ta?.let {
            if (ta.hasValue(R.styleable.AudioRecordView_blink_color)) {
                blinkColor = ta.getColor(R.styleable.AudioRecordView_blink_color, ContextCompat.getColor(context, R.color.color_blink))
            }

            ta.recycle()
        }

        val blinkSize = context.resources.getDimensionPixelSize(R.dimen.blink_size)
        blinkingDrawable = BlinkingDrawable(blinkColor).apply {
            setBounds(0, 0, blinkSize, blinkSize)
        }
        time_tv.setCompoundDrawables(blinkingDrawable, null, null, null)
        mic_view.callback = micCallback

        orientation = VERTICAL
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

        override fun onEnd() {
            val valueAnimator = ValueAnimator.ofInt(slide_ll.scrollX, 0).apply {
                duration = 150
                interpolator = AccelerateInterpolator()
                addUpdateListener {
                    val curScrollX = animatedValue as Int
                    slide_ll.scrollTo(curScrollX, 0)
                    val alpha = curScrollX.toFloat() / slide_ll.width
                    slide_ll.alpha = 1 - alpha + slide_ll.alpha
                }
            }
            valueAnimator.start()
        }
    }
}