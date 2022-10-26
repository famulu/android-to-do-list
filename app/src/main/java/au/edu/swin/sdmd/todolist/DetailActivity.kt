package au.edu.swin.sdmd.todolist

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import au.edu.swin.sdmd.todolist.databinding.ActivityDetailBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.ZoneId


private const val DATE_PICKER_TAG = "DATE_PICKER_TAG"
private const val TIME_PICKER_TAG = "TIME_PICKER_TAG"

class DetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TO_DO = "EXTRA_TO_DO"
    }

    private lateinit var binding: ActivityDetailBinding
    private lateinit var toDo: ToDo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toDo = intent.getParcelableExtra(EXTRA_TO_DO)!!

        updateView()

        binding.title.doAfterTextChanged {
            toDo.title = it.toString()
        }

        binding.date.setOnClickListener {
            val dateUtc =
                toDo.reminderDateTime.toInstant().toEpochMilli()

            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
                .setSelection(dateUtc).build()

            datePicker.show(supportFragmentManager, DATE_PICKER_TAG)

            datePicker.addOnPositiveButtonClickListener {
                val date =
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).withFixedOffsetZone()
                toDo.reminderDateTime = date
                updateView()
            }

        }

        binding.time.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(toDo.reminderDateTime.hour).setMinute(toDo.reminderDateTime.minute)
                .setTitleText("Select time").build()
            timePicker.show(supportFragmentManager, TIME_PICKER_TAG)

            timePicker.addOnPositiveButtonClickListener {
                toDo.reminderDateTime =
                    toDo.reminderDateTime.withHour(timePicker.hour).withMinute(timePicker.minute)
                updateView()
            }
        }
    }

    override fun onBackPressed() {
        val i = intent.apply {
            putExtra(EXTRA_UPDATED_TO_DO, toDo)
        }

        setResult(Activity.RESULT_OK, i)
        super.onBackPressed()
    }

    private fun updateView() {
        binding.title.setText(toDo.title)
        binding.time.setText(toDo.reminderDateTime.format(ToDoAdapter.timeFormatter))
        binding.date.setText(toDo.reminderDateTime.format(ToDoAdapter.dateFormatter))
    }
}