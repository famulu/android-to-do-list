package au.edu.swin.sdmd.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.swin.sdmd.todolist.databinding.FragmentToDoListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class ToDoListFragment : Fragment() {
    private var _binding: FragmentToDoListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "binding is null. Check if view is visible"
        }
    private val toDoListViewModel: ToDoListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToDoListBinding.inflate(inflater, container, false)
        binding.toDoRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                toDoListViewModel.toDos.collect { toDos ->
                    binding.toDoRecyclerView.adapter = ToDoAdapter(toDos) { toDoId ->
                        findNavController().navigate(
                            ToDoListFragmentDirections.showTodoDetail(
                                toDoId
                            )
                        )
                    }
                }
            }
        }

        binding.floatingActionButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val newToDo = ToDo(
                    title = "",
                    reminderDateTime = ZonedDateTime.now().plusHours(1).withMinute(0).withSecond(0)
                        .withNano(0)
                )
                val newToDoId = toDoListViewModel.insertToDo(newToDo)
                findNavController().navigate(ToDoListFragmentDirections.showTodoDetail(newToDoId))
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
                val id = viewHolder.itemId
                viewLifecycleOwner.lifecycleScope.launch {
                    val deletedToDo = toDoListViewModel.loadById(id)
                    toDoListViewModel.delete(deletedToDo)
                    MainActivity.cancelNotification(deletedToDo, requireContext())
                    Snackbar.make(
                        binding.root, "Deleted " + deletedToDo.title, Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        lifecycleScope.launch {
                            toDoListViewModel.insertToDo(deletedToDo)
                        }
                        MainActivity.scheduleNotification(
                            deletedToDo,
                            requireContext(),
                            binding.root
                        )
                    }.show()
                }
            }
        }).attachToRecyclerView(binding.toDoRecyclerView)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}