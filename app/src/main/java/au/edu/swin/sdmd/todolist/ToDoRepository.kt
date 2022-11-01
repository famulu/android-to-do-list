package au.edu.swin.sdmd.todolist

import android.content.Context
import androidx.room.Room
import au.edu.swin.sdmd.todolist.database.ToDoDatabase

private const val DATABASE_NAME = "todo-database"

class ToDoRepository private constructor(context: Context) {
    private val database: ToDoDatabase = Room.databaseBuilder(
        context.applicationContext, ToDoDatabase::class.java, DATABASE_NAME
    ).allowMainThreadQueries().build()

    fun loadAll() = database.toDoDao().loadAll()
    fun loadById(id: Long) = database.toDoDao().loadById(id)
    fun updateToDo(toDo: ToDo) = database.toDoDao().updateToDo(toDo)
    fun insert(toDo: ToDo) = database.toDoDao().insert(toDo)
    fun getRowCount() = database.toDoDao().getRowCount()
    fun delete(toDo: ToDo) = database.toDoDao().delete(toDo)

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