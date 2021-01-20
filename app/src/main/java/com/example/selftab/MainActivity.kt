package com.example.selftab

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<ViewPager2>(R.id.vVp2).adapter = SelfAdapter()
        findViewById<SelfTabView>(R.id.vTab).attachViewPager2(findViewById<ViewPager2>(R.id.vVp2))
    }
}