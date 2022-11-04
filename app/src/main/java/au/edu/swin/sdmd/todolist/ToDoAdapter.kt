package au.edu.swin.sdmd.todolist

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import au.edu.swin.sdmd.todolist.databinding.ToDoItemBinding
import java.time.format.DateTimeFormatter

const val EXTRA_UPDATED_TO_DO = "EXTRA_UPDATED_TO_DO"

class ToDoAdapter(
    val toDoList: List<ToDo>,
    private val onToDoClicked: (toDoId: Long) -> Unit
) : RecyclerView.Adapter<ToDoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ToDoItemBinding.inflate(inflater, parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val toDo = toDoList[position]
        holder.bind(toDo, onToDoClicked)
    }

    override fun getItemCount() = toDoList.size

    override fun getItemId(position: Int): Long {
        return toDoList[position].id
    }

    companion object {
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM")
    }
}

class ToDoViewHolder(private val binding: ToDoItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(toDo: ToDo, onToDoClicked: (toDoId: Long) -> Unit) {
        binding.toDoItemTitle.text = toDo.title
        binding.reminderDate.text = toDo.reminderDateTime.format(ToDoAdapter.dateFormatter)
        binding.reminderTime.text = toDo.reminderDateTime.format(ToDoAdapter.timeFormatter)

        binding.root.setOnClickListener {
            onToDoClicked(toDo.id)
        }
    }
}



