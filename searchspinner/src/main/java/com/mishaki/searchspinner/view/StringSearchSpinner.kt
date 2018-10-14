package com.mishaki.searchspinner.view

import android.content.Context
import android.util.AttributeSet

class StringSearchSpinner:SearchSpinner<String>{
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}