package com.tougee.recorderview

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.view_audio_record.view.*
import kotlinx.android.synthetic.main.view_slide_panel.view.*

@Suppress("MemberVisibilityCanBePrivate")
class AudioRecordView : FrameLayout {

    companion object {
        const val RECORD_DELAY = 200L
        const val RECORD_TIP_MILLIS = 2000L

        const val ANIMATION_DURATION = 250L
    }

    lateinit var callback: Callback
    lateinit var activity: Activity

    private var isRecording = false
    private var upBeforeGrant = false

    @DrawableRes
    var micIcon: Int = R.drawable.ic_record_mic_black
        set(value) {
            if (value == field) return

            field = value
            record_ib.setImageResource(value)
        }

    @DrawableRes
    var micActiveIcon: Int = R.drawable.ic_record_mic_white
        set(value) {
            if (value == field) return

            field = value
            record_circle.audioDrawable = ResourcesCompat.getDrawable(resources, value, null)!!
        }

    var micHintEnable: Boolean = true

    @ColorInt
    var micHintColor: Int = ContextCompat.getColor(context, android.R.color.white)
        set(value) {
            if (value == field) return

            field = value
            record_tip_tv.setTextColor(value)
        }

    var micHintText: String = context.getString(R.string.hold_to_record_audio)
        set(value) {
            if (value == field) return

            field = value
            record_tip_tv.text = micHintText
        }

    @DrawableRes
    var micHintBg: Int = R.drawable.bg_record_tip
        set(value) {
            if (value == field) return

            field = value
            record_tip_tv.setBackgroundResource(value)
        }

    @ColorInt
    var circleColor: Int = ContextCompat.getColor(context, R.color.color_blue)
        set(value) {
            if (value == field) return

            field = value
            record_circle.circlePaint.color = value
        }

    @ColorInt
    var cancelIconColor: Int = ContextCompat.getColor(context, R.color.color_blink)
        set(value) {
            if (value == field) return

            field = value
            record_circle.cancelIconPaint.color = value
        }

    @ColorInt
    var blinkColor: Int = ContextCompat.getColor(context, R.color.color_blink)
        set(value) {
            if (value == field) return

            field = value
            slide_panel.updateBlinkDrawable(value)
        }

    var slideCancelText: String = context.getString(R.string.slide_to_cancel)
        set(value) {
            if (value == field) return

            field = value
            slide_cancel_tv.text = value
        }

    var cancelText: String = context.getString(R.string.cancel)
        set(value) {
            if (value == field) return

            field = value
            cancel_tv.text = value
        }

