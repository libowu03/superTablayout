package com.example.selftab

import android.view.View

interface SuperTabCallback {
    /**
     * tab被选中
     */
    fun onSelected(view:View)

    /**
     * tab被取消选中
     */
    fun onUnSelected(view:View)

    /**
     * 指示器移动
     */
    fun onIndicatorMove(offset:Float)
}