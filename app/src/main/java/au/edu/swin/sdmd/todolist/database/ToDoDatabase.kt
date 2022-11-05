package au.edu.swin.sdmd.todolist.database

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import au.edu.swin.sdmd.todolist.ToDo

@Database(
    entities = [ToDo::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = ToDoDatabase.MyAutoMigration::class)]
)
@TypeConverters(ToDoTypeConverter::class)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun toDoDao(): ToDoDao

    @RenameColumn(
        tableName = "ToDo",
        fromColumnName = "reminder_date_time",
        toColumnName = "reminderDateTime"
    )
    class MyAutoMigration : AutoMigrationSpec
}