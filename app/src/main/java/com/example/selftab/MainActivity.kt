package com.example.selftab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<ViewPager2>(R.id.vVp2).adapter = SelfAdapter()
        findViewById<SelfTabView>(R.id.vTab).setCustomTabContent { customView, title, position ->

        }
        findViewById<SelfTabView>(R.id.vTab).attachViewPager2(findViewById<ViewPager2>(R.id.vVp2))
        findViewById<SelfTabView>(R.id.vTab).addTab(arrayListOf("你","你好","发动机","发动机","发动机"))
        findViewById<ViewPager2>(R.id.vVp2).currentItem = 3
    }
}