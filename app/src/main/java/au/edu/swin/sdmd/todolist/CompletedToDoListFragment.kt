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
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.swin.sdmd.todolist.databinding.FragmentCompletedToDoListBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FragmentCompletedToDoList : Fragment() {
    private var _binding: FragmentCompletedToDoListBinding? = null
    val binding
        get() = checkNotNull(_binding) {
            "binding cannot be accessed. check if view is visible"
        }
    private val completedToDoListViewModel: CompletedToDoListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedToDoListBinding.inflate(inflater, container, false)
        binding.completedToDoListRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                completedToDoListViewModel.toDos.collect { toDos ->
                    binding.completedToDoListRecyclerView.adapter = ToDoAdapter(toDos) {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}