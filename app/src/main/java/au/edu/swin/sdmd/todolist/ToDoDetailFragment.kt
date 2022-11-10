package au.edu.swin.sdmd.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import au.edu.swin.sdmd.todolist.databinding.FragmentToDoDetailBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


private const val DATE_PICKER_TAG = "DATE_PICKER_TAG"
private const val TIME_PICKER_TAG = "TIME_PICKER_TAG"

class ToDoDetailFragment : Fragment() {
    private var _binding: FragmentToDoDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "binding cannot be accessed. Check if view is visible"
        }
    private val args: ToDoDetailFragmentArgs by navArgs()
    private val toDoDetailViewModel: ToDoDetailViewModel by viewModels {
        ToDoDetailViewModelFactory(args.toDoId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToDoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            saveButton.setOnClickListener {
                val toDo = toDoDetailViewModel.toDo.value
                if (toDo == null || toDo.title.isEmpty()) {
                    Toast.makeText(context, "Please add a title", Toast.LENGTH_SHORT).show()
                } else {
                    toDoDetailViewModel.updateDatabase()
                    MyNotification.scheduleNotification(toDo, requireContext(), binding.root)
                    findNavController().popBackStack()
                }
            }

            toDoTitle.doOnTextChanged { text, _, _, _ ->
                toDoDetailViewModel.updateToDo { oldToDo ->
                    oldToDo.copy(title = text.toString())
                }
            }

            toDoDate.setOnClickListener {
                val reminderDateTime =
                    toDoDetailViewModel.toDo.value?.reminderDateTime ?: ZonedDateTime.now()
                        .truncatedTo(ChronoUnit.DAYS)
                val dateUtc = reminderDateTime.toInstant().toEpochMilli()

                val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
                    .setSelection(dateUtc).build()

                datePicker.show(parentFragmentManager, DATE_PICKER_TAG)

                datePicker.addOnPositiveButtonClickListener {
                    val updatedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                    toDoDetailViewModel.updateToDo { oldToDo ->
                        oldToDo.copy(reminderDateTime = updatedDate)
                    }
                }
            }

            toDoTime.setOnClickListener {
                val reminderDateTime =
                    toDoDetailViewModel.toDo.value?.reminderDateTime ?: ZonedDateTime.now()
                        .truncatedTo(ChronoUnit.HOURS).plusHours(1)

                val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(reminderDateTime.hour).setMinute(reminderDateTime.minute)
                    .setTitleText("Select time").build()

                timePicker.show(parentFragmentManager, TIME_PICKER_TAG)

                timePicker.addOnPositiveButtonClickListener {
                    toDoDetailViewModel.updateToDo { oldToDo ->
                        val newReminderDateTime = oldToDo.reminderDateTime ?: ZonedDateTime.now()
                            .truncatedTo(ChronoUnit.MINUTES)
                        oldToDo.copy(
                            reminderDateTime = newReminderDateTime.withHour(timePicker.hour)
                                .withMinute(timePicker.minute)
                        )
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

        requireActivity().onBackPressedDispatcher.addCallback(this, true) {


            val toDo = toDoDetailViewModel.toDo.value

            if (toDo == null || toDo.id <= 0L && toDo.title.isEmpty() && toDo.reminderDateTime == null) {
                findNavController().popBackStack()
            } else {
                MaterialAlertDialogBuilder(requireContext()).setTitle("Discard current task?")
                    .setMessage("Are you sure you want to discard the current draft?")
                    .setNegativeButton("Cancel") { dialog, which ->

                    }.setPositiveButton("Discard") { dialog, which ->
                        findNavController().popBackStack()
                    }.show()
            }

        }
    }


    private fun updateUi(toDo: ToDo) {
        binding.apply {
            if (toDoTitle.text.toString() != toDo.title) {
                toDoTitle.setText(toDo.title)
            }
            toDo.reminderDateTime?.let {
                if (toDoDate.text.toString() != it.format(ToDoAdapter.dateFormatter)) {
                    toDoDate.setText(it.format(ToDoAdapter.dateFormatter))
                }
                if (toDoTime.text.toString() != it.format(ToDoAdapter.timeFormatter)) {
                    toDoTime.setText(it.format(ToDoAdapter.timeFormatter))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}