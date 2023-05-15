package com.cotrin.todolist.taskDetailActivity

import net.cachapa.expandablelayout.ExpandableLayout

interface OnCardClickListener {
    fun onItemClick(el: ExpandableLayout, position: Int)
}