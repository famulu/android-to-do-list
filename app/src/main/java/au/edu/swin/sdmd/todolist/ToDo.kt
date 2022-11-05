package au.edu.swin.sdmd.todolist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity
data class ToDo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val reminderDateTime: ZonedDateTime,
    @ColumnInfo(defaultValue = "0") val isCompleted: Boolean = false
)