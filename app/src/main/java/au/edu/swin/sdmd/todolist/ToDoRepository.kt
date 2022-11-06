package au.edu.swin.sdmd.todolist

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import au.edu.swin.sdmd.todolist.database.ToDoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val DATABASE_NAME = "todo-database"

class ToDoRepository private constructor(
    context: Context, private val coroutineScope: CoroutineScope = GlobalScope
) {
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

    private val database: ToDoDatabase = Room.databaseBuilder(
        context.applicationContext, ToDoDatabase::class.java, DATABASE_NAME
    ).addMigrations(MIGRATION_2_3, MIGRATION_3_4).build()

    fun loadAll() = database.toDoDao().loadAll()
    suspend fun loadById(id: Long) = database.toDoDao().loadById(id)
    fun updateToDo(toDo: ToDo) {
        coroutineScope.launch {
            database.toDoDao().updateToDo(toDo)
        }
    }

    fun insert(toDo: ToDo) {
        coroutineScope.launch {
            database.toDoDao().insert(toDo)
        }
    }

    suspend fun delete(toDo: ToDo) = database.toDoDao().delete(toDo)
    suspend fun deleteAll() = database.toDoDao().deleteAll()

    companion object {
        private var INSTANCE: ToDoRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ToDoRepository(context)
            }
        }

        fun get(): ToDoRepository {
            return INSTANCE ?: throw IllegalStateException("ToDoRepository must be initialized")
        }
    }

}