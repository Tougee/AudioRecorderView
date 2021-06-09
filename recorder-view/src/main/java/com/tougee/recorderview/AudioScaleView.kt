@file:Suppress("unused")

package com.tougee.recorderview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArrayList

class AudioScaleView : View {

    companion object {
        const val SCALE_COLOR = Color.GRAY
        const val COUNT = 60
        const val ITEM_MAX_DURATION = 1000
    }

    private val scales = CopyOnWriteArrayList<Float>()
    private val animQueue = ConcurrentLinkedDeque<Float>()
    private var count = COUNT
    private var itemWidth = 0f
    private var curStopY = 0f
    private var animPos = 0f
    private var animating = false

    @Suppress("MemberVisibilityCanBePrivate")
    var canceled = false
        set(value) {
            field = value
            if (value) {
                scales.clear()
                animQueue.clear()
                curStopY = 0f
                animPos = 0f
                animating = false
                invalidate()
            }
        }

    private var scaleColor = SCALE_COLOR

    private val scalePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = scaleColor
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (itemWidth == 0f) {
            itemWidth = measuredWidth / (count * 2f + 1)
            scalePaint.strokeWidth = itemWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (canceled) {
            canceled = false
            canvas.drawColor(Color.TRANSPARENT)
            return
        }

        if (scales.isEmpty()) return

        if (animQueue.isNotEmpty()) {
            if (animating) {
                val startX = itemWidth + animPos * itemWidth * 2
                canvas.drawLine(startX, height.toFloat(), startX, curStopY, scalePaint)
            } else {
                animating = true
                startScaleAnim()
            }
        }

        scales.forEachIndexed { i, h ->
            if (i >= animPos) return@forEachIndexed

            val startX = itemWidth + i * itemWidth * 2
            canvas.drawLine(startX, height.toFloat(), startX, height - h, scalePaint)
        }
    }

    private fun startScaleAnim() {
        val h = animQueue.peekLast() ?: return
        val valueAnimator = ValueAnimator.ofFloat(0f, h).apply {
            duration = ((h / height) * ITEM_MAX_DURATION).toLong()
            interpolator = AccelerateInterpolator()
        }
        valueAnimator?.addUpdateListener {
            curStopY = height - it.animatedValue as Float
            invalidate()
        }
        valueAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                animQueue.pollLast()
                animPos++
                animating = false
            }

            override fun onAnimationCancel(animation: Animator?) {
                animQueue.pollLast()
                animPos++
                animating = false
            }
        })
        valueAnimator?.start()
    }

    @Suppress("unused")
    fun addScale(h: Float) {
        if (scales.size >= count) return

        scales.add(h)
        animQueue.offerFirst(h)
        postInvalidate()
    }
}
