package com.example.selftab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2

/**
 * tab导航栏
 */
class SelfTabView : FrameLayout {
    //tab总个数
    private var mTabCount = 0

    //默认选中的tab索引
    private var mDefaultTabIndex = 0

    //绘制指示器的画板
    private val mIndicatorPaint = Paint()

    //指示器宽度
    private var mIndicatorWidth = 10f

    //指示器高度
    private var mIndicatorHeight = 5f

    //viewpager2的宽度
    private var mViewpagerWidth = 0

    //viewapger滑动距离
    private var mOffset = 0

    //指示器的属性
    private var mLlp: ConstraintLayout.LayoutParams? = null

    //指示器背景
    private var mIndicatorBg: Drawable? = null

    //选中tab字体颜色
    private var mTabSelectedTextColor: Int = Color.BLACK

    //tab字体颜色
    private var mTabTextColor: Int = Color.parseColor("#cccccc")

    //tab字体大小
    private var mTabTextSize: Float = 0f

    //自定义tab布局
    private var mTabCustomLayout: Int = -1

    //选中tab字体大小
    private var mTabSelectedTextSize: Float = 1.2f

    //默认字体粗细样式
    private var mDefaultFontFaceType: Typeface? = null

    //是否允许选中tab文字加粗
    private var mTabEnableSelectedTextBold: Boolean = true

