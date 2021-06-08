package com.tougee.recorderview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.tougee.recorderview.databinding.ViewSlidePanelBinding
import kotlin.math.abs

class SlidePanelView : RelativeLayout {

    private val blinkSize = context.resources.getDimensionPixelSize(R.dimen.blink_size)
    private var blinkingDrawable: BlinkingDrawable? = null
    private var timeValue = 0
    private var toCanceled = false
    private var isEnding = false

    var timeoutSeconds = 60

    var callback: Callback? = null
    lateinit var binding: ViewSlidePanelBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        binding = ViewSlidePanelBinding.inflate(LayoutInflater.from(context), this)
        setBackgroundColor(Color.WHITE)
        isClickable = true

        updateBlinkDrawable(ContextCompat.getColor(context, R.color.color_blink))
        binding.cancelTv.setOnClickListener { callback?.onCancel() }
        binding.timeTv.text = 0L.formatMillis()
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
        binding.slideLl.getLocationOnScreen(location)
        location[0] - context.dip(64f)
    }

    fun slideText(x: Float) {
        val preX = binding.slideLl.translationX
        if (preX - x > 0) {
            binding.slideLl.translationX = 0f
        } else {
            binding.slideLl.translationX -= x * 1.5f
        }
        val alpha = abs(binding.slideLl.translationX * 1.5f / binding.slideLl.width)
        binding.slideLl.alpha = 1 - alpha
    }

    fun toCancel() {
        if (isEnding) return

        val animSet = AnimatorSet().apply {
            duration = 200
            interpolator = DecelerateInterpolator()
        }
        animSet.playTogether(
            ObjectAnimator.ofFloat(binding.slideLl, "alpha", 0f),
            ObjectAnimator.ofFloat(binding.slideLl, "translationY", context.dip(20f)),
            ObjectAnimator.ofFloat(binding.cancelTv, "alpha", 1f),
            ObjectAnimator.ofFloat(binding.cancelTv, "translationY", -context.dip(20f), 0f)
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
        binding.timeTv.setCompoundDrawables(blinkingDrawable, null, null, null)
    }

    private fun handleEnd() {
        toCanceled = false
        binding.timeTv.alpha = 0f
        binding.cancelTv.translationY = 0f
        binding.slideLl.alpha = 1f
        binding.slideLl.translationY = 0f
        binding.slideLl.translationX = 0f

        blinkingDrawable?.stopBlinking()
        removeCallbacks(updateTimeRunnable)
        timeValue = 0
        binding.timeTv.text = 0L.formatMillis()
    }

    private val updateTimeRunnable: Runnable by lazy {
        Runnable {
            if (timeValue >= timeoutSeconds) {
                callback?.onTimeout()
                return@Runnable
            }

            timeValue++
            binding.timeTv.text = (timeValue * 1000L).formatMillis()
            postDelayed(updateTimeRunnable, 1000)
        }
    }

    interface Callback {
        fun onTimeout()
        fun onCancel()
    }
}
