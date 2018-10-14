package com.mishaki.searchspinner.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mishaki.searchspinner.R
import com.mishaki.searchspinner.util.getDimension

open class SearchSpinnerAdapter<T>(context: Context) : BaseAdapter() {
    private val TAG = "SearchSpinnerAdapterMsg"
    //当选择后设置选择的背景
    protected open val selectList = ArrayList<Boolean>()
    open var list: ArrayList<T> = ArrayList()
        set(list) {
            field = list
            selectList.clear()
            (0 until list.size).mapTo(selectList) { false }
        }

    open fun setSelect(position: Int) {
        for (i in 0 until selectList.size) {
            selectList[i] = false
        }
        selectList[position] = true
    }

    fun cancelSelect() {
        repeat(selectList.size) {
            selectList[it] = false
        }
    }

    //选中的背景的颜色
    var selectColor: Int = 0xffaaaaaa.toInt()

    var textSize: Float = 30f
    var textSizeUnit: Int = TypedValue.COMPLEX_UNIT_PX
    var height: Int = -1
    var gravity: Int = -1
    var paddingLeft = context.getDimension(R.dimen.search_spinner_pl).toInt()
    var paddingRight = context.getDimension(R.dimen.search_spinner_pr).toInt()
        set(paddingRight) {
            field = paddingRight / 2
        }
    var paddingTop = 24
    var paddingBottom = paddingTop

    open var backgroundColor: Int = -1
        set(backgroundColor) {
            reset()
            field = backgroundColor
        }

    open var backgroundResource: Int = R.drawable.ripple_white
        set(backgroundResource) {
            reset()
            field = backgroundResource
        }

    open var backgroundDrawable: Drawable? = null
        set(backgroundDrawable) {
            reset()
            field = backgroundDrawable
        }

    open var backgroundDrawableId: Int = -1
        set(backgroundDrawableId) {
            reset()
            field = backgroundDrawableId
        }

    protected open fun reset() {
        backgroundColor = -1
        backgroundResource = -1
        backgroundDrawable = null
        backgroundDrawableId = -1
    }

//    var textColor: Int = 0xff000000.toInt()
    var textColor: Int = -1

    override fun getItem(position: Int): T = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = list.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val textView: TextView
        if (convertView == null) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_spinner, parent, false)
        } else {
            view = convertView
        }
        textView = view as TextView
        val obj: T = list[position]
        if (obj is CharSequence) {
            textView.text = obj
        } else {
            textView.text = obj.toString()
        }
        if (gravity != -1){
            textView.gravity = gravity
        }
        textView.setTextColor(textColor)
        textView.setTextSize(textSizeUnit, textSize)
        textView.ellipsize = TextUtils.TruncateAt.MARQUEE
        if (height != -1) {
            view.height = height
        }
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        if (selectList[position]) {
            view.setBackgroundColor(selectColor)
        } else {
            when {
                backgroundColor != -1 -> view.setBackgroundColor(backgroundColor)
                backgroundResource != -1 -> view.setBackgroundResource(backgroundResource)
                backgroundDrawable != null -> view.setBackgroundDrawable(backgroundDrawable!!)
                backgroundDrawableId != -1 -> view.setBackgroundDrawable(ContextCompat.getDrawable(parent.context, backgroundDrawableId)!!)
                else -> view.setBackgroundResource(R.drawable.ripple_white)
            }
        }
        return view
    }
}