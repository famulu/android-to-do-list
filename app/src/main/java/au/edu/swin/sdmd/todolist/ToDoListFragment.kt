package au.edu.swin.sdmd.todolist

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
                    binding.toDoRecyclerView.adapter =
                        ToDoAdapter(toDos) { toDoId ->
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
                findNavController().navigate(ToDoListFragmentDirections.showTodoDetail())
            }
        }

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = viewHolder.itemId
                if (direction == ItemTouchHelper.LEFT) {
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
                                deletedToDo, requireContext(), binding.root
                            )
                        }.show()
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val toDo = toDoListViewModel.loadById(id)
                        toDoListViewModel.update(toDo.copy(isCompleted = true))
                        MainActivity.cancelNotification(toDo, requireContext())
                        Snackbar.make(
                            binding.root, "Completed " + toDo.title, Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            lifecycleScope.launch {
                                toDoListViewModel.update(toDo)
                            }
                            MainActivity.scheduleNotification(
                                toDo, requireContext(), binding.root
                            )
                        }.show()
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val paint = Paint()
                    val icon: Bitmap?

                    if (dX > 0) {
                        icon = requireContext().getDrawable(R.drawable.ic_check)?.toBitmap()
                        if (icon != null) {
                            paint.color = Color.parseColor("#388E3C")
                            c.drawRect(
                                viewHolder.itemView.left.toFloat(),
                                viewHolder.itemView.top.toFloat(),
                                viewHolder.itemView.left.toFloat() + dX,
                                viewHolder.itemView.bottom.toFloat(),
                                paint
                            )
                            c.drawBitmap(
                                icon,
                                viewHolder.itemView.left + 16.0f,
                                viewHolder.itemView.top.toFloat() + (viewHolder.itemView.bottom.toFloat() - viewHolder.itemView.top.toFloat() - icon.height.toFloat()) / 2,
                                paint
                            )
                        }
                    } else {
                        icon = requireContext().getDrawable(R.drawable.ic_delete)?.toBitmap()
                        if (icon != null) {
                            paint.color = Color.parseColor("#D32F2F")
                            c.drawRect(
                                viewHolder.itemView.right.toFloat() + dX,
                                viewHolder.itemView.top.toFloat(),
                                viewHolder.itemView.right.toFloat(),
                                viewHolder.itemView.bottom.toFloat(),
                                paint
                            )

                            c.drawBitmap(
                                icon,
                                viewHolder.itemView.right.toFloat() - icon.width,
                                viewHolder.itemView.top.toFloat() + (viewHolder.itemView.bottom.toFloat() - viewHolder.itemView.top.toFloat() - icon.height.toFloat()) / 2,
                                paint
                            )
                        }
                    }
                    viewHolder.itemView.translationX = dX
                }
                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                )


            }
        }).attachToRecyclerView(binding.toDoRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}