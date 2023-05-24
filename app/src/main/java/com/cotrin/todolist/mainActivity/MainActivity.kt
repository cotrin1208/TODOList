package com.cotrin.todolist.mainActivity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import at.grabner.circleprogress.CircleProgressView
import com.cotrin.todolist.R
import com.cotrin.todolist.Task
import com.cotrin.todolist.databinding.ActivityMainBinding
import com.cotrin.todolist.taskDetailActivity.OnCardClickListener
import com.cotrin.todolist.taskDetailActivity.OnItemClickListener
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.putTask
import com.cotrin.todolist.viewModel.MainActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import net.cachapa.expandablelayout.ExpandableLayout
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class MainActivity : AppCompatActivity(), OnDialogResultListener {
    private lateinit var taskListRecycler: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var dateText: TextView
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainActivityViewModel by viewModels()
    companion object {
        var date: LocalDate = LocalDate.now()
        lateinit var sharedPreferences: SharedPreferences
        private val dateRange = -500 .. 500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        sharedPreferences = this@MainActivity.getSharedPreferences(Reference.APP_ID, MODE_PRIVATE)
        Task.loadTasks()

        //翌日繰り越し処理
        Task.carryoverPreviousTasks(date)

        //RecyclerViewの表示
        taskListRecycler = findViewById<RecyclerView?>(R.id.taskListRecyclerView).apply {
            TaskListRecyclerAdapter().apply {
                //RecyclerView内のチェックボックスにリスナー登録、タスク保存
                setOnCheckBoxClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        Task.saveTasks()
                    }
                })
                //RecyclerView内のメニューボタンにリスナー登録
                setOnTaskDetailClickListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        PopupMenu(this@MainActivity, view).apply {
                            inflate(R.menu.popup_menu_task)
                            menu::class.java.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.java).apply {
                                isAccessible = true
                                invoke(menu, true)
                            }
                            setOnMenuItemClickListener { menuItem ->
                                val task = Task.taskList[position]
                                when (menuItem.itemId) {
                                    //編集画面の表示
                                    R.id.menu_edit -> {
                                        mainViewModel.taskData.value = task
                                        showTaskDetailFragment(Reference.EDIT, task, position)
                                        true
                                    }
                                    //タスクを複製する
                                    R.id.menu_copy -> {
                                        onDialogResult(task.copy(uuid = UUID.randomUUID()), position, Reference.ADD)
                                        true
                                    }
                                    //削除ダイアログの表示
                                    R.id.menu_delete -> {
                                        showDeleteAlertDialog {
                                            Task.removeTaskByUUID(task.uuid)
                                            notifyItemRemoved(position)
                                            notifyItemRangeChanged(position, itemCount)
                                            Task.saveTasks()
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            }
                        }.show()
                    }
                })
                //RecyclerViewの行Viewにリスナー登録
                setOnTaskClickListener(object: OnCardClickListener {
                    override fun onItemClick(el: ExpandableLayout, position: Int) {
                        el.toggle()
                    }
                })
                //プログレスバー更新リスナー登録
                setProgressChangeListener(object: OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val task = Task.taskList[position]
                        val isFinish: Boolean = if (view is CircleProgressView) {
                            view.maxValue == view.currentValue
                        } else false
                        Task.taskList[position] = task.copy(isFinished = isFinish)
                    }
                })
                binding.adapter = this
            }
        }
        //タスク追加ボタンのリスナー登録
        mainViewModel.isAddFragmentShown.observe(this) {
            if (!it) return@observe
            showTaskDetailFragment(Reference.ADD)
        }
        //タスク編集時のリスナー登録
        mainViewModel.isEditFragmentShown.observe(this) {
            if (!it) return@observe

        }

        binding.viewModel = mainViewModel
    }

    //削除ダイアログを表示
    private fun showDeleteAlertDialog(onConform: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle("タスクを削除する")
            setMessage("タスクを削除しますか？繰り返し設定がされている場合,今後のタスクもすべて削除されます。")
            setPositiveButton("削除") { _, _ ->
                onConform.invoke()
            }
            setNegativeButton("キャンセル", null)
        }.create().apply {
            setOnShowListener {
                val message = this.findViewById<TextView>(android.R.id.message)
                message?.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
                getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
                getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_light))
            }
        }.show()
    }

    private fun showTaskDetailFragment(mode: String, task: Task? = null, position: Int? = null) {
        TaskDetailFragment().apply {
            val args = Bundle()
            task?.let { args.putTask(Reference.TASK, it) }
                ?: run { args.putTask(Reference.TASK, Task()) }
            position?.let { args.putInt(Reference.POSITION, it) }
            arguments = args
        }.show(supportFragmentManager, mode)
    }

    override fun onDialogResult(task: Task, position: Int, mode: String) {
        val adapter = binding.adapter as TaskListRecyclerAdapter
        if (mode == Reference.ADD) {
            Task.addTask(task)
            if (adapter.itemCount == 0) {
                adapter.notifyDataSetChanged()
            } else {
                adapter.notifyItemInserted(adapter.itemCount - 1)
            }
            Toast.makeText(this@MainActivity, "ADD", Toast.LENGTH_SHORT).show()
        } else if (mode == Reference.EDIT) {
            Task.editTask(position, task)
            adapter.notifyItemChanged(position)
        }
    }
}