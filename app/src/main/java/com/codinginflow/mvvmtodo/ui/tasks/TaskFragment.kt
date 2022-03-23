package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.util.SwipeToDeleteCallback
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.getSnackBar
import com.codinginflow.mvvmtodo.util.queryTextChangeListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


/**
 * `@AndroidEntryPoint` Marks an Android component class to be setup for injection with the standard Hilt Dagger Android
 * components. Currently, this supports activities, fragments, views, services, and broadcast
 * receivers.
 *
 * This annotation will generate a base class that the annotated class should extend, either
 * directly or via the Hilt Gradle Plugin. This base class will take care of injecting members
 * into the Android class as well as handling instantiating the proper Hilt components at the
 * right point in the lifecycle. The name of the base class will be "Hilt_ ".
 * */
@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.onClickListener {
	/**
	 *  Returns a property delegate to access a viewModel by default Scoped to this Fragment. Which
	 *  Allows it to be lifecycle aware etc.
	 */
	private val viewModel: TasksViewModel by viewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val binding = FragmentTasksBinding.bind(view)

		val tasksAdapter = TasksAdapter(this)
		binding.apply {
 			tasksRecyclerView.apply {
				adapter = tasksAdapter
				layoutManager = LinearLayoutManager(requireContext())
				setHasFixedSize(true)   // Optimise RV
			}
			/**
			 * `recyclerview.widget.ItemTouchHelper` :

			 * utility class to add swipe to dismiss and drag & drop support to RecyclerView.
			 * It works with a `RecyclerView` and a `Callback` class, which configures what type of
			 * interactions are enabled and also receives events when user performs these actions.
			 *
			 * Override Callbacks depending on functionality you want.
			 */
			val swipeHandler = object: SwipeToDeleteCallback(requireContext()){
				override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
					val task = tasksAdapter.currentList[viewHolder.adapterPosition]
					viewModel.onItemSwiped(task)

				}
			}
			ItemTouchHelper(swipeHandler).attachToRecyclerView(tasksRecyclerView)

			// FAB
			fabAddTask.setOnClickListener {
				viewModel.onAddNewTaskClick()
			}
		}


		// activate the options menu
		setHasOptionsMenu(true)

		setFragmentResultListener("add_edit_request") { _, bundle ->
			val result = bundle.getInt("add_edit_result")
			viewModel.addEditResult(result)
		}

		 /**
		  * OBSERVE livedata accepts a lambda/observer which gets passed the list by the ViewModel
		  * observer simply calls submitlist on the list given by livedata
		 */
		viewModel.tasksLiveData.observe(viewLifecycleOwner){
			tasksAdapter.submitList(it)
		}


		 /**
		  * OBSERVE taskEventsFlow, this happens inside a coroutine scope,
		  * since we are collecting a flow
		  *
		  * `launchWhenStarted` restricts scope onstart->onstop
		  *
		  * we check event type, and create appropriate snackbar. If snackbar action is triggered
		  * we pass a viewmodel function to handle it.
		  * */
		viewLifecycleOwner.lifecycleScope.launchWhenStarted{
			viewModel.taskEventsFlow.collect { event ->
				when (event) {
					is TasksViewModel.TasksEvent.ShowUndoDeleteMessage -> {
						getSnackBar(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
							.setAction("Undo") {
								viewModel.undoDeleteClick(event.task)
							}.show()
					}
					is TasksViewModel.TasksEvent.navigateToAddTask -> {
						// safeArgs give compile time safety
						val action = TaskFragmentDirections
							.actionTaskFragmentToAddEditTaskFragment(null, "New Task")
						findNavController().navigate(action)

					}
					is TasksViewModel.TasksEvent.navigateToEditTask -> {
						val action = TaskFragmentDirections
							.actionTaskFragmentToAddEditTaskFragment(event.task, "Edit Task")
						findNavController().navigate(action)
					}
					is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
						getSnackBar(requireView(), event.message, Snackbar.LENGTH_LONG).show()
					}
					TasksViewModel.TasksEvent.showDeleteAllCompletedConfitmation -> {
						val action = TaskFragmentDirections.actionGlobalDialogFragment(
							"Confirm Deletion",
							"Sure you want to delete all completed tasks?",
							"Cancel",
							"Yes"
						)
						findNavController().navigate(action)
					}
				}.exhaustive

			}
		}

	}


	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu_fragment_tasks, menu)
		val hideCompletedItem = menu.findItem(R.id.action_hide_complted_tesks)
		val searchItem = menu.findItem(R.id.action_search)
		val searchView = searchItem.actionView as SearchView

		/**
		 *  `queryTextChangeListener`: Custom Extension Function that sets onQueryTextListener for the
		 *  search view
		 *
		 *  We update the viewmodel's searchQueryFlow.value inside the trailing lambda to reflect in the
		 *  viewmodel
		 */
		searchView.queryTextChangeListener {
			/**
			 * every time the searchQueryFlow's value is updated a flatMapLatest updates the
			 * `tasksFlow` property in ViewModel which updates the livedata consumed by our
			 * tasksAdapter in `onCreatedView` viewmodel's livedata*/
			viewModel.searchQueryFlow.value = it
		}

		// Init HideCompleted isChecked
		viewLifecycleOwner.lifecycleScope.launch{
			hideCompletedItem.isChecked = viewModel.getHideCompleted()
		}

	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_sort_by_name -> {
				viewModel.onSortOrderSelected(SortOrder.BY_NAME)
				true
			}
			R.id.action_sort_by_date_created -> {
				viewModel.onSortOrderSelected(SortOrder.BY_DATE)
				true
			}
			R.id.action_delete_all_completed -> {
				viewModel.onDeleteAllCompletedSelected()
				true
			}
			R.id.action_hide_complted_tesks -> {
				val isChecked = item.isChecked
				// update the checkbox
				item.isChecked = !isChecked
				// update viewmodel filter
				viewModel.onHideCompletedSelected(!isChecked)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
		return super.onOptionsItemSelected(item)
	}


	// Implement TaskAdapter.onClickListener

	override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
		viewModel.onCheckBoxClick(task, isChecked)
	}

	override fun onItemClick(task: Task) {
		viewModel.onItemClick(task)
	}

}