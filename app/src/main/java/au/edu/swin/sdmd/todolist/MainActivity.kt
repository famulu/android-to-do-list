package au.edu.swin.sdmd.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.swin.sdmd.todolist.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toDoList = List(100) {
            ToDo(it.toString(), LocalDate.now(), LocalTime.now())
        }

        binding.toDoList.apply {
            adapter = ToDoAdapter(toDoList)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
}