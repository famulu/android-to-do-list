package au.edu.swin.sdmd.todolist.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import au.edu.swin.sdmd.todolist.ToDo

@Database(entities = [ToDo::class], version = 1)
@TypeConverters(ToDoTypeConverter::class)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun toDoDao(): ToDoDao
}