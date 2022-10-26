package au.edu.swin.sdmd.todolist

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
@Entity
data class ToDo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var title: String,
    @ColumnInfo(name = "reminder_date_time") var reminderDateTime: ZonedDateTime,
) : Parcelable