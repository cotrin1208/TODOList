package com.cotrin.todolist.taskDetailActivity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cotrin.todolist.*
import com.cotrin.todolist.utils.GsonUtils
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.getTaskExtra
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var taskNameEditText: EditText
    private lateinit var textViewDate: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewReminder: TextView
    private lateinit var textViewRepeat: TextView
    private lateinit var carryOverCheckBox: CheckBox
    private lateinit var textViewCategory: TextView
    private var task = Task("", LocalDate.now(), LocalTime.now(), ReminderInterval.NONE, RepeatInterval.NONE, false, false, UUID.randomUUID(), TaskCategory.OTHER, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        //Viewの取得
        taskNameEditText = findViewById(R.id.editTextTaskName)
        val timeClearText: TextView = findViewById(R.id.timeClearText)
        val saveTaskButton: FloatingActionButton = findViewById(R.id.saveTaskButton)
        val backMainMenuButton: FloatingActionButton = findViewById(R.id.cancelButton)
        textViewDate= findViewById(R.id.textViewDate)
        textViewTime = findViewById(R.id.textViewTime)
        textViewReminder = findViewById(R.id.textViewReminder)
        textViewRepeat = findViewById(R.id.textViewRepeat)
        carryOverCheckBox = findViewById(R.id.carryOverCheckBox)
        textViewCategory = findViewById(R.id.textViewCategory)

        //MainActivityからIntentを受け取る
        val intent = intent
        if (intent.hasExtra(Reference.TASK)) task = intent.getTaskExtra(Reference.TASK)
        val position  = intent.getIntExtra(Reference.TASK_POSITION, -1)
        if (task.time == null) textViewReminder.isEnabled = false


        //Viewの初期設定
        taskNameEditText.setText(task.name)
        updateView()

        //リスナー設定
        textViewDate.setOnClickListener { showDatePickerDialog() }
        textViewTime.setOnClickListener { showTimePickerDialog() }
        textViewReminder.setOnClickListener { showReminderDialog() }
        textViewRepeat.setOnClickListener { showRepeatDialog() }
        taskNameEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {}

            override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
                task = task.copy(name = p0.toString())
            }
        })
        carryOverCheckBox.setOnClickListener {
            task = task.copy(carryOver = carryOverCheckBox.isChecked)
        }
        textViewCategory.setOnClickListener { showCategoryDialog() }

        timeClearText.setOnClickListener {
            textViewTime.text = "**:**"
            textViewReminder.isEnabled = false
            textViewReminder.text = ReminderInterval.NONE.OptionName
            task = task.copy(time = null, remindInterval = ReminderInterval.NONE)
        }

        saveTaskButton.setOnClickListener {
            val resultIntent = Intent().apply {
                val gson = GsonUtils.getCustomGson()
                val json = gson.toJson(task)
                putExtra(Reference.TASK, json)
                if (this.hasExtra(Reference.TASK_POSITION)) putExtra(Reference.TASK_POSITION, position)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        backMainMenuButton.setOnClickListener { finish() }
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(this).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                task = task.copy(date = LocalDate.of(year, month + 1, dayOfMonth))
                updateView()
            }
            show()
        }
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(this, { _, hour, minute ->
            task = task.copy(time = LocalTime.of(hour, minute))
            updateView() }, 0, 0, true).show()
        textViewReminder.isEnabled = true
    }

    private fun showReminderDialog() {
        val reminderOptions = enumValues<ReminderInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(this).apply {
            setTitle("リマインダーを設定")
            setItems(reminderOptions) { _, option ->
                task = task.copy(remindInterval = ReminderInterval.values()[option])
                updateView()
            }
        }.show()
    }

    private fun showRepeatDialog() {
        val repeatOptions = enumValues<RepeatInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(this).apply {
            setTitle("繰り返し間隔を設定")
            setItems(repeatOptions) { _, option ->
                task = task.copy(repeatInterval = RepeatInterval.values()[option])
                updateView()
            }
        }.show()
    }

    private fun showCategoryDialog() {
        val categoryOptions = enumValues<TaskCategory>().map { it.categoryName }.toTypedArray()
        AlertDialog.Builder(this).apply {
            setTitle("カテゴリーを設定")
            setItems(categoryOptions) { _, option ->
                task = task.copy(category = TaskCategory.values()[option])
                updateView()
            }
        }.show()
    }

    private fun updateView() {
        textViewDate.text = task.date.format(Reference.DATE_FORMATTER)
        textViewTime.text = task.time?.format(Reference.TIME_FORMATTER) ?: run {
            "**:**"
        }
        textViewReminder.text = task.remindInterval.OptionName
        textViewRepeat.text = task.repeatInterval.OptionName
        carryOverCheckBox.isChecked = task.carryOver
        textViewCategory.text = task.category.categoryName
    }
}