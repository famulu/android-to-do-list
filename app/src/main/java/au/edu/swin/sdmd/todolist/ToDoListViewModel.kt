package au.edu.swin.sdmd.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToDoListViewModel : ViewModel() {
    private val toDoRepository = ToDoRepository.get()
    private val _toDos: MutableStateFlow<List<ToDo>> = MutableStateFlow(emptyList())
    val toDos: StateFlow<List<ToDo>>
        get() = _toDos.asStateFlow()

    init {
        viewModelScope.launch {
            toDoRepository.loadAll().collect { toDoList ->
                _toDos.value = toDoList
            }
        }
    }

    fun insertToDo(toDo: ToDo) = toDoRepository.insert(toDo)

    suspend fun delete(toDo: ToDo) = toDoRepository.delete(toDo)

    suspend fun loadById(id: Long) = toDoRepository.loadById(id)

    fun update(toDo: ToDo) = toDoRepository.updateToDo(toDo)
}