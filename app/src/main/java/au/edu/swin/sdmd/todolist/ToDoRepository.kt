package au.edu.swin.sdmd.todolist

import android.content.Context
import androidx.room.Room
import au.edu.swin.sdmd.todolist.database.ToDoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val DATABASE_NAME = "todo-database"

class ToDoRepository private constructor(
    context: Context, private val coroutineScope: CoroutineScope = GlobalScope
) {
    private val database: ToDoDatabase = Room.databaseBuilder(
        context.applicationContext, ToDoDatabase::class.java, DATABASE_NAME
    ).addMigrations(ToDoDatabase.MIGRATION_2_3, ToDoDatabase.MIGRATION_3_4).build()

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