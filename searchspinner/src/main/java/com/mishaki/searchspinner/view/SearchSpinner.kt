package com.mishaki.searchspinner.view

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.RotateDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.mishaki.searchspinner.R
import com.mishaki.searchspinner.adapter.SearchSpinnerAdapter
import com.mishaki.searchspinner.util.isNotEmpty

open class SearchSpinner<T> : AppCompatTextView, View.OnClickListener {
    protected lateinit var popupWindow: PopupWindow

    protected lateinit var relativeLayout: RelativeLayout

    protected lateinit var searchView: EditText

    protected lateinit var emptyTipView: TextView

    protected lateinit var listView: ListView
    var adapter = SearchSpinnerAdapter<T>(context)
        set(adapter) {
            field = adapter
            initAdapter()
            if (field.list.isEmpty()) {
                field.list = list
            } else {
                list = field.list
            }
            if (adapterTextColor != -1) {
                field.textColor = adapterTextColor
            } else {
                field.textColor = textColors.defaultColor
            }
            if (adapterItemHeight != -1) {
                field.height = adapterItemHeight
            } else {
                field.height = height
            }
            field.gravity = gravity
            listView.adapter = field
            field.setSelect(selectIndex)
            listView.setSelection(selectIndex)
        }

    private var listSelectIndex = 0

    var selectIndex = 0
        get() {
            //当点击搜索list的时候,就会记录搜索list的数据
            //当点击全部数据的list的时候,就会清除该list的数据
            //它的作用是,当搜索后有选择某个item,然后清除EditText里面的内容后
            //计算出上次在搜索的list选择的item在全部数据的list的index
            //因为存搜索数据的list会根据EditText输入的内容变化而变化,所以才用另一个list才保存搜索的数据
            if (!tmpSearchList.isEmpty()) {
                //listSelectIndex的作用很简单,监听ListView的setOnItemSelect事件,记录position
                val selectField = tmpSearchList[listSelectIndex].toString()
                val countList = ArrayList<Int>()
                for (i in 0 until tmpSearchList.size) {
                    //记录相同数据的个数
                    if (tmpSearchList[i].toString() == selectField) {
                        countList.add(i)
                    }
                    if (i == listSelectIndex) {
                        break
                    }
                }
                //如果只有一个toString后相同的,代表这个在列表是唯一的
                if (countList.size == 1) {
                    var index = -1
                    for (i in 0 until list.size) {
                        if (list[i].toString() == selectField) {
                            //返回全部数据的list所在的index
                            index = i
                            break
                        }
                    }
                    return index
                } else {//如果这个数据toString后出现相同的数据
                    var num = countList.size
                    var index = -1
                    for (i in 0 until list.size) {
                        if (list[i].toString() == selectField) {
                            num--
                        }
                        if (num == 0) {
                            index = i
                            break
                        }
                    }
                    return index
                }
            } else {//当空的时候,表示这个listSelectIndex来源于全部数据的index
                return listSelectIndex
            }
        }
    //调用set方法的时候,直接设置全部数据的index,并清除其他所有list
        set(selectIndex) {
            if (selectIndex >= list.size) {
                throw IndexOutOfBoundsException("index:$selectIndex")
            }
            field = selectIndex
            listSelectIndex = selectIndex
            searchView.setText("")
            searchList.clear()
            tmpSearchList.clear()
            adapter.list = list
            adapter.notifyDataSetChanged()
            adapter.setSelect(field)
            listView.setSelection(field)
            text = list[field].toString()
        }

    protected var searchSelectIndex: Int = 0
        get() {
            //获取上次选择item的值
            val selectField = list[selectIndex].toString()
            var count = 0
            for (i in 0..selectIndex) {
                //可能会有相同的值,所以记录一下
                if (list[i].toString() == selectField) {
                    count++
                }
            }
            if (count == 0) {
                return -1
            }
            var index = -1
            for (i in 0 until searchList.size) {
                if (searchList[i].toString() == selectField) {
                    count--
                }
                if (count == 0) {
                    index = i
                    break
                }
            }
            return index
        }