    private var mOldSelectPosition: Int = mDefaultTabIndex

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setWillNotDraw(false)
        initAttr(attrs, defStyleAttr)
        mIndicatorPaint.color = Color.RED
        mIndicatorPaint.isAntiAlias = true
        LayoutInflater.from(context).inflate(R.layout.view_self_tab, this, true)
    }

    /**
     * 获取xml参数
     */
    private fun initAttr(attrs: AttributeSet?, defStyleAttr: Int) {
        val parameters =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SelfTabView, defStyleAttr, 0)
        mIndicatorWidth = parameters.getDimension(R.styleable.SelfTabView_suTabIndicatorWidth, -1f)
        mIndicatorHeight = parameters.getDimension(
            R.styleable.SelfTabView_suTabIndicatorHeight,
            dipTopx(3f).toFloat()
        )
        mIndicatorBg = parameters.getDrawable(R.styleable.SelfTabView_suTabIndicatorBg)
        mTabSelectedTextColor =
            parameters.getColor(R.styleable.SelfTabView_suTabSelectedTextColor, Color.BLACK)
        mTabTextColor =
            parameters.getColor(R.styleable.SelfTabView_suTabTextColor, Color.parseColor("#cccccc"))
        mTabTextSize =
            parameters.getDimension(R.styleable.SelfTabView_suTabTextSize, dipTopx(16f).toFloat())
        mTabSelectedTextSize = parameters.getFloat(
            R.styleable.SelfTabView_suTabSelectedTextSize,
            mTabTextSize + dipTopx(3f)
        )
        mTabCustomLayout = parameters.getResourceId(R.styleable.SelfTabView_suTabSelfLayout, -1)
        mTabEnableSelectedTextBold =
            parameters.getBoolean(R.styleable.SelfTabView_suTabEnableSelectedTextBold, true)
        parameters.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        //单个tab的宽度
        val singleTabWidth = (width / mTabCount)
        if (mIndicatorWidth < 0) {
            mIndicatorWidth = singleTabWidth.toFloat()
        }
        val indicatorStartX =
            ((mDefaultTabIndex) * singleTabWidth + singleTabWidth / 2) - mIndicatorWidth / 2
        //计算viewpager2与tab的比例，用于滑动指示器
        val scale = mViewpagerWidth / singleTabWidth
        //计算指示器需要滑动的距离
        val offset = mOffset / scale
        localIndicatorPosition(indicatorStartX, offset)
    }

    /**
     * 滑动指示器
     * @param indicatorStartX 指示器起始绘制位置
     * @param offset 指示器位移
     */
    private fun localIndicatorPosition(indicatorStartX: Float, offset: Int) {
        if (mLlp == null) {
            mLlp = ConstraintLayout.LayoutParams(mIndicatorWidth.toInt(), mIndicatorHeight.toInt())
            findViewById<ImageView>(R.id.vImgIndicator).layoutParams = mLlp
            if (mIndicatorBg != null) {
                findViewById<ImageView>(R.id.vImgIndicator).setImageDrawable(mIndicatorBg)
            }
        }
        findViewById<ImageView>(R.id.vImgIndicator).translationX = indicatorStartX + offset
        findViewById<ImageView>(R.id.vImgIndicator).translationY = height - mIndicatorHeight
    }

    /**
     * 设置字体缩放
     */
    private fun setTabTextSize(position: Int, selected: TextView, unselected: TextView) {
        if (selected is TextView) {
            selected.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize)
        }
        if (unselected is TextView) {
            unselected.setTextColor(mTabTextColor)
            unselected.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize)
        }
    }

    /**
     * 设置选中tab的字体颜色
     * @param position 选中tab的索引
     */
    private fun setTabTextColor(position: Int) {
        if (position < findViewById<LinearLayout>(R.id.vLlTabBox).childCount) {
            val selected = findViewById<LinearLayout>(R.id.vLlTabBox).getChildAt(position)
            val unselected =
                findViewById<LinearLayout>(R.id.vLlTabBox).getChildAt(mOldSelectPosition)
            if (position != mOldSelectPosition) {
                if (selected is TextView && unselected is TextView) {
                    selected.setTextColor(mTabSelectedTextColor)
                    unselected.setTextColor(mTabTextColor)
                    setTabTextSize(position, selected, unselected)
                    setTabTypeFace(selected,unselected)
                }
            }
        }
    }

    /**
     * 设置默认字体粗细样式
     */
    private fun setTabTypeFace(selected:TextView,unselected: TextView){
        if (mTabEnableSelectedTextBold) {
            selected.typeface = Typeface.DEFAULT_BOLD
            mDefaultFontFaceType?.let {
                unselected.typeface = it
            }
        }
    }


    /**
     * 绑定viewpager2
     */
    fun attachViewPager2(viewPager2: ViewPager2) {
        findViewById<LinearLayout>(R.id.vLlTabBox).removeAllViews()
        mTabCount = viewPager2.adapter?.itemCount ?: 0
        mDefaultTabIndex = viewPager2.currentItem
        for (item in 0 until mTabCount) {
            if (mTabCustomLayout != -1) {
                val view = LayoutInflater.from(context)
                    .inflate(mTabCustomLayout, findViewById(R.id.vLlTabBox), false)
                if (view != null) {
                    addCustomView(item, view, viewPager2)
                } else {
                    addDefaultTextView(item, viewPager2)
                }
            } else {
                addDefaultTextView(item, viewPager2)
            }
        }
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                mDefaultTabIndex = position
                mViewpagerWidth = viewPager2.width
                mOffset = positionOffsetPixels
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mDefaultTabIndex = position
                setTabTextColor(position)
                mOldSelectPosition = position
            }
        })
    }

    /**
     * 添加默认tab布局
     */
    private fun addDefaultTextView(item: Int, viewPager2: ViewPager2) {
        val view = TextView(context)
        //设置默认选中项
        if (item == mDefaultTabIndex) {
            view.setTextColor(mTabSelectedTextColor)
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize)
            if (mTabEnableSelectedTextBold) {
                view.typeface = Typeface.DEFAULT_BOLD
            }
            mOldSelectPosition = item
        } else {
            view.setTextColor(mTabTextColor)
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize)
            mDefaultFontFaceType = view.typeface
        }
        view.gravity = Gravity.CENTER
        view.text = "你好呀"
        val viewLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        viewLp.weight = 1f
        view.layoutParams = viewLp
        view.setOnClickListener {
            val index = findViewById<LinearLayout>(R.id.vLlTabBox).indexOfChild(it)
            setTabTextColor(index)
            viewPager2.setCurrentItem(index, true)
        }
        findViewById<LinearLayout>(R.id.vLlTabBox).addView(view)
    }

    /**
     * 添加自定义布局
     */
    private fun addCustomView(item: Int, view: View, viewPager2: ViewPager2) {
        val viewLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        //设置默认选中项
        if (item == mDefaultTabIndex) {
            if (view is TextView) {
                view.setTextColor(mTabSelectedTextColor)
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize)
                if (mTabEnableSelectedTextBold) {
                    view.typeface = Typeface.DEFAULT_BOLD
                }
            }
            mOldSelectPosition = item
        } else {
            if (view is TextView) {
                view.setTextColor(mTabTextColor)
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize)
                mDefaultFontFaceType = view.typeface
            }
        }
        viewLp.weight = 1f
        view.layoutParams = viewLp
        view.setOnClickListener {
            val index = findViewById<LinearLayout>(R.id.vLlTabBox).indexOfChild(it)
            setTabTextColor(index)
            viewPager2.setCurrentItem(index, true)
        }
        //添加自定义布局
        findViewById<LinearLayout>(R.id.vLlTabBox).addView(view)
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context
     * @param dpValue dp值
     * @return px值
     */
    fun dipTopx(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}