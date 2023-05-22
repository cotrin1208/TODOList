package com.cotrin.todolist.mainActivity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.cotrin.todolist.R
import com.cotrin.todolist.ReminderInterval
import com.cotrin.todolist.RepeatInterval
import com.cotrin.todolist.Task
import com.cotrin.todolist.TaskCategory
import com.cotrin.todolist.databinding.FragmentTaskDetailBinding
import com.cotrin.todolist.utils.Reference
import com.cotrin.todolist.viewModel.MainActivityViewModel
import com.cotrin.todolist.viewModel.TaskDetailViewModel
import java.time.LocalDate
import java.time.LocalTime

class TaskDetailFragment: DialogFragment(R.layout.fragment_task_detail) {
    private lateinit var binding: FragmentTaskDetailBinding
    private val mainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }
    private val taskDetailViewModel by lazy {
        ViewModelProvider(requireActivity())[TaskDetailViewModel::class.java]
    }
    private var position: Int = 0
    private lateinit var mode: String
    private lateinit var listener: OnDialogResultListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        arguments?.let {
            position = requireArguments().getInt(Reference.POSITION, 0)
        }
        mode = tag as String

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_detail, container, false)
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = taskDetailViewModel
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setGravity(Gravity.CENTER)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.6f)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Viewの初期化
        val task = mainViewModel.task
        taskDetailViewModel.name.value = task.name
        taskDetailViewModel.date.value = task.date
        taskDetailViewModel.time.value = task.time
        taskDetailViewModel.remind.value = task.remind
        taskDetailViewModel.repeat.value = task.repeat
        taskDetailViewModel.category.value = task.category
        taskDetailViewModel.carryover.value = task.carryover
        //タスク名
        binding.taskTitleText.apply {
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                    taskDetailViewModel.name.value = text.toString()
                }
            })
        }
        //日付ブロック
        binding.dateBlock.setOnClickListener { showDatePickerDialog() }
        //時刻ブロック
        binding.timeBlock.setOnClickListener { showTimePickerDialog() }
        //リマインドブロック
        binding.remindBlock.setOnClickListener { showReminderDialog() }
        //リピートブロック
        binding.repeatBlock.setOnClickListener { showRepeatDialog() }
        //繰り越しブロック
        binding.carryoverBlock.setOnClickListener {
            binding.taskCarryoverSwitch.toggle()
            taskDetailViewModel.carryover.value = binding.taskCarryoverSwitch.isChecked
        }
        //カテゴリブロック
        binding.categoryBlock.setOnClickListener { showCategoryDialog() }
        //MainViewModelのタスクを連動して変更する
        taskDetailViewModel.name.observe(requireActivity()) { mainViewModel.task.name = it }
        taskDetailViewModel.date.observe(requireActivity()) { mainViewModel.task.date = it }
        taskDetailViewModel.time.observe(requireActivity()) { mainViewModel.task.time = it }
        taskDetailViewModel.remind.observe(requireActivity()) { mainViewModel.task.remind = it }
        taskDetailViewModel.repeat.observe(requireActivity()) { mainViewModel.task.repeat = it }
        taskDetailViewModel.category.observe(requireActivity()) { mainViewModel.task.category = it }
        taskDetailViewModel.carryover.observe(requireActivity()) { mainViewModel.task.carryover = it }
        //タスク登録ボタン
        binding.applyTaskButton.apply {
            setOnClickListener {
                listener.onDialogResult(mainViewModel.taskData.value!!, position, mode)
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
        mainViewModel.isAddFragmentShown.value = false
        mainViewModel.task = Task()
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(requireContext()).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                taskDetailViewModel.date.value = date
            }
            show()
        }
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(requireContext(), { _, hour, minute ->
            val time = LocalTime.of(hour, minute)
            taskDetailViewModel.time.value = time },
            0, 0, true).apply {
                setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.remove_time)) { _, _ ->
                    taskDetailViewModel.time.value = null
                    binding.taskRemindText.text = ReminderInterval.NONE.OptionName
                    binding.remindBlock.isEnabled = false
                    binding.taskRemindText.isEnabled = false
                }
        }.show()
        binding.remindBlock.isEnabled = true
        binding.taskRemindText.isEnabled = true
    }

    private fun showReminderDialog() {
        val reminderOptions = enumValues<ReminderInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("リマインダーを設定")
            setItems(reminderOptions) { _, option ->
                taskDetailViewModel.remind.value = ReminderInterval.values()[option]
            }
        }.show()
    }

    private fun showRepeatDialog() {
        val repeatOptions = enumValues<RepeatInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("繰り返し間隔を設定")
            setItems(repeatOptions) { _, option ->
                taskDetailViewModel.repeat.value = RepeatInterval.values()[option]
            }
        }.show()
    }

    private fun showCategoryDialog() {
        val categoryOptions = enumValues<TaskCategory>().map { it.categoryName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("カテゴリーを設定")
            setItems(categoryOptions) { _, option ->
                taskDetailViewModel.category.value = TaskCategory.values()[option]
            }
        }.show()
    }
}