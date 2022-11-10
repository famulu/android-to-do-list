package au.edu.swin.sdmd.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ToDoDetailViewModel(toDoId: Long) : ViewModel() {
    private val toDoRepository: ToDoRepository = ToDoRepository.get()
    private val _toDo: MutableStateFlow<ToDo?> = MutableStateFlow(null)
    val toDo: StateFlow<ToDo?> = _toDo.asStateFlow()
    var initialToDo: ToDo? = null

    init {
        if (toDoId == 0L) {
            _toDo.value = ToDo(title = "", reminderDateTime = null)
        } else {
            viewModelScope.launch {
                _toDo.value = toDoRepository.loadById(toDoId)
                initialToDo = _toDo.value
            }
        }
    }

    fun updateDatabase() {
        toDo.value?.let {
            if (it.id <= 0L) {
                toDoRepository.insert(it)
            } else {
                toDoRepository.updateToDo(it)
            }
        }
    }
    fun updateToDo(onUpdate: (ToDo) -> ToDo) {
        _toDo.update { oldToDo ->
            oldToDo?.let {
                onUpdate(it)
            }
        }
    }
}

class ToDoDetailViewModelFactory(private val toDoId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ToDoDetailViewModel(toDoId) as T
    }
}