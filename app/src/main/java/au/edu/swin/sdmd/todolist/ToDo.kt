package au.edu.swin.sdmd.todolist

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
@Entity
data class ToDo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val reminderDateTime: ZonedDateTime,
) : Parcelable