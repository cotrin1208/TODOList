package com.cotrin.todolist.mainActivity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.cotrin.todolist.R
import com.cotrin.todolist.ReminderInterval
import com.cotrin.todolist.RepeatInterval
import com.cotrin.todolist.Task
import com.cotrin.todolist.TaskCategory
import com.cotrin.todolist.databinding.FragmentTaskDetailBinding
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.utils.getTask
import java.time.LocalDate
import java.time.LocalTime

class TaskDetailFragment: DialogFragment(R.layout.fragment_task_detail) {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var task: Task
    private var position: Int = 0
    private lateinit var mode: String
    private lateinit var listener: OnDialogResultListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        arguments?.let {
            task = requireArguments().getTask(Reference.TASK)
            position = requireArguments().getInt(Reference.POSITION, 0)
        }

        Toast.makeText(requireContext(), tag as String, Toast.LENGTH_SHORT).show()
        mode = tag as String

        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setGravity(Gravity.CENTER)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.5f)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Viewの初期化
        //タスク名
        binding.taskTitleText.apply {
            setText(task.name)
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                    task = task.copy(name = text.toString())
                }
            })
        }
        //日付表示
        binding.taskDateText.apply {
            text = task.date.toString()
            setOnClickListener { showDatePickerDialog() }
        }
        //時刻表示
        binding.taskTimeText.apply {
            text = task.time?.format(Reference.TIME_FORMATTER) ?: run { "**:**" }
            setOnClickListener { showTimePickerDialog() }
        }
        //時刻キャンセルボタン
        binding.deleteTaskTimeIcon.setOnClickListener {
            binding.taskTimeText.text = "**:**"
            binding.taskRemindText.isEnabled = false
            binding.taskRemindText.text = ReminderInterval.NONE.OptionName
            task = task.copy(time = null, remindInterval = ReminderInterval.NONE)
        }
        //リマインド表示
        binding.taskRemindText.apply {
            isEnabled = (task.time != null)
            text = task.remindInterval.OptionName
            setOnClickListener { showReminderDialog() }
        }
        //リピート表示
        binding.taskRepeatText.apply {
            text = task.repeatInterval.OptionName
            setOnClickListener { showRepeatDialog() }
        }
        //繰り越し表示
        binding.taskCarryoverSwitch.apply {
            isChecked = task.carryover
            setOnCheckedChangeListener { _, bool ->
                task = task.copy(carryover = bool)
            }
        }
        //カテゴリ表示
        binding.taskCategoryText.apply {
            setOnClickListener { showCategoryDialog() }
            text = task.category.categoryName
            val drawable = ContextCompat.getDrawable(requireContext(), task.category.iconResId)
            binding.taskCategoryIcon.setImageDrawable(drawable)
        }
        //タスク登録ボタン
        binding.applyTaskButton.apply {
            setOnClickListener {
                listener.onDialogResult(task, position, mode)
                dismiss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as MainActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(requireContext()).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                task = task.copy(date = LocalDate.of(year, month + 1, dayOfMonth))
                updateView()
            }
            show()
        }
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(requireContext(), { _, hour, minute ->
            task = task.copy(time = LocalTime.of(hour, minute))
            updateView() }, 0, 0, true).show()
        binding.taskRemindText.isEnabled = true
    }

    private fun showReminderDialog() {
        val reminderOptions = enumValues<ReminderInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("リマインダーを設定")
            setItems(reminderOptions) { _, option ->
                task = task.copy(remindInterval = ReminderInterval.values()[option])
                updateView()
            }
        }.show()
    }

    private fun showRepeatDialog() {
        val repeatOptions = enumValues<RepeatInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("繰り返し間隔を設定")
            setItems(repeatOptions) { _, option ->
                task = task.copy(repeatInterval = RepeatInterval.values()[option])
                updateView()
            }
        }.show()
    }

    private fun showCategoryDialog() {
        val categoryOptions = enumValues<TaskCategory>().map { it.categoryName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("カテゴリーを設定")
            setItems(categoryOptions) { _, option ->
                task = task.copy(category = TaskCategory.values()[option])
                val drawable = ContextCompat.getDrawable(requireContext(), task.category.iconResId)
                binding.taskCategoryIcon.setImageDrawable(drawable)
                updateView()
            }
        }.show()
    }

    private fun updateView() {
        binding.taskDateText.text = task.date.format(Reference.DATE_FORMATTER)
        binding.taskTimeText.text = task.time?.format(Reference.TIME_FORMATTER) ?: run { "**:**" }
        binding.taskRemindText.text = task.remindInterval.OptionName
        binding.taskRepeatText.text = task.repeatInterval.OptionName
        binding.taskCarryoverSwitch.isChecked = task.carryover
        binding.taskCategoryText.text = task.category.categoryName
    }
}