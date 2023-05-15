package com.cotrin.todolist.mainActivity

import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.R
import com.cotrin.todolist.databinding.LayoutTaskCardBinding
import net.cachapa.expandablelayout.ExpandableLayout

class TaskListViewHolder(val binding: LayoutTaskCardBinding): RecyclerView.ViewHolder(binding.root) {
    fun startFadeInAnimation(position: Int) {
        val anim = AlphaAnimation(0f, 1f)
        anim.startOffset = position * 100L
        anim.duration = 500L
        itemView.startAnimation(anim)
    }
}