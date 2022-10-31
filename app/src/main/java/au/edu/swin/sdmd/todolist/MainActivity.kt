package au.edu.swin.sdmd.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import au.edu.swin.sdmd.todolist.database.ToDoDatabase
import au.edu.swin.sdmd.todolist.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.time.ZonedDateTime

private const val DATABASE_NAME = "TO_DO_DATABASE"


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var db: ToDoDatabase
    private val toDoList = mutableListOf<ToDo>()

    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getParcelableExtra<ToDo>(EXTRA_UPDATED_TO_DO)!!

                val dataDb = db.toDoDao().loadById(data.id)
                dataDb.apply {
                    title = data.title
                    reminderDateTime = data.reminderDateTime
                }

                db.toDoDao().updateToDo(dataDb)

                toDoList.clear()
                toDoList.addAll(db.toDoDao().loadAll())
                toDoAdapter.notifyDataSetChanged()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(applicationContext, ToDoDatabase::class.java, DATABASE_NAME).allowMainThreadQueries()
            .build()

        toDoList.addAll(db.toDoDao().loadAll())

        toDoAdapter = ToDoAdapter(toDoList, detailLauncher)
        binding.toDoList.apply {
            adapter = toDoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.floatingActionButton.setOnClickListener {
            val newToDo = ToDo(
                title = "To Do",
                reminderDateTime = ZonedDateTime.now()
            )
            val newId = db.toDoDao().insert(newToDo)
            toDoList.clear()
            toDoList.addAll(db.toDoDao().loadAll())
            toDoAdapter.notifyItemInserted(db.toDoDao().getRowCount() - 1)
            val intent = Intent(this, DetailActivity::class.java).putExtra(
                DetailActivity.EXTRA_TO_DO, db.toDoDao().loadById(newId)
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
                val deletedToDo: ToDo = toDoList[viewHolder.adapterPosition]
                db.toDoDao().delete(deletedToDo)
                toDoList.clear()
                toDoList.addAll(db.toDoDao().loadAll())
                val position = viewHolder.adapterPosition
                toDoAdapter.notifyItemRemoved(position)
                Snackbar.make(
                    binding.toDoList, "Deleted " + deletedToDo.title, Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    db.toDoDao().insert(deletedToDo)
                    toDoAdapter.toDoList.add(position, deletedToDo)
                    toDoAdapter.notifyItemInserted(position)
                }.show()
            }
        }).attachToRecyclerView(binding.toDoList)
    }
}