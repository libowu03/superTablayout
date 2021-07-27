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
import android.view.View.MeasureSpec.AT_MOST
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

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

    //指示器宽度(-1表示自动)
    var mIndicatorWidth = 10f

    //默认选中tab指示器的长度，自动宽度模式下有用，其他模式可以无视此值
    var mIndicatorDefaultIndexWidth = 0f

    //指示器高度
    var mIndicatorHeight = 5f

    //viewpager2的宽度
    private var mViewpagerWidth = 0

    //viewapger滑动距离
    private var mOffset = 0

    //指示器的属性
    private var mLlp: ConstraintLayout.LayoutParams? = null

    //指示器背景
    var mIndicatorBg: Drawable? = null

    //选中tab字体颜色
    var mTabSelectedTextColor: Int = Color.BLACK

    //tab字体颜色
    var mTabTextColor: Int = Color.parseColor("#cccccc")

    //tab字体大小
    var mTabTextSize: Float = 0f

    //自定义tab布局
    var mTabCustomLayout: Int = -1

    //选中tab字体大小
    var mTabSelectedTextSize: Float = 1.2f

    //默认字体粗细样式
    private var mDefaultFontFaceType: Typeface? = null

    //是否允许选中tab文字加粗
    var mTabEnableSelectedTextBold: Boolean = true

    //tab的显示模式，包括滚动和平均分配两种
    private var mTabShowModel: Int = 0

    private var mIsAutoWidthIndicator:Boolean = false

    //tab滚动模式下每个tab的最大宽度
    private var mTabScrollMaxWidth: Float = 0f

    //tab的滚动模式下的tab宽度
    private var mTabScrollWidth: Float = 0f

    //存放tab父盒子的宽度
    private var mTabParentBoxWidth = 0f

    //滚动模式下，如果用户设置的宽度为match或wrap，则会将tab宽度设置为tab标题最大宽度+该padding
    var mTabDefaultScrollPadding: Float = 0f

    //指示器距离底部的距离
    var mTabIndicatorMarginBottom: Float = 0f

    //用户设置的viewpager2
    private var mViewpager2: ViewPager2? = null

    //用户设置的viewpager
    private var mViewpager: ViewPager? = null

    //用户设置的viewpager2监听器
    private var mViewpager2Callback: ViewPager2.OnPageChangeCallback? = null

    //用户设置的viewpager监听器
    private var mViewpagerCallback: ViewPager.OnPageChangeListener? = null

    //tab监听器
    private var mSuperTabCallback: SuperTabCallback? = null

    //选中tab的背景
    private var mSuperTabSelectedBg:Drawable?=null

    //未选中tab的背景
    private var mSuperTabUnselectedBg:Drawable?=null

    private var mDefaultTabHeight:Int = 0

    //tab的标题
    private var mTabTitleArray:MutableList<String> = mutableListOf()

    //tab指示器长度自动情况下使用到的指示器长度
    private var mTabIndicatorWidthList:MutableList<Int> = mutableListOf()

    //调用者自行设置的tab宽度，优先读取该值
    private var mTabWidth = -1f

    var mTabEnablePageSmoothScroll:Boolean = true

    //view的宽度
    private var mViewWidth = 0


    private var mCustomTabConfigCallback: (customView: View, title: String, position: Int) -> Unit =
        { customView, title, position -> }

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
        if (mTabShowModel == 0) {
            LayoutInflater.from(context).inflate(R.layout.view_self_tab, this, true)
        } else {
            LayoutInflater.from(context).inflate(R.layout.view_self_scroll_tab, this, true)
        }

        if (mIndicatorWidth == 0f){
            findViewById<ImageView>(R.id.vImgIndicator).visibility = View.GONE
        }
    }


    /**
     * 获取xml参数
     */
    private fun initAttr(attrs: AttributeSet?, defStyleAttr: Int) {
        //滚动模式下每个tab的最大宽度限制为200dp
        mTabScrollMaxWidth = dipTopx(200f).toFloat()
        mTabDefaultScrollPadding = dipTopx(20f).toFloat()

        val parameters =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SelfTabView, defStyleAttr, 0)
        mIndicatorWidth = parameters.getLayoutDimension(R.styleable.SelfTabView_suTabIndicatorWidth, -1).toFloat()
        if (mIndicatorWidth == -1f){
            mIsAutoWidthIndicator = true
        }
        //Log.e("日志","前面宽度为：${mIndicatorWidth}")
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
            parameters.getDimension(R.styleable.SelfTabView_suTabTextSize, dipTopx(13f).toFloat())
        mTabWidth = parameters.getDimension(R.styleable.SelfTabView_subTabWidth,-1f)
        mTabSelectedTextSize = parameters.getDimension(
            R.styleable.SelfTabView_suTabSelectedTextSize,
            mTabTextSize + dipTopx(3f)
        )
        mTabCustomLayout = parameters.getResourceId(R.styleable.SelfTabView_suTabSelfLayout, -1)
        mTabEnableSelectedTextBold =
            parameters.getBoolean(R.styleable.SelfTabView_suTabEnableSelectedTextBold, true)
        mTabShowModel = parameters.getInt(R.styleable.SelfTabView_suTabShowModel, 0)
        mTabIndicatorMarginBottom =
            parameters.getDimension(R.styleable.SelfTabView_suTabIndicatorMarginBottom, 0f)
        mTabEnablePageSmoothScroll = parameters.getBoolean(R.styleable.SelfTabView_suTabEnablePageSmoothScroll,true)
        mSuperTabUnselectedBg = parameters.getDrawable(R.styleable.SelfTabView_suTabUnselectedBg)
        mSuperTabSelectedBg = parameters.getDrawable(R.styleable.SelfTabView_suTabSelectedBg)
        if (mTabShowModel != 0 && mTabWidth != -1f && mIndicatorWidth > mTabWidth){
            mIndicatorWidth = mTabWidth
        }
        parameters.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        if (mTabCount == 0){
            return
        }
        if (mIndicatorWidth == -1f && mIndicatorDefaultIndexWidth != 0f){
            mIndicatorWidth = mIndicatorDefaultIndexWidth
        }
        //Log.e("日志","宽度为：${mIndicatorWidth}")

        mDefaultTabHeight = height
        //单个tab的宽度
        var singleTabWidth = (width / mTabCount)
        if (mTabShowModel == 1 && mTabScrollWidth != 0f) {
            singleTabWidth = mTabScrollWidth.toInt()
        }
        //如果指示器长度比tab长度还长，则指示器长度直接设置为tab长度
        if (singleTabWidth < mIndicatorWidth) {
            mIndicatorWidth = singleTabWidth.toFloat()
        }

        if (mIndicatorWidth < 0) {
            mIndicatorWidth = singleTabWidth.toFloat()
        }
        var indicatorStartX = ((mDefaultTabIndex) * singleTabWidth + singleTabWidth / 2) - mIndicatorWidth / 2
        //下一个tab指示器结束位置
        var indicatorNextEndX = ((mDefaultTabIndex + 1) * singleTabWidth + singleTabWidth / 2) + mIndicatorWidth / 2
        //下一个tab指示器开始位置
        var indicatorNextStartX = ((mDefaultTabIndex + 1) * singleTabWidth + singleTabWidth / 2) - mIndicatorWidth / 2
        //指示器自动长度模式下需要做适当调整
        if (mIsAutoWidthIndicator){
            indicatorStartX = ((mDefaultTabIndex) * singleTabWidth + singleTabWidth / 2f) - mTabIndicatorWidthList[mDefaultTabIndex] / 2f
            indicatorNextEndX = ((mDefaultTabIndex + 1) * singleTabWidth + singleTabWidth / 2f) + mTabIndicatorWidthList.getObj(mDefaultTabIndex+1) / 2f
            indicatorNextStartX = ((mDefaultTabIndex + 1) * singleTabWidth + singleTabWidth / 2f) - mTabIndicatorWidthList.getObj(mDefaultTabIndex+1) / 2f
        }
        //计算viewpager2与tab的比例，用于滑动指示器
        val scale = mViewpagerWidth / singleTabWidth
        //计算指示器需要滑动的距离
        val offset = mOffset / scale
        //如果指示器宽度为0，没必要执行移动指示器的步骤
        if (mIndicatorWidth != 0f){
            localIndicatorPosition(indicatorStartX, indicatorNextStartX, indicatorNextEndX, offset,singleTabWidth.toFloat())
        }
    }

    var tmp = 1f
    /**
     * 滑动指示器
     * @param indicatorStartX 指示器起始绘制位置
     * @param offset 指示器位移
     */
    private fun localIndicatorPosition(
        indicatorStartX: Float,
        indicatorNextStartX: Float,
        indicatorNextEndX: Float,
        offset: Int,
        singleTabWidth:Float
    ) {
        if (mLlp == null) {
            mLlp = ConstraintLayout.LayoutParams(mIndicatorWidth.toInt(), mIndicatorHeight.toInt())
            findViewById<ImageView>(R.id.vImgIndicator).layoutParams = mLlp
            if (mIndicatorBg != null) {
                findViewById<ImageView>(R.id.vImgIndicator).setImageDrawable(mIndicatorBg)
            }
        }
        if (mIsAutoWidthIndicator){
            var pro = ((mTabIndicatorWidthList.getObj(mDefaultTabIndex+1)-mTabIndicatorWidthList[mDefaultTabIndex].toFloat())*(offset/singleTabWidth))
            //Log.e("日志","pro为:${offset/singleTabWidth}")
            var widthTmp = mTabIndicatorWidthList[mDefaultTabIndex]+pro.toInt()
            mLlp = ConstraintLayout.LayoutParams(widthTmp, mIndicatorHeight.toInt())
            findViewById<ImageView>(R.id.vImgIndicator).layoutParams = mLlp
        }
  /*      if (indicatorStartX + offset > (indicatorNextEndX - mTabIndicatorWidthList[mDefaultTabIndex])) {
            setTabTextColor(mDefaultTabIndex + 1)
        }*/
        if (mTabIndicatorWidthList.size-1 >= mDefaultTabIndex){
            findViewById<ImageView>(R.id.vImgIndicator).translationX =
                if (indicatorStartX + offset >= (indicatorNextEndX - mTabIndicatorWidthList[mDefaultTabIndex])-dipTopx(10f)) indicatorNextStartX else indicatorStartX + offset
            findViewById<ImageView>(R.id.vImgIndicator).translationY =
                height - mIndicatorHeight - mTabIndicatorMarginBottom
            mSuperTabCallback?.onIndicatorMove(offset.toFloat())
        }
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
            val unselected = findViewById<LinearLayout>(R.id.vLlTabBox).getChildAt(mOldSelectPosition)
            mSuperTabCallback?.onSelected(selected)
            mSuperTabCallback?.onUnSelected(unselected)
            if (position != mOldSelectPosition) {
                if (selected is TextView && unselected is TextView) {
                    selected.setTextColor(mTabSelectedTextColor)
                    unselected.setTextColor(mTabTextColor)
                    setTabTextSize(selected, unselected)
                    setTabTypeFace(selected, unselected)
                }

                //设置选中及未选中背景
                if (mSuperTabSelectedBg != null){
                    selected.background = mSuperTabSelectedBg
                }
                if (mSuperTabUnselectedBg != null){
                    unselected.background = mSuperTabUnselectedBg
                }
            }
        }
    }

    /**
     * 设置默认字体粗细样式
     */
    private fun setTabTypeFace(selected: TextView, unselected: TextView) {
        if (mTabEnableSelectedTextBold) {
            selected.typeface = Typeface.DEFAULT_BOLD
            mDefaultFontFaceType?.let {
                unselected.typeface = it
            }
        }
    }

    /**
     * 绑定viewpager
     */
    fun attachViewpager(viewpager: ViewPager) {
        findViewById<LinearLayout>(R.id.vLlTabBox).removeAllViews()
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                mViewpagerCallback?.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                mViewpagerCallback?.onPageScrolled(position, positionOffset, positionOffsetPixels)
                mDefaultTabIndex = position
                mViewpagerWidth = viewpager.width
                mOffset = positionOffsetPixels
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                mViewpagerCallback?.onPageSelected(position)
                mDefaultTabIndex = position
                setTabTextColor(position)
                mOldSelectPosition = position
                if (mTabShowModel == 1) {
                    val scrollBox = findViewById<HorizontalScrollView>(R.id.vHScrollTabBox)
                    scrollBox.scrollTo((position * mTabScrollWidth).toInt(), 0)
                    //设置tab父盒子宽度
                    mTabParentBoxWidth = findViewById<LinearLayout>(R.id.vLlTabBox).width.toFloat()
                }
            }

        })
    }

    /**
     * 绑定viewpager2
     * 这里需要注意一下，如果使用该tab，就不可以自己再注册一个viewpager2的监听器了，如果再注册一次，该tab中的功能将会受到影响
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
                mViewpager2Callback?.onPageScrolled(position, positionOffset, positionOffsetPixels)
                mDefaultTabIndex = position
                mViewpagerWidth = viewPager2.width
                mOffset = positionOffsetPixels
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mViewpager2Callback?.onPageSelected(position)
                mDefaultTabIndex = position
                setTabTextColor(position)
                mOldSelectPosition = position
                if (mTabShowModel == 1) {
                    val scrollBox = findViewById<HorizontalScrollView>(R.id.vHScrollTabBox)
                    scrollBox.scrollTo((position * mTabScrollWidth).toInt(), 0)
                    //设置tab父盒子宽度
                    mTabParentBoxWidth = findViewById<LinearLayout>(R.id.vLlTabBox).width.toFloat()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                mViewpager2Callback?.onPageScrollStateChanged(state)
            }
        })
    }

    /**
     * 设置用户需要的viewpager2监听器
     * 如果使用了改控件，直接在控件以外再次注册监听器，该控件功能将会受到影响
     */
    fun registerViewpager2Callback(callback: ViewPager2.OnPageChangeCallback) {
        this.mViewpager2Callback = callback
    }

    /**
     * 设置用户需要的viewpager监听器
     * 如果使用了改控件，直接在控件以外再次注册监听器，该控件功能将会受到影响
     */
    fun registerViewpagerCallabck(callback: ViewPager.OnPageChangeListener) {
        this.mViewpagerCallback = callback
    }

    /**
     * 添加tab
     */
    fun addTab(tabText: MutableList<String>) {
        mTabIndicatorWidthList.clear()
        if (mTabTitleArray.isEmpty()){
            mTabTitleArray.addAll(tabText)
        }
        //获取最长的文本长度，用于设置滚动模式下的tab长度
        var maxLength = 0

        for (item in 0 until mTabCount) {
            if (item <= mTabTitleArray.size - 1) {
                if (mTabTitleArray[item].length > maxLength) {
                    maxLength = mTabTitleArray[item].length
                }
                if (mIsAutoWidthIndicator){
                    //获取每个tab文案的长度，用于在指示器长度自动模式下的宽度
                    if ((mTabTitleArray[item].length*mTabTextSize) > mTabScrollMaxWidth) {
                        mTabIndicatorWidthList.add( mTabScrollMaxWidth.toInt() )
                        if(item == mDefaultTabIndex){
                            mIndicatorDefaultIndexWidth = mTabScrollMaxWidth
                            //Log.e("日志","最大值")
                        }
                    }else{
                        mTabIndicatorWidthList.add( (mTabTitleArray[item].length * mTabTextSize).toInt() )
                        if(item == mDefaultTabIndex){
                            mIndicatorDefaultIndexWidth = (mTabTitleArray[item].length * mTabTextSize)
                            //Log.e("日志","执行其他")
                        }
                    }
                }else{
                    mTabIndicatorWidthList.add( mIndicatorWidth.toInt() )
                }
            }
        }
        //设置滚动模式下的tab长度，这里需要添加一个默认的长度给textView，否则直接用字符最大长度会非常拥挤
        if(mTabShowModel == 1){
            mTabScrollWidth = maxLength * mTabTextSize + mTabDefaultScrollPadding
            if (mTabWidth != -1f){
                mTabScrollWidth = mTabWidth
            }
            mTabIndicatorWidthList.clear()
        }else if (mTabShowModel == 2){
            //自定模式下长度设置
            if (mViewWidth != 0){
                if (maxLength * mTabTextSize + mTabDefaultScrollPadding > mViewWidth){
                    mTabScrollWidth = maxLength * mTabTextSize + mTabDefaultScrollPadding
                }else{
                    mTabScrollWidth = (mViewWidth/tabText.size).toFloat()
                }
                if (mTabWidth != -1f){
                    mTabScrollWidth = mTabWidth
                }
                mTabIndicatorWidthList.clear()
            }
        }


        for (item in 0 until mTabCount) {
            if (mTabCustomLayout != -1) {
                val view = LayoutInflater.from(context)
                    .inflate(mTabCustomLayout, findViewById(R.id.vLlTabBox), false)
                if (view != null) {
                    if (item > mTabTitleArray.size - 1) {
                        addCustomView("", item, view)
                    } else {
                        addCustomView(mTabTitleArray[item], item, view)
                    }
                } else {
                    if (item > mTabTitleArray.size - 1) {
                        addDefaultTextView("", item)
                    } else {
                        addDefaultTextView(mTabTitleArray[item], item)
                    }
                }
            } else {
                if (item > mTabTitleArray.size - 1) {
                    addDefaultTextView("", item)
                } else {
                    addDefaultTextView(mTabTitleArray[item], item)
                }
            }
        }
    }

    /**
     * 添加默认tab布局
     */
    private fun addDefaultTextView(title: String, item: Int) {
        if (mDefaultTabHeight == 0){
            return
        }
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
            if (mSuperTabSelectedBg != null){
                view.background = mSuperTabSelectedBg
            }
        } else {
            view.background = mSuperTabUnselectedBg
            view.setTextColor(mTabTextColor)
            view.text = title
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize)
            mDefaultFontFaceType = view.typeface
        }
        view.maxLines = 1
        view.gravity = Gravity.CENTER
        view.ellipsize = TextUtils.TruncateAt.END
        val viewLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        if (mTabScrollWidth != 0f && mTabShowModel == 1) {
            if (mTabScrollWidth > mTabScrollMaxWidth) {
                mTabScrollWidth = mTabScrollMaxWidth
            }
            viewLp.width = mTabScrollWidth.toInt()
            mTabIndicatorWidthList.add(mTabScrollWidth.toInt())
        } else if (mTabShowModel == 2){
            viewLp.width = mTabScrollWidth.toInt()
            mTabIndicatorWidthList.add(mTabScrollWidth.toInt())
        } else {
            viewLp.weight = 1f
        }
        //Log.e("日志","高度为:${mDefaultTabHeight}")
        viewLp.height = mDefaultTabHeight
        //Log.e("日志","高度：${mDefaultTabHeight}")
        view.layoutParams = viewLp
        view.setOnClickListener {
            val index = findViewById<LinearLayout>(R.id.vLlTabBox).indexOfChild(it)
            setTabTextColor(index)
            mViewpager2?.setCurrentItem(index, true)
            mViewpager?.setCurrentItem(index,true)
        }
        findViewById<LinearLayout>(R.id.vLlTabBox).addView(view)
    }

    /**
     * 添加自定义布局
     */
    private fun addCustomView(title: String, item: Int, view: View) {
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
        mCustomTabConfigCallback.invoke(view, title, item)

        if (mTabShowModel == 0) {
            val viewLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            viewLp.weight = 1f
            view.layoutParams = viewLp
        } else {
            //如果自定义布局设置了宽度，且宽度不是自适应或铺满，则用用户的宽度，否则使用字符最大宽度
            if (view.layoutParams.width > 0) {
                //这里限制一下tab的最大宽度，大于最大宽度直接取最大宽度
                if (view.layoutParams.width.toFloat() > mTabScrollMaxWidth) {
                    val llp = view.layoutParams
                    llp.width = mTabScrollMaxWidth.toInt()
                    view.layoutParams = llp
                } else {
                    mTabScrollWidth = view.layoutParams.width.toFloat()
                }
            } else {
                val llp = view.layoutParams
                llp.width = mTabScrollWidth.toInt()
                mTabIndicatorWidthList.add(mTabScrollWidth.toInt())
                view.layoutParams = llp
            }
            //Log.e("日志","宽度：${view.layoutParams.width}")
        }

        view.setOnClickListener {
            val index = findViewById<LinearLayout>(R.id.vLlTabBox).indexOfChild(it)
            setTabTextColor(index)
            mViewpager2?.setCurrentItem(index, true)
            mViewpager?.setCurrentItem(index,true)
        }
        //添加自定义布局
        findViewById<LinearLayout>(R.id.vLlTabBox).addView(view)
    }

    /**
     * 假如设置了自定义布局，请通过该方法给自定义布局中设置标题等信息
     */
    fun setCustomTabContent(customTabConfigCallback: (customView: View, title: String, position: Int) -> Unit) {
        this.mCustomTabConfigCallback = customTabConfigCallback
    }

    /**
     * 设置tab的监听器
     */
    fun setTabCallback(callback: SuperTabCallback) {
        this.mSuperTabCallback = callback
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

    /**
     * 选中某个tab
     */
    fun selectTab(position:Int){
        mViewpager?.setCurrentItem(position,mTabEnablePageSmoothScroll)
        mViewpager2?.setCurrentItem(position,mTabEnablePageSmoothScroll)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (mTabTitleArray.isNotEmpty()){
            if (MeasureSpec.getSize(heightMeasureSpec) > 0){
                if (MeasureSpec.getMode(heightMeasureSpec) == AT_MOST){
                    mDefaultTabHeight = dipTopx(35f)
                }else{
                    mDefaultTabHeight = MeasureSpec.getSize(heightMeasureSpec)
                }
                //Log.e("日志","高度为：${mDefaultTabHeight},${MeasureSpec.getSize(heightMeasureSpec)}")
            }
            if (findViewById<LinearLayout>(R.id.vLlTabBox).childCount == 0){
                addTab(mTabTitleArray)
            }
        }
    }

}