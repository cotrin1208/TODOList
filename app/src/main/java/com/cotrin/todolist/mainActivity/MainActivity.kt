package com.cotrin.todolist.mainActivity

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cotrin.todolist.R
import com.cotrin.todolist.databinding.ActivityMainBinding
import com.cotrin.todolist.realm.IDGenerator
import com.cotrin.todolist.realm.RealmUtil
import com.cotrin.todolist.realm.Task
import com.cotrin.todolist.realm.TaskViewModel
import com.cotrin.todolist.task.TaskListAdapter
import com.cotrin.todolist.taskDetailFragment.TaskDetailFragment
import com.cotrin.todolist.utils.Reference
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this)[MainActivityViewModel::class.java]
    }
    private val taskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }
    companion object {
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        //通知権限の付与
        checkPermission()
        createNotificationChannel()
        //Realm設定
        val realm = RealmUtil.getRealm()
        val tasks = realm.query<Task>().find()
        lifecycleScope.launch {
            tasks.asFlow().collect {
                when (it) {
                    //更新時
                    is UpdatedResults -> taskViewModel.taskList.value = it.list
                    //起動時
                    is InitialResults -> {
                        //翌日繰り越し処理
                        taskViewModel.carryoverTasks()
                        //リピート処理
                        taskViewModel.repeatTasks()
                        //リマインド再設定処理
                        taskViewModel.setAllReminders()
                        taskViewModel.taskList.value = it.list
                    }
                }
            }
        }
        //SharedPreferencesを読み込み、リクエストIDの開始値を取得
        sharedPreferences = this@MainActivity.getSharedPreferences(Reference.APP_ID, MODE_PRIVATE)
        IDGenerator.setStartValue(sharedPreferences.getInt(Reference.REQUEST_ID_START, -1))
        //RecyclerViewの表示
        binding.adapter = TaskListAdapter(this@MainActivity, taskViewModel).apply {
            submitList(taskViewModel.taskList.value)
        }
        //タスク追加ボタンのリスナー登録
        mainViewModel.isAddFragmentShown.observe(this) {
            if (!it) return@observe
            showTaskDetailFragment(Reference.ADD)
        }
        //タスクリスト更新処理
        taskViewModel.taskList.observe(this) {
            binding.adapter?.submitList(it)
        }
        //タスク詳細ポップアップメニュー
        taskViewModel.isDetailShown.observe(this) {
            if (!it) return@observe
            showTaskDetailPopupMenu()
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

    private fun showTaskDetailFragment(mode: String) {
        TaskDetailFragment().show(supportFragmentManager, mode)
    }

    private fun showTaskDetailPopupMenu() {
        val view = taskViewModel.taskDetail.value!!.first
        PopupMenu(this@MainActivity, view).apply {
            inflate(R.menu.popup_menu_task)
            menu::class.java.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.java).apply {
                isAccessible = true
                invoke(menu, true)
            }
            //回転アニメーション
            ObjectAnimator.ofFloat(view, "rotation", 0f, -90f).apply {
                duration = 300
                addListener(object: Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) { view.rotation = -90f }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }.start()
            //クリックリスナー
            setOnMenuItemClickListener { menuItem ->
                val task = taskViewModel.taskDetail.value!!.second
                when (menuItem.itemId) {
                    //編集画面の表示
                    R.id.menu_edit -> {
                        mainViewModel.taskData.value = task
                        showTaskDetailFragment(Reference.EDIT)
                        true
                    }
                    //タスクを複製する
                    R.id.menu_copy -> {
                        taskViewModel.copyTask(task)
                        true
                    }
                    //削除ダイアログの表示
                    R.id.menu_delete -> {
                        showDeleteAlertDialog {
                            taskViewModel.deleteTask(task)
                        }
                        true
                    }
                    else -> false
                }
            }
            //閉じたときにアニメーションをもとに戻す
            setOnDismissListener {
                ObjectAnimator.ofFloat(view, "rotation", -90f, 0f).apply {
                    duration = 300
                    addListener(object: Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}

                        override fun onAnimationEnd(animation: Animator) { view.rotation = 0f }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                }.start()
            }
        }.show()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(Reference.APP_ID, "リマインダー", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "タスクリマインダー"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.createNotificationChannel(channel)
    }

    private fun checkPermission() {
        //Android13未満は通知権限不要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        requestPermissions(permissions, 123)
    }
}