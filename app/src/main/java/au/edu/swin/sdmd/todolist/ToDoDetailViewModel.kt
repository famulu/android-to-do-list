package au.edu.swin.sdmd.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ToDoDetailViewModel(
    /**
     * If 0, create a new ToDo
     * Else, update an existing ToDo
     */
    toDoId: Long
) : ViewModel() {
    private val toDoRepository: ToDoRepository = ToDoRepository.get()
    private val _toDo: MutableStateFlow<ToDo?> = MutableStateFlow(null)
    val toDo: StateFlow<ToDo?> = _toDo.asStateFlow()
    var changesExist: Boolean = false
        private set


    init {
        if (isNewToDo(toDoId)) {
            _toDo.value = ToDo(title = "New To Do", reminderDateTime = null)
        } else {
            viewModelScope.launch {
                _toDo.value = toDoRepository.loadById(toDoId)
            }
        }
    }

    /**
     * We can't set id to null, since nullable longs are not allowed in navigation args
     * So, 0 is used to indicate the absence of a ToDo
     */
    private fun isNewToDo(id: Long) = id == 0L

    fun updateDatabase() {
        toDo.value?.let {
            if (isNewToDo(it.id)) {
                toDoRepository.insert(it)
            } else {
                toDoRepository.updateToDo(it)
            }
        }
    }
    fun updateToDo(onUpdate: (ToDo) -> ToDo) {
        _toDo.update { oldToDo ->
            oldToDo?.let {
                changesExist = true
                onUpdate(it)
            }
        }
    }

    suspend fun deleteToDo(toDo: ToDo) {
        toDoRepository.delete(toDo)
    }
}

class ToDoDetailViewModelFactory(private val toDoId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ToDoDetailViewModel(toDoId) as T
    }
}