    protected var arrowDrawable: Drawable? = null

    protected var screenHeight: Int = 0
    protected var statusBarHeight = 0

    protected var rootViewResId: Int = 0
    protected var searchViewResId: Int = 0
    protected var listViewResId: Int = 0
    protected var emptyTipViewResId: Int = 0

    protected var ssPaddingVertical = 0
    protected var ssArrowWidth = 0
    protected var ssSelectAdapterColor: Int = 0

    var isIgnoreCase = false

    private var x = 0
    private var y = 0

    var onSelectListener: OnSelectListener<T>? = null

    var list = ArrayList<T>()
        set(list) {
            field = list
            adapter.list = list
            adapter.notifyDataSetChanged()
            if (list.size != 0) {
                text = list[0].toString()
                adapter.setSelect(0)
            }
        }
    protected var searchList = ArrayList<T>()
    //当通过搜索的时候选择item,记录当前搜索的list
    //下次拿所选择的item的selectIndex的时候就判断该集合是否为空
    //不为空表示这个item是从tmpSearchList(searchList)得到的,这时
    //就通过tmpSearchList计算selectIndex,否则直接返回listSelectIndex
    protected var tmpSearchList = ArrayList<T>()
    protected var isSearch = false

    var topPopupAnim = R.style.Search_spinner__top_anim
    var bottomPopupAnim = R.style.Search_spinner__bottom_anim

    var adapterTextColor = 0xff000000.toInt()
        set(adapterTextColor) {
            field = adapterTextColor
            adapter.textColor = field
        }
    var adapterItemHeight = -1
        set(adapterItemHeight) {
            field = adapterItemHeight
            if (field == -1) {
                adapter.height = height
            } else {
                adapter.height = field
            }
        }

    protected var searchViewHeight = -1
    protected var searchViewActualHeight = 0

    private var searchViewMargin = 0

