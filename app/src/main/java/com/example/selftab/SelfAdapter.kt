package com.example.selftab

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelfAdapter : RecyclerView.Adapter<SelfAdapter.Sa>() {
    class Sa(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Sa {
        val test = TextView(parent.context)
        val testLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
        test.layoutParams = testLp
        test.text = "你好呀"
        return Sa(test)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: Sa, position: Int) {

    }
}