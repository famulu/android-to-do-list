package au.edu.swin.sdmd.todolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import au.edu.swin.sdmd.todolist.databinding.ToDoItemBinding
import java.time.format.DateTimeFormatter

class ToDoAdapter(
    val toDoList: List<ToDo>, private val onToDoClicked: (toDoId: Long) -> Unit
) : RecyclerView.Adapter<ToDoViewHolder>() {
    init {
        setHasStableIds(true)
    }

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

class ToDoViewHolder(private val binding: ToDoItemBinding) : ViewHolder(binding.root) {

    fun bind(toDo: ToDo, onToDoClicked: (toDoId: Long) -> Unit) {
        binding.toDoItemTitle.text = toDo.title
        if (toDo.isCompleted) {
            binding.toDoItemTitle.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
        toDo.reminderDateTime?.let {
            binding.reminderDate.text = it.format(ToDoAdapter.dateFormatter)
            binding.reminderTime.text = it.format(ToDoAdapter.timeFormatter)
        }

        binding.root.setOnClickListener {
            onToDoClicked(toDo.id)
        }
    }
}
