package com.tougee.recorderview

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_audio_record.view.*

class AudioRecordView: LinearLayout {

    private val blinkingDrawable: BlinkingDrawable

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

        orientation = VERTICAL
    }

    fun addScale(h: Float) = audio_scale.addScale(h)

    fun start() {
        blinkingDrawable.blinking()
    }
}