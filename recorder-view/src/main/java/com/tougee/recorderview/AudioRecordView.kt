package com.tougee.recorderview

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent.*
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
import com.tougee.recorderview.databinding.ViewAudioRecordBinding
import com.tougee.recorderview.databinding.ViewSlidePanelBinding

@Suppress("MemberVisibilityCanBePrivate")
class AudioRecordView : FrameLayout {

    companion object {
        const val RECORD_DELAY = 200L
        const val RECORD_TIP_MILLIS = 2000L

        const val ANIMATION_DURATION = 250L
    }

    lateinit var callback: Callback
    lateinit var activity: Activity
    lateinit var binding: ViewAudioRecordBinding
    lateinit var _binding: ViewSlidePanelBinding

    private var isRecording = false
    private var upBeforeGrant = false

    @DrawableRes
    var micIcon: Int = R.drawable.ic_record_mic_black
        set(value) {
            if (value == field) return

            field = value
            binding.recordIb.setImageResource(value)
        }

    @DrawableRes
    var micActiveIcon: Int = R.drawable.ic_record_mic_white
        set(value) {
            if (value == field) return

            field = value
            binding.recordCircle.audioDrawable = ResourcesCompat.getDrawable(resources, value, null)!!
        }

    var micHintEnable: Boolean = true

    @ColorInt
    var micHintColor: Int = ContextCompat.getColor(context, android.R.color.white)
        set(value) {
            if (value == field) return

            field = value
            binding.recordTipTv.setTextColor(value)
        }

    var micHintText: String = context.getString(R.string.hold_to_record_audio)
        set(value) {
            if (value == field) return

            field = value
            binding.recordTipTv.text = micHintText
        }

    @DrawableRes
    var micHintBg: Int = R.drawable.bg_record_tip
        set(value) {
            if (value == field) return

            field = value
            binding.recordTipTv.setBackgroundResource(value)
        }

    @ColorInt
    var circleColor: Int = ContextCompat.getColor(context, R.color.color_blue)
        set(value) {
            if (value == field) return

            field = value
            binding.recordCircle.circlePaint.color = value
        }

    @ColorInt
    var cancelIconColor: Int = ContextCompat.getColor(context, R.color.color_blink)
        set(value) {
            if (value == field) return

            field = value
            binding.recordCircle.cancelIconPaint.color = value
        }

    @ColorInt
    var blinkColor: Int = ContextCompat.getColor(context, R.color.color_blink)
        set(value) {
            if (value == field) return

            field = value
            binding.slidePanel.updateBlinkDrawable(value)
        }

    var slideCancelText: String = context.getString(R.string.slide_to_cancel)
        set(value) {
            if (value == field) return

            field = value
            _binding.slideCancelTv.text = value
        }

    var cancelText: String = context.getString(R.string.cancel)
        set(value) {
            if (value == field) return

            field = value
            _binding.cancelTv.text = value
        }

    var vibrationEnable: Boolean = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        binding = ViewAudioRecordBinding.inflate(LayoutInflater.from(context), this)
        _binding = ViewSlidePanelBinding.bind(binding.root)

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

        binding.slidePanel.callback = chatSlideCallback
        binding.recordCircle.callback = recordCircleCallback
        binding.recordIb.setOnTouchListener(recordOnTouchListener)
    }

    @Suppress("unused")
    fun cancelExternal() {
        removeCallbacks(recordRunnable)
        cleanUp()
        updateRecordCircleAndSendIcon()
        binding.slidePanel.parent.requestDisallowInterceptTouchEvent(false)
    }

    fun setTimeoutSeconds(seconds: Int) {
        binding.slidePanel.timeoutSeconds = seconds
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
            binding.recordCircle.visibility = View.VISIBLE
            binding.recordCircle.setAmplitude(.0)
            ObjectAnimator.ofFloat(binding.recordCircle, "scale", 1f).apply {
                interpolator = DecelerateInterpolator()
                duration = 200
                addListener(
                    onEnd = {
                        binding.recordCircle.visibility = View.VISIBLE
                    },
                    onCancel = {
                        binding.recordCircle.visibility = View.VISIBLE
                    }
                )
            }.start()
            binding.recordIb.animate().setDuration(200).alpha(0f).start()
            binding.slidePanel.onStart()
        } else {
            ObjectAnimator.ofFloat(binding.recordCircle, "scale", 0f).apply {
                interpolator = AccelerateInterpolator()
                duration = 200
                addListener(
                    onEnd = {
                        binding.recordCircle.visibility = View.GONE
                        binding.recordCircle.setSendButtonInvisible()
                    },
                    onCancel = {
                        binding.recordCircle.visibility = View.GONE
                        binding.recordCircle.setSendButtonInvisible()
                    }
                )
            }.start()
            binding.recordIb.animate().setDuration(200).alpha(1f).start()
            binding.slidePanel.onEnd()
        }
    }

    private fun clickSend() {
        if (micHintEnable && binding.recordTipTv.visibility == View.INVISIBLE) {
            binding.recordTipTv.fadeIn(ANIMATION_DURATION)
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
                if (binding.recordCircle.sendButtonVisible) {
                    return@OnTouchListener false
                }

                originX = event.rawX
                startX = event.rawX
                val w = binding.slidePanel.slideWidth
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
                if (binding.recordCircle.sendButtonVisible || !hasStartRecord) return@OnTouchListener false

                val x = binding.recordCircle.setLockTranslation(event.y)
                if (x == 2) {
                    ObjectAnimator.ofFloat(
                        binding.recordCircle, "lockAnimatedTranslation",
                        binding.recordCircle.startTranslation
                    ).apply {
                        duration = 150
                        interpolator = DecelerateInterpolator()
                        doOnEnd { locked = true }
                    }.start()
                    binding.slidePanel.toCancel()
                    return@OnTouchListener false
                }

                val moveX = event.rawX
                if (moveX != 0f) {
                    binding.slidePanel.slideText(startX - moveX)
                    if (originX - moveX > maxScrollX) {
                        removeCallbacks(recordRunnable)
                        removeCallbacks(checkReadyRunnable)
                        handleCancelOrEnd(true)
                        binding.slidePanel.parent.requestDisallowInterceptTouchEvent(false)
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
                            if (!binding.recordCircle.sendButtonVisible) {
                                handleCancelOrEnd(true)
                            } else {
                                binding.recordCircle.sendButtonVisible = false
                            }
                        },
                        200
                    )
                    return@OnTouchListener false
                }

                if (isRecording && !binding.recordCircle.sendButtonVisible) {
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
        if (binding.recordTipTv.visibility == View.VISIBLE) {
            binding.recordTipTv.fadeOut(ANIMATION_DURATION)
        }
    }

    private val recordRunnable: Runnable by lazy {
        Runnable {
            hasStartRecord = true
            removeCallbacks(hideRecordTipRunnable)
            post(hideRecordTipRunnable)

            if (ContextCompat.checkSelfPermission(
                    activity,
                    (Manifest.permission.RECORD_AUDIO)
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 99)
                return@Runnable
            }

            callback.onRecordStart()
            if (vibrationEnable) {
                context.vibrate(longArrayOf(0, 10))
            }
            upBeforeGrant = false
            post(checkReadyRunnable)
            binding.recordIb.parent.requestDisallowInterceptTouchEvent(true)
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
                binding.recordCircle.setLockTranslation(10000f)
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