    var vibrationEnable: Boolean = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_audio_record, this, true)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AudioRecordView)
        if (ta.hasValue(R.styleable.AudioRecordView_mic_icon)) {
            micIcon = ta.getResourceId(R.styleable.AudioRecordView_mic_icon, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_mic_active_icon)) {
            micActiveIcon = ta.getResourceId(R.styleable.AudioRecordView_mic_active_icon, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_mic_hint_enable)) {
            micHintEnable = ta.getBoolean(R.styleable.AudioRecordView_mic_hint_enable, true)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_mic_hint_text)) {
            ta.getString(R.styleable.AudioRecordView_mic_hint_text)?.let {
                micHintText = it
            }
        }
        if (ta.hasValue(R.styleable.AudioRecordView_mic_hint_color)) {
            micHintColor = ta.getColor(R.styleable.AudioRecordView_mic_hint_color, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_mic_hint_bg)) {
            micHintBg = ta.getResourceId(R.styleable.AudioRecordView_mic_hint_bg, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_circle_color)) {
            circleColor = ta.getColor(R.styleable.AudioRecordView_circle_color, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_cancel_icon_color)) {
            cancelIconColor = ta.getColor(R.styleable.AudioRecordView_cancel_icon_color, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_blink_color)) {
            blinkColor = ta.getColor(R.styleable.AudioRecordView_blink_color, 0)
        }
        if (ta.hasValue(R.styleable.AudioRecordView_slide_cancel_text)) {
            ta.getString(R.styleable.AudioRecordView_slide_cancel_text)?.let {
                slideCancelText = it
            }
        }
        if (ta.hasValue(R.styleable.AudioRecordView_cancel_text)) {
            ta.getString(R.styleable.AudioRecordView_cancel_text)?.let {
                cancelText = it
            }
        }
        if (ta.hasValue(R.styleable.AudioRecordView_vibration_enable)) {
            vibrationEnable = ta.getBoolean(R.styleable.AudioRecordView_vibration_enable, true)
        }

        ta.recycle()

        slide_panel.callback = chatSlideCallback
        record_circle.callback = recordCircleCallback
        record_ib.setOnTouchListener(recordOnTouchListener)
    }

    @Suppress("unused")
    fun cancelExternal() {
        removeCallbacks(recordRunnable)
        cleanUp()
        updateRecordCircleAndSendIcon()
        slide_panel.parent.requestDisallowInterceptTouchEvent(false)
    }

    fun setTimeoutSeconds(seconds: Int) {
        slide_panel?.timeoutSeconds = seconds
    }

    private fun cleanUp() {
        startX = 0f
        originX = 0f
        isRecording = false
    }

    private fun handleCancelOrEnd(cancel: Boolean) {
        if (cancel) callback.onRecordCancel() else callback.onRecordEnd()
        if (vibrationEnable) {
            context.vibrate(longArrayOf(0, 10))
        }
        cleanUp()
        updateRecordCircleAndSendIcon()
    }

    private fun updateRecordCircleAndSendIcon() {
        if (isRecording) {
            record_circle.visibility = View.VISIBLE
            record_circle.setAmplitude(.0)
            ObjectAnimator.ofFloat(record_circle, "scale", 1f).apply {
                interpolator = DecelerateInterpolator()
                duration = 200
                addListener(
                    onEnd = {
                        record_circle.visibility = View.VISIBLE
                    },
                    onCancel = {
                        record_circle.visibility = View.VISIBLE
                    }
                )
            }.start()
            record_ib.animate().setDuration(200).alpha(0f).start()
            slide_panel.onStart()
        } else {
            ObjectAnimator.ofFloat(record_circle, "scale", 0f).apply {
                interpolator = AccelerateInterpolator()
                duration = 200
                addListener(
                    onEnd = {
                        record_circle.visibility = View.GONE
                        record_circle.setSendButtonInvisible()
                    },
                    onCancel = {
                        record_circle.visibility = View.GONE
                        record_circle.setSendButtonInvisible()
                    }
                )
            }.start()
            record_ib.animate().setDuration(200).alpha(1f).start()
            slide_panel.onEnd()
        }
    }

    private fun clickSend() {
        if (micHintEnable && record_tip_tv.visibility == View.INVISIBLE) {
            record_tip_tv.fadeIn(ANIMATION_DURATION)
            if (vibrationEnable) {
                context.vibrate(longArrayOf(0, 10))
            }
            postDelayed(hideRecordTipRunnable, RECORD_TIP_MILLIS)
        } else {
            removeCallbacks(hideRecordTipRunnable)
        }
        postDelayed(hideRecordTipRunnable, RECORD_TIP_MILLIS)
    }

    private var startX = 0f
    private var originX = 0f
    private var startTime = 0L
    private var triggeredCancel = false
    private var hasStartRecord = false
    private var locked = false
    private var maxScrollX = context.dip(100f)

    @SuppressLint("ClickableViewAccessibility")
    private val recordOnTouchListener = OnTouchListener { _, event ->
        when (event.action) {
            ACTION_DOWN -> {
                if (record_circle.sendButtonVisible) {
                    return@OnTouchListener false
                }

                originX = event.rawX
                startX = event.rawX
                val w = slide_panel.slideWidth
                if (w > 0) {
                    maxScrollX = w
                }
                startTime = System.currentTimeMillis()
                hasStartRecord = false
                locked = false
                postDelayed(recordRunnable, RECORD_DELAY)
                return@OnTouchListener true
            }
            ACTION_MOVE -> {
                if (record_circle.sendButtonVisible || !hasStartRecord) return@OnTouchListener false

                val x = record_circle.setLockTranslation(event.y)
                if (x == 2) {
                    ObjectAnimator.ofFloat(
                        record_circle, "lockAnimatedTranslation",
                        record_circle.startTranslation
                    ).apply {
                        duration = 150
                        interpolator = DecelerateInterpolator()
                        doOnEnd { locked = true }
                    }.start()
                    slide_panel.toCancel()
                    return@OnTouchListener false
                }

                val moveX = event.rawX
                if (moveX != 0f) {
                    slide_panel.slideText(startX - moveX)
                    if (originX - moveX > maxScrollX) {
                        removeCallbacks(recordRunnable)
                        removeCallbacks(checkReadyRunnable)
                        handleCancelOrEnd(true)
                        slide_panel.parent.requestDisallowInterceptTouchEvent(false)
                        triggeredCancel = true
                        return@OnTouchListener false
                    }
                }
                startX = moveX
            }
            ACTION_UP, ACTION_CANCEL -> {
                if (triggeredCancel) {
                    cleanUp()
                    triggeredCancel = false
                    return@OnTouchListener false
                }

                if (!hasStartRecord) {
                    removeCallbacks(recordRunnable)
                    removeCallbacks(checkReadyRunnable)
                    cleanUp()
                    if (!post(sendClickRunnable)) {
                        clickSend()
                    }
                } else if (hasStartRecord && !locked && System.currentTimeMillis() - startTime < 500) {
                    removeCallbacks(recordRunnable)
                    removeCallbacks(checkReadyRunnable)
                    // delay check sendButtonVisible
                    postDelayed(
                        {
                            if (!record_circle.sendButtonVisible) {
                                handleCancelOrEnd(true)
                            } else {
                                record_circle.sendButtonVisible = false
                            }
                        },
                        200
                    )
                    return@OnTouchListener false
                }

                if (isRecording && !record_circle.sendButtonVisible) {
                    handleCancelOrEnd(event.action == ACTION_CANCEL)
                } else {
                    cleanUp()
                }

                if (!callback.isReady()) {
                    upBeforeGrant = true
                }
            }
        }
        return@OnTouchListener true
    }

    private val sendClickRunnable = Runnable { clickSend() }

    private val hideRecordTipRunnable = Runnable {
        if (record_tip_tv.visibility == View.VISIBLE) {
            record_tip_tv.fadeOut(ANIMATION_DURATION)
        }
    }

    private val recordRunnable: Runnable by lazy {
        Runnable {
            hasStartRecord = true
            removeCallbacks(hideRecordTipRunnable)
            post(hideRecordTipRunnable)

            if (ContextCompat.checkSelfPermission(activity, (Manifest.permission.RECORD_AUDIO)) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 99)
                return@Runnable
            }

            callback.onRecordStart()
            if (vibrationEnable) {
                context.vibrate(longArrayOf(0, 10))
            }
            upBeforeGrant = false
            post(checkReadyRunnable)
            record_ib.parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    private val checkReadyRunnable: Runnable by lazy {
        Runnable {
            if (callback.isReady()) {
                if (upBeforeGrant) {
                    upBeforeGrant = false
                    return@Runnable
                }
                isRecording = true
                updateRecordCircleAndSendIcon()
                record_circle.setLockTranslation(10000f)
            } else {
                postDelayed(checkReadyRunnable, 50)
            }
        }
    }

    private val chatSlideCallback = object : SlidePanelView.Callback {
        override fun onTimeout() {
            handleCancelOrEnd(false)
        }

        override fun onCancel() {
            handleCancelOrEnd(true)
        }
    }

    private val recordCircleCallback = object : RecordCircleView.Callback {
        override fun onSend() {
            handleCancelOrEnd(false)
        }

        override fun onCancel() {
            handleCancelOrEnd(true)
        }
    }

    interface Callback {
        fun onRecordStart()
        fun isReady(): Boolean
        fun onRecordEnd()
        fun onRecordCancel()
    }
}