    var tipTextHeight = -1
        set(tipTextHeight) {
            field = tipTextHeight
            try {
                if (field == -1) {
                    emptyTipView.height = height
                } else {
                    emptyTipView.height = field
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SearchSpinner)

        //旋转图片
        val arrow: Drawable? = array.getDrawable(R.styleable.SearchSpinner_arrow)
        //图片的颜色
        val arrowColor = array.getColor(R.styleable.SearchSpinner_arrowColor, 0xff000000.toInt())
        //是否改变旋转图片的颜色
        val changeColor = array.getBoolean(R.styleable.SearchSpinner_changeColor, true)
        //是否隐藏图片
        val hideArrow = array.getBoolean(R.styleable.SearchSpinner_hideArrow, false)
        //空数据的时候提示的文字
        val tipText = array.getString(R.styleable.SearchSpinner_tipText)
        //空数据的时候提示的文字的颜色
        val tipTextColor = array.getColor(R.styleable.SearchSpinner_tipTextColor, 0xff000000.toInt())
        //item选中后的背景色
        ssSelectAdapterColor = array.getColor(R.styleable.SearchSpinner_adapterSelectColor, -1)
        //textView/adapter的item的垂直padding
        ssPaddingVertical = array.getDimension(R.styleable.SearchSpinner_verticalPadding, 0f).toInt()
        //是否设置activity的键盘输入模式为ADJUST_NOTHING
//        val inputAdjustNothing = array.getBoolean(R.styleable.SearchSpinner_inputAdjustNothing, true)
        //搜索的时候是否忽略大小写
        isIgnoreCase = array.getBoolean(R.styleable.SearchSpinner_isIgnoreCase, false)

        adapterTextColor = array.getColor(R.styleable.SearchSpinner_adapterTextColor, 0xff000000.toInt())
        adapterItemHeight = array.getDimension(R.styleable.SearchSpinner_adapterItemHeight, -1f).toInt()
        val adapterItemHeightPercentStr = array.getString(R.styleable.SearchSpinner_adapterItemHeightPercent)
        val adapterItemHeightPercent = calcSize(adapterItemHeightPercentStr)
        if (adapterItemHeightPercent != -1f) {
            adapterItemHeight = adapterItemHeightPercent.toInt()
        }
        searchViewHeight = array.getDimension(R.styleable.SearchSpinner_searchViewHeight, -1f).toInt()
        val searchViewHeightPercentStr = array.getString(R.styleable.SearchSpinner_searchViewHeightPercent)
        val searchViewHeightPercent = calcSize(searchViewHeightPercentStr)
        if (searchViewHeightPercent != -1f) {
            searchViewHeight = searchViewHeightPercent.toInt()
        }
        tipTextHeight = array.getDimension(R.styleable.SearchSpinner_tipTextHeight, -1f).toInt()
        val tipTextHeightPercentStr: String? = array.getString(R.styleable.SearchSpinner_tipTextHeightPercent)
        val tipTextHeightPercent = calcSize(tipTextHeightPercentStr)
        if (tipTextHeightPercent != -1f) {
            tipTextHeight = tipTextHeightPercent.toInt()
        }

        array.recycle()

        setLines(1)
        ellipsize = TextUtils.TruncateAt.END

        if (ssPaddingVertical != 0) {
            super.setPadding(paddingLeft, ssPaddingVertical, paddingRight, ssPaddingVertical)
        }
        super.setOnClickListener(this)

        //方便子类按需重新初始化
        initScreenHeight()
        initStatusBarHeight()
        initLayoutResId()
        initViewResId()
        initPopupWindow()

        ssArrowWidth = if (!hideArrow) {
            initArrowDrawable(arrow, arrowColor, changeColor)
        } else {
            0
        }

        initAdapter()
        /*if (adapterTextColor != -1) {
            adapter.textColor = adapterTextColor
        } else {
            adapter.textColor = textColors.defaultColor
        }*/
        adapter.textColor = adapterTextColor
        initShowView(tipText, tipTextColor)
        adapter.gravity = gravity
        searchViewMargin = dip2px(1f) * 2

        popupWindow.contentView = relativeLayout

        //本来以为在PopupWindow设置的话就没问题,但发现在ScollView搜索的话容易出现y值计算出错的问题
        //所以强制设置吧
        try {
            val activity = context as Activity
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        popupWindow.width = w
        if (adapterItemHeight == -1) {
            adapter.height = h
        } else {
            adapter.height = adapterItemHeight
        }
        if (searchViewHeight == -1) {
            searchView.height = h
            searchViewActualHeight = h
        } else {
            searchView.height = searchViewHeight
            searchViewActualHeight = searchViewHeight
        }
        searchView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        if (tipTextHeight == -1) {
            emptyTipView.height = h
            tipTextHeight = h
        }
        initAdapterPadding()
    }

    override fun setGravity(gravity: Int) {
        super.setGravity(gravity)
        //初始化的时候好像存在adapter空的情况,如果不判断一下会出现空指针异常
        adapter?.gravity = gravity
    }

    /**
     * 获取屏幕高度
     */
    protected open fun initScreenHeight() {
        screenHeight = resources.displayMetrics.heightPixels
    }

    /**
     * 计算通知栏的高度
     */
    protected open fun initStatusBarHeight() {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
    }

    /**
     * 设置根布局
     */
    protected open fun initLayoutResId() {
        rootViewResId = R.layout.popup_search_spinner
    }

    /**
     * 设置3个控件的id
     */
    protected open fun initViewResId() {
        searchViewResId = R.id.popup_search_spinner_et
        listViewResId = R.id.popup_search_spinner_lv
        emptyTipViewResId = R.id.popup_search_spinner_tv
    }

    /**
     * 初始化popupWindow
     */
    protected open fun initPopupWindow() {
        popupWindow = PopupWindow(context)
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.search_popup_bg))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 16f
        }
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        popupWindow.setOnDismissListener {
            animateArrow(false)
        }
    }

    /**
     * 初始化旋转图片
     */
    protected open fun initArrowDrawable(arrowDrawable: Drawable?, color: Int, changeColor: Boolean): Int {
        val tmpArrowDrawable = if (arrowDrawable != null && arrowDrawable is RotateDrawable) {
            arrowDrawable.mutate()
        } else {
            ContextCompat.getDrawable(context, R.drawable.search_arrow)!!.mutate()
        }
        val rect = Rect()
        rect.apply {
            top = 6
            bottom = textSize.toInt() - 8

            val arrowHeight = bottom - top

            val destWidth = tmpArrowDrawable.minimumWidth * (arrowHeight.toFloat() / tmpArrowDrawable.minimumHeight.toFloat())

            val middleWith = paddingRight / 2

            left = middleWith
            right = middleWith + destWidth.toInt()
        }
        tmpArrowDrawable.bounds = rect
        this.arrowDrawable = tmpArrowDrawable
        if (changeColor) {
            this.arrowDrawable!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        super.setCompoundDrawables(null, null, this.arrowDrawable, null)
        return tmpArrowDrawable.minimumWidth
    }

    /**
     * 初始化Adapter
     */
    protected open fun initAdapter() {
        adapter.apply {
            initAdapterPadding()
            if (ssSelectAdapterColor != -1) {
                this.selectColor = ssSelectAdapterColor
            }
            this.textSize = this@SearchSpinner.textSize
        }
    }

    protected open fun initAdapterPadding() {
        adapter.apply {
            this.paddingLeft = this@SearchSpinner.paddingLeft
            this.paddingRight = this@SearchSpinner.paddingRight + ssArrowWidth
            this.paddingTop = ssPaddingVertical
            this.paddingBottom = ssPaddingVertical
        }
    }

    /**
     * 初始化popup显示的View
     */
    protected open fun initShowView(tipText: String?, tipTextColor: Int) {
        relativeLayout = LayoutInflater.from(context).inflate(rootViewResId, null) as RelativeLayout

        searchView = relativeLayout.findViewById(searchViewResId)
        searchView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
//        searchView.setPadding(ssPaddingLeft, ssPaddingVertical / 2, (ssPaddingRight + ssArrowWidth) / 2, ssPaddingVertical / 2)
        searchView.setPadding(paddingLeft, ssPaddingVertical / 2, (paddingRight + ssArrowWidth) / 2, ssPaddingVertical / 2)
        searchView.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_ENTER
        }
        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = searchView.text.toString()
                //当搜索的内容不为空的时候
                if (searchText.isNotEmpty()) {
                    searchList.clear()
                    //将条件符合的内容过滤出来
                    list.filter { it.toString().contains(searchText, isIgnoreCase) }.forEach { searchList.add(it) }
                    adapter.list = searchList
                    adapter.notifyDataSetChanged()
                    isSearch = true
                    //当列表不为空的时候
                    if (!searchList.isEmpty()) {
                        //计算PopupWindow实际弹出的高度
                        if (popupWindow.isShowing) {
                            val popupHeight = calculatePopupWindowHeight(searchList)
                            if (isTop()) {
                                popupWindow.update(x, y - popupHeight, popupWindow.width, popupHeight)
                            } else {
//                            popupWindow.update(x, y + height, popupWindow.width, popupHeight)
                                popupWindow.update(-1, popupHeight)
                            }
                        }
                        listView.visibility = View.VISIBLE
                        emptyTipView.visibility = View.GONE
                        //计算上个选择的item在这个list所在的index.这个计算公式有点复杂,留在下面
                        val index = searchSelectIndex
                        //-1的时候,表示上次选择的item不在这个列表里面
                        if (index != -1) {
                            adapter.setSelect(searchSelectIndex)
                            listView.setSelection(searchSelectIndex)
                        } else {
                            adapter.cancelSelect()
                        }
                    } else {
                        //列表为空,显示用于提示的TextView
                        removeRule(emptyTipView)
                        val param = emptyTipView.layoutParams as RelativeLayout.LayoutParams
                        if (popupWindow.isShowing) {
                            val popupHeight = calculatePopupWindowHeight(searchList) + tipTextHeight
                            if (isTop()) {
                                param.addRule(RelativeLayout.ABOVE, searchViewResId)
                                emptyTipView.layoutParams = param
                                popupWindow.update(x, y - popupHeight, popupWindow.width, popupHeight)
                            } else {
                                param.addRule(RelativeLayout.BELOW, searchViewResId)
                                emptyTipView.layoutParams = param
                                popupWindow.update(popupWindow.width, popupHeight)
                            }
                        }
                        emptyTipView.visibility = View.VISIBLE
                        listView.visibility = View.GONE
                    }
                } else {
                    //当搜索内容为空的时候,显示全部数据
                    if (popupWindow.isShowing) {
                        val popupHeight = calculatePopupWindowHeight(list)
                        if (isTop()) {
                            popupWindow.update(x, y - popupHeight, popupWindow.width, popupHeight)
                        } else {
                            popupWindow.update(-1, popupHeight)
                        }
                    }
                    adapter.list = list
                    adapter.notifyDataSetChanged()
                    isSearch = false
                    adapter.setSelect(selectIndex)
                    listView.setSelection(selectIndex)
                    listView.visibility = View.VISIBLE
                    emptyTipView.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        listView = relativeLayout.findViewById(listViewResId)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            if (isSearch) {
                text = searchList[position].toString()
            } else {
                text = list[position].toString()
            }
            //选择的item不是通过搜索得到的,
            if (!isSearch) {
                tmpSearchList.clear()
            } else {
                tmpSearchList.clear()
                searchList.forEach { tmpSearchList.add(it) }
            }
            listSelectIndex = position
            onSelectListener?.onSelect(this, selectIndex)
            popupWindow.dismiss()
            adapter.setSelect(position)
            listView.setSelection(position)
        }

        emptyTipView = relativeLayout.findViewById(emptyTipViewResId)
        emptyTipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        if (tipText.isNotEmpty()) {
            emptyTipView.text = tipText
        }
        emptyTipView.setTextColor(tipTextColor)
        if (tipTextHeight != -1) {
            emptyTipView.height = tipTextHeight
        }
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        if (adapterTextColor == -1) {
            adapter.textColor = color
        }
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        adapter.textSizeUnit = TypedValue.COMPLEX_UNIT_SP
        adapter.textSize = size
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        adapter.textSizeUnit = unit
        adapter.textSize = size
    }

    private fun animateArrow(shouldRotateUp: Boolean) {
        if (arrowDrawable != null) {
            val start = if (shouldRotateUp) 0 else 10000
            val end = if (shouldRotateUp) 10000 else 0
            //通过查看API文档的注释明白了这个方法的作用
            //参数一:要调用的对象
            //参数二:该对象的要设置的属性
            val animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end)
            animator.start()
        }
    }

    override fun onClick(v: View?) {
        val locations = IntArray(2)
        getLocationOnScreen(locations)
        x = locations[0]
        y = locations[1]

        //移除RelativeLayout限制的所有规则
        removeRule(searchView)
        removeRule(listView)
        //当EditText有输入内容的时候,并且搜索到的内容列表不为空 或者 EditText没有内容
        //表示这个时候ListView里面绝对有数据
        if ((isSearch && !searchList.isEmpty()) || !isSearch) {
            //当处于搜索状态的时候,使用搜索列表的size计算PopupWindow需要弹出的高度,否则使用总数据
            val popupHeight = calculatePopupWindowHeight(if (isSearch) searchList else list)
            listView.adapter = adapter
            //当上方高度大于下方高度的时候
            if (isTop()) {
                //设置弹出动画
                popupWindow.animationStyle = topPopupAnim

                val searchParam = searchView.layoutParams as RelativeLayout.LayoutParams
                //设置EditText在RelativeLayout的底部
                searchParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                searchView.layoutParams = searchParam

                val listParam = listView.layoutParams as RelativeLayout.LayoutParams
                //设置ListView在EditText的上方
                listParam.addRule(RelativeLayout.ABOVE, searchViewResId)
                listView.layoutParams = listParam

                popupWindow.height = popupHeight
                popupWindow.showAtLocation(this, Gravity.START or Gravity.TOP, x, y - popupHeight)
            } else {
                //设置弹出动画
                popupWindow.animationStyle = bottomPopupAnim

                val param = listView.layoutParams as RelativeLayout.LayoutParams
                //设置ListView在EditText的下面
                param.addRule(RelativeLayout.BELOW, searchViewResId)
                listView.layoutParams = param

                popupWindow.height = popupHeight

                popupWindow.showAsDropDown(this)
            }
            //隐藏提示的View,显示ListView
            emptyTipView.visibility = View.GONE
            listView.visibility = View.VISIBLE
            listView.setSelection(listSelectIndex)
        } else {//当没有数据的时候
            val popupHeight = calculatePopupWindowHeight(searchList) + tipTextHeight
            removeRule(emptyTipView)
            val param = emptyTipView.layoutParams as RelativeLayout.LayoutParams
            if (isTop()) {
                popupWindow.animationStyle = topPopupAnim

                val searchParam = searchView.layoutParams as RelativeLayout.LayoutParams
                searchParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                searchView.layoutParams = searchParam

                param.addRule(RelativeLayout.ABOVE, searchViewResId)
                emptyTipView.layoutParams = param

                popupWindow.height = popupHeight
                popupWindow.showAtLocation(this, Gravity.START or Gravity.TOP, x, y - popupHeight)
            } else {
                popupWindow.animationStyle = bottomPopupAnim

                param.addRule(RelativeLayout.BELOW, searchViewResId)
                emptyTipView.layoutParams = param

                val listViewParam = listView.layoutParams as RelativeLayout.LayoutParams
                //设置ListView在EditText的下面
                listViewParam.addRule(RelativeLayout.BELOW, searchViewResId)
                listView.layoutParams = listViewParam

                popupWindow.height = popupHeight
                popupWindow.showAsDropDown(this)
            }
            emptyTipView.visibility = View.VISIBLE
            listView.visibility = View.GONE
        }
        animateArrow(true)
    }

    private val TAG = "SearchSpinnerMsg"

    private fun removeRule(view: View) {
        val param = view.layoutParams as RelativeLayout.LayoutParams
        // 如果最低版本在16以上,可以直接调用removeRule(rule)
        param.addRule(RelativeLayout.BELOW, 0)
        param.addRule(RelativeLayout.ABOVE, 0)
        param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
        view.layoutParams = param
    }

    //屏幕高度-y坐标-控件高度
    private fun getBottomHeight(): Int = screenHeight - y - height

    //当当前控件所处在屏幕的y坐标减去通知栏的高度大于控件下方的高度的时候
    private fun isTop(): Boolean = y - statusBarHeight > getBottomHeight()

    protected fun calcSize(sizePercent: String?): Float {
        if (sizePercent != null) {
            val regex = "^\\d+%$"
            if (sizePercent.matches(regex.toRegex())) {
                val percent = sizePercent.substring(0, sizePercent.length - 1)
                return percent.toFloat() * context.resources.displayMetrics.widthPixels.toFloat() / 100f
            }
        }
        return -1f
    }

    //计算PopupWindow显示的高度
    private fun calculatePopupWindowHeight(list: ArrayList<T>): Int {
        //所有数据*item的高度+EditText的高度
        val listFullHeight = list.size * adapter.height + searchViewActualHeight + searchViewMargin
        if (isTop()) {
            var popupHeight = Math.min(listFullHeight, y)
            //当y值为PopupWindow的高度的时候
            if (popupHeight == y) {
                //减去通知栏的高度
                popupHeight = popupHeight - statusBarHeight
            }
            return popupHeight
        } else {
            var popupHeight = Math.min(listFullHeight, getBottomHeight())
            if (popupHeight == getBottomHeight()) {
                //留一部分用于显示阴影
                popupHeight = popupHeight - (height * 0.25).toInt()
            }
            return popupHeight
        }
    }

    fun setArrowColor(color: Int) {
        this.arrowDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    protected fun dip2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    interface OnSelectListener<T> {
        fun onSelect(spinner: SearchSpinner<T>, position: Int)
    }
}