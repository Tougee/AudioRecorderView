package com.tougee.recorderview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.BounceInterpolator
import android.widget.ImageView

class MicImageView : ImageView {

    companion object {
        const val EXPAND = 2f
    }

    private val expandBg: Drawable

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

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                expand()
            }
        }
        return true
    }

    private fun expand() {
        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    background =
                    callback?.onExpand()
                }
            })
            playTogether(expandX, expandY)
        }
    }

    private fun shrink() {
        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    callback?.onShrink()
                }
            })
            playTogether(shrinkX, shrinkY)
        }
    }

    interface MicImageCallback {
        fun onExpand()
        fun onShrink()
    }
}