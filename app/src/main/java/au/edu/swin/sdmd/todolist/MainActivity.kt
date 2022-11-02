package au.edu.swin.sdmd.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.swin.sdmd.todolist.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.ZonedDateTime


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toDoAdapter: ToDoAdapter
    private val toDoList = mutableListOf<ToDo>()
    private val toDoRepository = ToDoRepository.get()
    private var hasLoaded = false

    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getParcelableExtra<ToDo>(EXTRA_UPDATED_TO_DO)!!
                lifecycleScope.launch {
                    toDoRepository.updateToDo(data)
                    val position = toDoList.indexOfFirst { toDo -> data.id == toDo.id  }
                    toDoAdapter.notifyItemChanged(position)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toDoAdapter = ToDoAdapter(toDoList, detailLauncher)
        binding.toDoRecycler.apply {
            adapter = toDoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                toDoRepository.loadAll().collect {
                    toDoList.clear()
                    toDoList.addAll(it)
                    if (!hasLoaded) {
                        toDoAdapter.notifyDataSetChanged()
                        hasLoaded = true
                    }
                }
            }
        }


        binding.floatingActionButton.setOnClickListener {
            val newToDo = ToDo(
                title = "To Do", reminderDateTime = ZonedDateTime.now()
            )
            lifecycleScope.launch {
                val newId = toDoRepository.insert(newToDo)
                val intent = Intent(this@MainActivity, DetailActivity::class.java).putExtra(
                    DetailActivity.EXTRA_TO_DO, toDoRepository.loadById(newId)
                )
                detailLauncher.launch(intent)
            }
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
                lifecycleScope.launch {
                    toDoRepository.delete(deletedToDo)
                    toDoAdapter.notifyItemRemoved(position)
                    Snackbar.make(
                        binding.toDoRecycler, "Deleted " + deletedToDo.title, Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        lifecycleScope.launch {
                            toDoRepository.insert(deletedToDo)
                            toDoAdapter.notifyItemInserted(position)
                        }
                    }.show()
                }
            }
        }).attachToRecyclerView(binding.toDoRecycler)
    }
}