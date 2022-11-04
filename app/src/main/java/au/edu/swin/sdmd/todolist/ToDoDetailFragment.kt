package au.edu.swin.sdmd.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import au.edu.swin.sdmd.todolist.databinding.FragmentToDoDetailBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId


const val DATE_PICKER_TAG = "DATE_PICKER_TAG"
const val TIME_PICKER_TAG = "TIME_PICKER_TAG"

class ToDoDetailFragment : Fragment() {
    private var _binding: FragmentToDoDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "binding cannot be accessed. Check if view is visible"
        }
    private val toDoDetailViewModel: ToDoDetailViewModel by viewModels {
        ToDoDetailViewModelFactory(args.toDoId)
    }
    private val args: ToDoDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToDoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toDoTitle.doOnTextChanged { text, _, _, _ ->
                toDoDetailViewModel.updateToDo { oldToDo ->
                    oldToDo.copy(title = text.toString())
                }
            }

            toDoDate.setOnClickListener {
                val dateUtc =
                    toDoDetailViewModel.toDo.value?.reminderDateTime?.toInstant()?.toEpochMilli()

                val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
                    .setSelection(dateUtc).build()

                datePicker.show(parentFragmentManager, DATE_PICKER_TAG)

                datePicker.addOnPositiveButtonClickListener {
                    val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                        .withFixedOffsetZone()
                    toDoDetailViewModel.updateToDo { oldToDo ->
                        oldToDo.copy(reminderDateTime = date)
                    }
                }
            }

            toDoTime.setOnClickListener {
                val toDo = toDoDetailViewModel.toDo.value
                toDo?.let {
                    val timePicker =
                        MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(it.reminderDateTime.hour).setMinute(it.reminderDateTime.minute)
                            .setTitleText("Select time").build()
                    timePicker.show(parentFragmentManager, TIME_PICKER_TAG)

                    timePicker.addOnPositiveButtonClickListener {
                        toDoDetailViewModel.updateToDo { oldToDo ->
                            oldToDo.copy(
                                reminderDateTime = oldToDo.reminderDateTime.withHour(
                                    timePicker.hour
                                ).withMinute(timePicker.minute)
                            )
                        }
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                toDoDetailViewModel.toDo.collect { toDo ->
                    toDo?.let {
                        updateUi(it)
                    }
                }
            }
        }
    }

    private fun scheduleNotification(toDo: ToDo) {
        val intent =
            Intent(requireContext().applicationContext, MyNotification::class.java).putExtra(
                ID_EXTRA,
                toDo.id.toInt()
            ).putExtra(TITLE_EXTRA, toDo.title)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext,
            toDo.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = toDo.reminderDateTime.toInstant().toEpochMilli()
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

        Snackbar.make(
            binding.root,
            "Notification has been scheduled for ${toDo.reminderDateTime}",
            Snackbar.LENGTH_SHORT
        ).show()
    }


    private fun updateUi(toDo: ToDo) {
        binding.apply {
            if (toDoTitle.text.toString() != toDo.title) {
                toDoTitle.setText(toDo.title)
            }
            if (toDoDate.text.toString() != toDo.reminderDateTime.format(ToDoAdapter.dateFormatter)) {
                toDoDate.setText(toDo.reminderDateTime.format(ToDoAdapter.dateFormatter))
            }
            if (toDoTime.text.toString() != toDo.reminderDateTime.format(ToDoAdapter.timeFormatter)) {
                toDoTime.setText(toDo.reminderDateTime.format(ToDoAdapter.timeFormatter))
            }
        }
        scheduleNotification(toDo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}