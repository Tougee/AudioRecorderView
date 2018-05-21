package com.tougee.recorderview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.BounceInterpolator
import android.widget.ImageView

class MicImageView : ImageView {

    companion object {
        const val EXPAND = 2f
    }

    var callback: MicImageCallback? = null

    private val expandX = ObjectAnimator.ofFloat(this@MicImageView, "scaleX", 1f, EXPAND).apply {
        duration = 400
        interpolator = BounceInterpolator()
    }
    private val expandY = ObjectAnimator.ofFloat(this@MicImageView, "scaleY", 1f, EXPAND).apply {
        duration = 400
        interpolator = BounceInterpolator()
    }

    private val shrinkX = ObjectAnimator.ofFloat(this, "scaleX", EXPAND, 1f).apply {
        duration = 400
        interpolator = BounceInterpolator()
    }

    private val shrinkY = ObjectAnimator.ofFloat(this, "scaleY", EXPAND, 1f).apply {
        duration = 400
        interpolator = BounceInterpolator()
    }

    private val defaultMaxScrollX = context.dip(150f).toInt()
    private var startX = 0f
    private var originX = 0f
    private var ignoreTouch = false
    private var maxScrollX = defaultMaxScrollX

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (ignoreTouch && (event.action != MotionEvent.ACTION_CANCEL
                && event.action != MotionEvent.ACTION_UP)) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                originX = event.rawX
                maxScrollX = callback?.onStart() ?: defaultMaxScrollX
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.rawX
                if (moveX != 0f) {
                    callback?.onSlide(startX - moveX)
                    if (originX - moveX >= maxScrollX) {
                        ignoreTouch = true
                        return true
                    }
                }
                startX = moveX
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                startX = 0f
                originX = 0f
                callback?.onEnd()
                ignoreTouch = false
            }
        }
        return true
    }

    private fun expand() {
        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    callback?.onStart()
                }
            })
            playTogether(expandX, expandY)
        }
    }

    private fun shrink() {
        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    callback?.onEnd()
                }
            })
            playTogether(shrinkX, shrinkY)
        }
    }

    interface MicImageCallback {
        fun onStart(): Int
        fun onSlide(x: Float)
        fun onEnd()
    }
}