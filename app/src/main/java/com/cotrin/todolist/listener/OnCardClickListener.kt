package com.cotrin.todolist.listener

import net.cachapa.expandablelayout.ExpandableLayout

interface OnCardClickListener {
    fun onItemClick(el: ExpandableLayout, position: Int)
}