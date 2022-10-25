package au.edu.swin.sdmd.todolist

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.swin.sdmd.todolist.databinding.ToDoItemBinding
import java.time.format.DateTimeFormatter

const val EXTRA_UPDATED_TO_DO = "EXTRA_UPDATED_TO_DO"

class ToDoAdapter(private val toDoList: List<ToDo>) :
    RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ToDoItemBinding.inflate(inflater, parent, false)
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        val toDo = toDoList[position]
        holder.bind(toDo)
    }

    override fun getItemCount() = toDoList.size

    inner class ToDoViewHolder(private val binding: ToDoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(toDo: ToDo) {
//            val activityLauncher =
//                (binding.root.context as ComponentActivity).registerForActivityResult(
//                    ActivityResultContracts.StartActivityForResult()
//                ) { result ->
//                    if (result.resultCode == Activity.RESULT_OK) {
//                        val updatedTodo =
//                            result.data?.getParcelableExtra<ToDo>(EXTRA_UPDATED_TO_DO)!!
//                        toDo.apply {
//                            title = updatedTodo.title
//                            reminderDate = updatedTodo.reminderDate
//                            reminderTime = updatedTodo.reminderTime
//                        }
//                    }
//                }

            binding.title.text = toDo.title
            binding.reminderDate.text = toDo.reminderDate.format(dateFormatter)
            binding.reminderTime.text = toDo.reminderTime.format(timeFormatter)

            binding.root.setOnClickListener {
                val intent = Intent(
                    it.context, DetailActivity::class.java
                ).putExtra(DetailActivity.EXTRA_TO_DO, toDo)
                it.context.startActivity(intent)
            }
        }
    }

    companion object {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    }
}

