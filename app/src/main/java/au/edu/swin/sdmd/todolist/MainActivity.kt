package au.edu.swin.sdmd.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.swin.sdmd.todolist.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.time.ZonedDateTime


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toDoAdapter: ToDoAdapter
    private val toDoList = mutableListOf<ToDo>()
    private val toDoRepository = ToDoRepository.get()

    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getParcelableExtra<ToDo>(EXTRA_UPDATED_TO_DO)!!
                toDoRepository.updateToDo(data)
                toDoList.clear()
                toDoList.addAll(toDoRepository.loadAll())
                toDoAdapter.notifyDataSetChanged()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toDoList.addAll(toDoRepository.loadAll())

        toDoAdapter = ToDoAdapter(toDoList, detailLauncher)
        binding.toDoRecycler.apply {
            adapter = toDoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.floatingActionButton.setOnClickListener {
            val newToDo = ToDo(
                title = "To Do", reminderDateTime = ZonedDateTime.now()
            )
            val newId = toDoRepository.insert(newToDo)
            toDoList.clear()
            toDoList.addAll(toDoRepository.loadAll())
            toDoAdapter.notifyItemInserted(toDoRepository.getRowCount() - 1)
            val intent = Intent(this, DetailActivity::class.java).putExtra(
                DetailActivity.EXTRA_TO_DO, toDoRepository.loadById(newId)
            )
            detailLauncher.launch(intent)
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
                val position = viewHolder.adapterPosition
                val deletedToDo: ToDo = toDoList[position]
                toDoRepository.delete(deletedToDo)
                toDoList.clear()
                toDoList.addAll(toDoRepository.loadAll())
                toDoAdapter.notifyItemRemoved(position)
                Snackbar.make(
                    binding.toDoRecycler, "Deleted " + deletedToDo.title, Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    toDoRepository.insert(deletedToDo)
                    toDoAdapter.toDoList.add(position, deletedToDo)
                    toDoAdapter.notifyItemInserted(position)
                }.show()
            }
        }).attachToRecyclerView(binding.toDoRecycler)
    }
}