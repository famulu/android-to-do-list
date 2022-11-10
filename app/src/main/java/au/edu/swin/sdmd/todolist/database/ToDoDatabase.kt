package au.edu.swin.sdmd.todolist.database

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import au.edu.swin.sdmd.todolist.ToDo

@Database(
    entities = [ToDo::class],
    version = 4,
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

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ToDo ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT(0)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL("CREATE TABLE NewToDo (id INTEGER NOT NULL, title TEXT NOT NULL, reminderDateTime INTEGER DEFAULT NULL, isCompleted INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id))")
                // Copy the data
                database.execSQL("INSERT INTO NewToDo (id, title, reminderDateTime, isCompleted) SELECT id, title, reminderDateTime, isCompleted FROM ToDo")
                // Remove the old table
                database.execSQL("DROP TABLE ToDo")
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE NewToDo RENAME TO ToDo")
            }
        }
    }
}