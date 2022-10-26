package au.edu.swin.sdmd.todolist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

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
    fun insertAll(vararg toDos: ToDo)

    @Delete
    fun delete(toDo: ToDo)

    @Query("DELETE FROM ToDo WHERE id = :toDoId")
    fun deleteById(toDoId: Long)
}