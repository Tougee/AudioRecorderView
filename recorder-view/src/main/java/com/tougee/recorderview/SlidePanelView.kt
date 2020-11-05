package com.tougee.recorderview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_slide_panel.view.*
import kotlin.math.abs

class SlidePanelView : RelativeLayout {

    private val blinkSize = context.resources.getDimensionPixelSize(R.dimen.blink_size)
    private var blinkingDrawable: BlinkingDrawable? = null
    private var timeValue = 0
    private var toCanceled = false
    private var isEnding = false

    var timeoutSeconds = 60

    var callback: Callback? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_slide_panel, this, true)
        setBackgroundColor(Color.WHITE)
        isClickable = true

        updateBlinkDrawable(ContextCompat.getColor(context, R.color.color_blink))
        cancel_tv.setOnClickListener { callback?.onCancel() }
        time_tv.text = 0L.formatMillis()
    }

    fun onStart() {
        visibility = VISIBLE
        translationX = measuredWidth.toFloat()
        val animSet = AnimatorSet().apply {
            interpolator = DecelerateInterpolator()
            duration = 200
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    blinkingDrawable?.blinking()
                    postDelayed(updateTimeRunnable, 200)
                }
            })
        }
        animSet.playTogether(
            ObjectAnimator.ofFloat(this, "translationX", 0f),
            ObjectAnimator.ofFloat(this, "alpha", 1f)
        )
        animSet.start()
    }

    val slideWidth by lazy {
        val location = IntArray(2)
        slide_ll.getLocationOnScreen(location)
        location[0] - context.dip(64f)
    }

    fun slideText(x: Float) {
        val preX = slide_ll.translationX
        if (preX - x > 0) {
            slide_ll.translationX = 0f
        } else {
            slide_ll.translationX -= x * 1.5f
        }
        val alpha = abs(slide_ll.translationX * 1.5f / slide_ll.width)
        slide_ll.alpha = 1 - alpha
    }

    fun toCancel() {
        if (isEnding) return

        val animSet = AnimatorSet().apply {
            duration = 200
            interpolator = DecelerateInterpolator()
        }
        animSet.playTogether(
            ObjectAnimator.ofFloat(slide_ll, "alpha", 0f),
            ObjectAnimator.ofFloat(slide_ll, "translationY", context.dip(20f)),
            ObjectAnimator.ofFloat(cancel_tv, "alpha", 1f),
            ObjectAnimator.ofFloat(cancel_tv, "translationY", -context.dip(20f), 0f)
        )
        animSet.start()
        toCanceled = true
    }

    fun onEnd() {
        isEnding = true
        val animSet = AnimatorSet().apply {
            interpolator = AccelerateInterpolator()
            duration = 200
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    handleEnd()
                    isEnding = false
                }

                override fun onAnimationCancel(animation: Animator?) {
                    handleEnd()
                    isEnding = false
                }
            })
        }
        animSet.playTogether(
            ObjectAnimator.ofFloat(this, "translationX", measuredWidth.toFloat()),
            ObjectAnimator.ofFloat(this, "alpha", 0f)
        )
        animSet.start()
    }

    fun updateBlinkDrawable(color: Int) {
        blinkingDrawable = BlinkingDrawable(color).apply {
            setBounds(0, 0, blinkSize, blinkSize)
        }
        time_tv.setCompoundDrawables(blinkingDrawable, null, null, null)
    }

    private fun handleEnd() {
        toCanceled = false
        cancel_tv.alpha = 0f
        cancel_tv.translationY = 0f
        slide_ll.alpha = 1f
        slide_ll.translationY = 0f
        slide_ll.translationX = 0f

        blinkingDrawable?.stopBlinking()
        removeCallbacks(updateTimeRunnable)
        timeValue = 0
        time_tv.text = 0L.formatMillis()
    }

    private val updateTimeRunnable: Runnable by lazy {
        Runnable {
            if (timeValue >= timeoutSeconds) {
                callback?.onTimeout()
                return@Runnable
            }

            timeValue++
            time_tv.text = (timeValue * 1000L).formatMillis()
            postDelayed(updateTimeRunnable, 1000)
        }
    }

    interface Callback {
        fun onTimeout()
        fun onCancel()
    }
}
