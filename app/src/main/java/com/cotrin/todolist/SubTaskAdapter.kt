package com.cotrin.todolist

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cotrin.todolist.databinding.LayoutSubTaskBinding
import com.cotrin.todolist.listener.OnItemClickListener
import com.cotrin.todolist.listener.OnTextChangeListener
import com.cotrin.todolist.model.SubTask

class SubTaskAdapter(private val subTasks: List<SubTask>): RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder>() {
    private lateinit var checkBoxClickListener: OnItemClickListener
    private lateinit var textChangeListener: OnTextChangeListener
    private lateinit var deleteButtonClickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        val subTaskXml = LayoutSubTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubTaskViewHolder(subTaskXml)
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        val subTask = subTasks[position]
        //チェックボックスの設定
        holder.binding.subTaskFinishedCheckBox.apply {
            isChecked = subTask.isFinished
            setOnClickListener {
                checkBoxClickListener.onItemClick(it, position)
            }
        }
        //サブタスク名の設定
        holder.binding.subTaskTitleText.apply {
            setText(subTask.name)
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    textChangeListener.onTextChanged(s, holder.adapterPosition)
                }
            })
        }
        //削除ボタンの設定
        holder.binding.subTaskDeleteButton.setOnClickListener {
            deleteButtonClickListener.onItemClick(it, position)
        }
    }

    override fun getItemCount(): Int = subTasks.size

    fun setOnCheckBoxClickListener(listener: OnItemClickListener) {
        checkBoxClickListener = listener
    }

    fun setOnTextChangeListener(listener: OnTextChangeListener) {
        textChangeListener = listener
    }

    fun setOnDeleteButtonClickListener(listener: OnItemClickListener) {
        deleteButtonClickListener = listener
    }

    class SubTaskViewHolder(val binding: LayoutSubTaskBinding): RecyclerView.ViewHolder(binding.root)
}