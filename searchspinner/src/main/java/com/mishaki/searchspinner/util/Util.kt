package com.mishaki.searchspinner.util

import android.content.Context

fun String?.isEmpty(): Boolean {
    if (this == null) {
        return true
    }
    return this.trim().length == 0
}

fun String?.isNotEmpty(): Boolean = !isEmpty()

fun Context.getDimension(resId: Int) = resources.getDimension(resId)
