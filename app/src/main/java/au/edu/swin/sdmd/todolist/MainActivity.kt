package au.edu.swin.sdmd.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.swin.sdmd.todolist.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var toDoList: MutableList<ToDo>

    private lateinit var toDoAdapter: ToDoAdapter

    private val detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getParcelableExtra<ToDo>(EXTRA_UPDATED_TO_DO)!!
            toDoList[data.index] = data
            toDoAdapter.notifyItemChanged(data.index)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toDoList = MutableList(100) {
            ToDo(it, it.toString(), LocalDate.now(), LocalTime.now())
        }

        toDoAdapter = ToDoAdapter(toDoList, detailLauncher)
        binding.toDoList.apply {
            adapter = toDoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.floatingActionButton.setOnClickListener {
            val newToDo = ToDo(toDoList.size, toDoList.size.toString(), LocalDate.of(1999, 1, 25), LocalTime.now())
            toDoList.add(newToDo)
            val intent = Intent(this, DetailActivity::class.java).putExtra(DetailActivity.EXTRA_TO_DO, newToDo)
            detailLauncher.launch(intent)
            runBlocking {

            }
            toDoAdapter.notifyItemInserted(toDoList.size - 1)
            binding.toDoList.scrollToPosition(toDoList.size - 1)
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedToDo: ToDo = toDoList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                toDoList.removeAt(viewHolder.adapterPosition)
                toDoAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Snackbar.make(binding.toDoList, "Deleted " + deletedToDo.title, Snackbar.LENGTH_LONG).setAction("Undo",
                    {
                        toDoList.add(position, deletedToDo)
                        toDoAdapter.notifyItemInserted(position)
                    }).show()
            }
        }).attachToRecyclerView(binding.toDoList)
    }
}