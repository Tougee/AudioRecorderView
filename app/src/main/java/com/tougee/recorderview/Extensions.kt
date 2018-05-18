package com.tougee.recorderview

import android.content.Context

fun Context.dip(value: Float): Float = (value * resources.displayMetrics.density)