package com.cotrin.todolist.taskDetailFragment

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.cotrin.todolist.R
import com.cotrin.todolist.databinding.FragmentTaskDetailBinding
import com.cotrin.todolist.enums.ReminderInterval
import com.cotrin.todolist.enums.RepeatInterval
import com.cotrin.todolist.enums.TaskCategory
import com.cotrin.todolist.mainActivity.MainActivityViewModel
import com.cotrin.todolist.realm.TaskViewModel
import com.cotrin.todolist.utils.Reference
import java.time.LocalDate
import java.time.LocalTime

class TaskDetailFragment: DialogFragment(R.layout.fragment_task_detail) {
    private lateinit var binding: FragmentTaskDetailBinding
    private val mainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
    }
    private val taskViewModel by lazy {
        ViewModelProvider(requireActivity())[TaskViewModel::class.java]
    }
    private lateinit var mode: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mode = tag as String

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_detail, container, false)
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = mainViewModel
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
        //タスク名
        binding.taskTitleText.apply {
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                    mainViewModel.setName(text.toString())
                }
            })
        }
        //日付ブロック
        binding.dateBlock.setOnClickListener { showDatePickerDialog() }
        //時刻ブロック
        binding.timeBlock.setOnClickListener { showTimePickerDialog() }
        //リマインドブロック
        binding.remindBlock.setOnClickListener {
            showReminderDialog()
        }
        binding.remindBlock.isEnabled = !mainViewModel.isNullTime()
        binding.taskRemindText.isEnabled = !mainViewModel.isNullTime()
        //リピートブロック
        binding.repeatBlock.setOnClickListener { showRepeatDialog() }
        //繰り越しブロック
        binding.carryoverBlock.setOnClickListener {
            binding.taskCarryoverSwitch.toggle()
            mainViewModel.setCarryover(binding.taskCarryoverSwitch.isChecked)
        }
        //カテゴリブロック
        binding.categoryBlock.setOnClickListener { showCategoryDialog() }
        //タスク登録ボタン
        binding.applyTaskButton.apply {
            setOnClickListener {
                when (mode) {
                    Reference.ADD -> taskViewModel.addTask(mainViewModel.taskData.value!!)
                    Reference.EDIT -> taskViewModel.editTask(mainViewModel.taskData.value!!)
                }
                dismiss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.isAddFragmentShown.value = false
        mainViewModel.clearTask()
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(requireContext()).apply {
            setOnDateSetListener { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                mainViewModel.setDate(date)
            }
            show()
        }
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(requireContext(), { _, hour, minute ->
            val time = LocalTime.of(hour, minute)
            mainViewModel.setTime(time) },
            0, 0, true).apply {
                setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.remove_time)) { _, _ ->
                    mainViewModel.setTime(null)
                    binding.taskRemindText.text = ReminderInterval.NONE.optionName
                    binding.remindBlock.isEnabled = false
                    binding.taskRemindText.isEnabled = false
                }
        }.show()
        binding.remindBlock.isEnabled = true
        binding.taskRemindText.isEnabled = true
    }

    private fun showReminderDialog() {
        val reminderOptions = enumValues<ReminderInterval>().map { it.optionName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("リマインダーを設定")
            setItems(reminderOptions) { _, option ->
                mainViewModel.setRemind(ReminderInterval.values()[option])
            }
        }.show()
    }

    private fun showRepeatDialog() {
        val repeatOptions = enumValues<RepeatInterval>().map { it.OptionName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("繰り返し間隔を設定")
            setItems(repeatOptions) { _, option ->
                mainViewModel.setRepeat(RepeatInterval.values()[option])
            }
        }.show()
    }

    private fun showCategoryDialog() {
        val categoryOptions = enumValues<TaskCategory>().map { it.categoryName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("カテゴリーを設定")
            setItems(categoryOptions) { _, option ->
                mainViewModel.setCategory(TaskCategory.values()[option])
            }
        }.show()
    }
}