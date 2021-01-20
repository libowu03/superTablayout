package com.example.selftab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    //tab的显示模式，包括滚动和平均分配两种
    private var mTabShowModel:Int = 0

    //tab滚动模式下每个tab的最大宽度
    private var mTabScrollMaxWidth:Float = 0f

    //tab的滚动模式下的tab宽度
    private var mTabScrollWidth:Float = 0f

    //存放tab父盒子的宽度
    private var mTabParentBoxWidth = 0f

    private var mViewpager2:ViewPager2?=null

    private var mCustomTabConfigCallback:(customView:View,title:String,position:Int)->Unit = {customView, title, position ->  }

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
        if (mTabShowModel == 0){
            LayoutInflater.from(context).inflate(R.layout.view_self_tab, this, true)
        }else{
            LayoutInflater.from(context).inflate(R.layout.view_self_scroll_tab, this, true)
        }
    }

    /**
     * 获取xml参数
     */
    private fun initAttr(attrs: AttributeSet?, defStyleAttr: Int) {
        //滚动模式下每个tab的最大宽度限制为200dp
        mTabScrollMaxWidth = dipTopx(200f).toFloat()

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
        mTabShowModel = parameters.getInt(R.styleable.SelfTabView_suTabShowModel,0)
        parameters.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        //单个tab的宽度
        var singleTabWidth = (width / mTabCount)
        if (mTabShowModel == 1 && mTabScrollWidth != 0f){
            singleTabWidth = mTabScrollWidth.toInt()
        }
        if (mIndicatorWidth < 0) {
            mIndicatorWidth = singleTabWidth.toFloat()
        }
        val indicatorStartX = ((mDefaultTabIndex) * singleTabWidth + singleTabWidth / 2) - mIndicatorWidth / 2
        val indicatorNextEndX = ((mDefaultTabIndex+1) * singleTabWidth + singleTabWidth / 2) + mIndicatorWidth / 2
        val indicatorNextStartX = ((mDefaultTabIndex+1) * singleTabWidth + singleTabWidth / 2) - mIndicatorWidth / 2
        //计算viewpager2与tab的比例，用于滑动指示器
        val scale = mViewpagerWidth / singleTabWidth
        //计算指示器需要滑动的距离
        val offset = mOffset / scale
        localIndicatorPosition(indicatorStartX,indicatorNextStartX,indicatorNextEndX, offset)
    }

    /**
     * 滑动指示器
     * @param indicatorStartX 指示器起始绘制位置
     * @param offset 指示器位移
     */
    private fun localIndicatorPosition(indicatorStartX: Float,indicatorNextStartX:Float,indicatorNextEndX: Float, offset: Int) {
        if (mLlp == null) {
            mLlp = ConstraintLayout.LayoutParams(mIndicatorWidth.toInt(), mIndicatorHeight.toInt())
            findViewById<ImageView>(R.id.vImgIndicator).layoutParams = mLlp
            if (mIndicatorBg != null) {
                findViewById<ImageView>(R.id.vImgIndicator).setImageDrawable(mIndicatorBg)
            }
        }
        if (indicatorStartX + offset > (indicatorNextEndX-mIndicatorWidth)){
            setTabTextColor(mDefaultTabIndex+1)
        }
        //Log.e("日志","offset为：${offset},${mOffset}")
        findViewById<ImageView>(R.id.vImgIndicator).translationX = if (indicatorStartX + offset > (indicatorNextEndX-mIndicatorWidth)) indicatorNextStartX else indicatorStartX + offset
        findViewById<ImageView>(R.id.vImgIndicator).translationY = height - mIndicatorHeight
    }

    /**
     * 设置字体缩放
     */
    private fun setTabTextSize(selected: TextView, unselected: TextView) {
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
                    setTabTextSize(selected, unselected)
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
        mViewpager2 = viewPager2
        findViewById<LinearLayout>(R.id.vLlTabBox).removeAllViews()
        mTabCount = viewPager2.adapter?.itemCount ?: 0
        mDefaultTabIndex = viewPager2.currentItem
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
                if (mTabShowModel == 1){
                    val scrollBox = findViewById<HorizontalScrollView>(R.id.vHScrollTabBox)
                    scrollBox.scrollTo((position*mTabScrollWidth).toInt(),0)
                    //设置tab父盒子宽度
                    mTabParentBoxWidth = findViewById<LinearLayout>(R.id.vLlTabBox).width.toFloat()
                }
            }
        })
    }

    /**
     * 添加tab
     */
    fun addTab(tabText:MutableList<String>){
        for (item in 0 until mTabCount) {
            if (mTabCustomLayout != -1) {
                val view = LayoutInflater.from(context)
                    .inflate(mTabCustomLayout, findViewById(R.id.vLlTabBox), false)
                if (view != null) {
                    if (item > tabText.size-1){
                        addCustomView("",item, view)
                    }else{
                        addCustomView(tabText[item],item, view)
                    }
                } else {
                    if (item > tabText.size-1){
                        addDefaultTextView("",item)
                    }else{
                        addDefaultTextView(tabText[item],item)
                    }
                }
            } else {
                if (item > tabText.size-1){
                    addDefaultTextView("",item)
                }else{
                    addDefaultTextView(tabText[item],item)
                }
            }
        }
    }

    /**
     * 添加默认tab布局
     */
    private fun addDefaultTextView(title:String,item: Int) {
        val view = TextView(context)
        //设置默认选中项
        if (item == mDefaultTabIndex) {
            view.setTextColor(mTabSelectedTextColor)
            view.text = title
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize)
            if (mTabEnableSelectedTextBold) {
                view.typeface = Typeface.DEFAULT_BOLD
            }
            mOldSelectPosition = item
        } else {
            view.setTextColor(mTabTextColor)
            view.text = title
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize)
            mDefaultFontFaceType = view.typeface
        }
        view.maxLines = 1
        view.gravity = Gravity.CENTER
        view.ellipsize = TextUtils.TruncateAt.END
        val viewLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        viewLp.weight = 1f
        view.layoutParams = viewLp
        view.setOnClickListener {
            val index = findViewById<LinearLayout>(R.id.vLlTabBox).indexOfChild(it)
            setTabTextColor(index)
            mViewpager2?.setCurrentItem(index, true)
        }
        findViewById<LinearLayout>(R.id.vLlTabBox).addView(view)
    }

    /**
     * 添加自定义布局
     */
    private fun addCustomView(title:String,item: Int, view: View) {
        //设置默认选中项
        if (item == mDefaultTabIndex) {
            if (view is TextView) {
                view.setTextColor(mTabSelectedTextColor)
                view.text = title
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize)
                if (mTabEnableSelectedTextBold) {
                    view.typeface = Typeface.DEFAULT_BOLD
                }
            }
            mOldSelectPosition = item
        } else {
            if (view is TextView) {
                view.setTextColor(mTabTextColor)
                view.text = title
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize)
                mDefaultFontFaceType = view.typeface
            }
        }
        //通过该方法设置自定义数据
        mCustomTabConfigCallback.invoke(view,title,item)

        if (mTabShowModel == 0){
            val viewLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            viewLp.weight = 1f
            view.layoutParams = viewLp
        }else{
            if (view.layoutParams.width > 0){
                mTabScrollWidth = view.layoutParams.width.toFloat()
            }
            //Log.e("日志","宽度：${view.layoutParams.width}")
        }

        view.setOnClickListener {
            val index = findViewById<LinearLayout>(R.id.vLlTabBox).indexOfChild(it)
            setTabTextColor(index)
            mViewpager2?.setCurrentItem(index, true)
        }
        //添加自定义布局
        findViewById<LinearLayout>(R.id.vLlTabBox).addView(view)
    }

    /**
     * 假如设置了自定义布局，请通过该方法给自定义布局中设置标题等信息
     */
    fun setCustomTabContent(customTabConfigCallback:(customView:View,title:String,position:Int)->Unit){
        this.mCustomTabConfigCallback = customTabConfigCallback
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context
     * @param dpValue dp值
     * @return px值
     */
    private fun dipTopx(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}