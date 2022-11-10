package au.edu.swin.sdmd.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompletedToDoListViewModel : ViewModel() {
    private val toDoRepository = ToDoRepository.get()
    private val _toDos: MutableStateFlow<List<ToDo>> = MutableStateFlow(emptyList())
    val toDos: StateFlow<List<ToDo>>
        get() = _toDos.asStateFlow()

    init {
        viewModelScope.launch {
            toDoRepository.loadAll().collect { toDoList ->
                _toDos.value = toDoList.filter { it.isCompleted }
            }
        }
    }
}