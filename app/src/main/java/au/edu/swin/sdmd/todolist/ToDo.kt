package au.edu.swin.sdmd.todolist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class ToDo(var title: String, var reminderDate: LocalDate, var reminderTime: LocalTime) : Parcelable