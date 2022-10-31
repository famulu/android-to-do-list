package au.edu.swin.sdmd.todolist.database

import androidx.room.*
import au.edu.swin.sdmd.todolist.ToDo

@Dao
interface ToDoDao {
    @Query("SELECT * FROM ToDo")
    fun loadAll(): List<ToDo>

    @Query("SELECT * FROM ToDo WHERE id = :toDoId")
    fun loadById(toDoId: Long): ToDo

    @Update
    fun updateToDo(vararg toDos: ToDo)


    @Query("SELECT COUNT(*) FROM ToDo")
    fun getRowCount(): Int

    @Insert
    fun insert(toDos: ToDo): Long

    @Delete
    fun delete(toDo: ToDo)

    @Query("DELETE FROM ToDo WHERE id = :toDoId")
    fun deleteById(toDoId: Long)

    @Query("DELETE FROM ToDo")
    fun deleteAll()
}