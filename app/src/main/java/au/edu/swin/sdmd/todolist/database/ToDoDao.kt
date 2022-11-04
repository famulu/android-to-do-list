package au.edu.swin.sdmd.todolist.database

import androidx.room.*
import au.edu.swin.sdmd.todolist.ToDo
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Query("SELECT * FROM ToDo")
    fun loadAll(): Flow<List<ToDo>>

    @Query("SELECT * FROM ToDo WHERE id = :toDoId")
    suspend fun loadById(toDoId: Long): ToDo

    @Update
    suspend fun updateToDo(toDos: ToDo)

    @Insert
    suspend fun insert(toDo: ToDo): Long

    @Delete
    suspend fun delete(toDo: ToDo)

    @Query("DELETE FROM ToDo")
    suspend fun deleteAll()
}