package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.measureTimeMillis


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
class TaskFragment : Fragment(R.layout.fragment_tasks) {
	/**
	 *  Returns a property delegate to access a viewModel by default Scoped to this Fragment. Which
	 *  Allows it to be lifecycle aware etc.
	 */
	private val viewModel: TasksViewModel by viewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// init binding
		val binding = FragmentTasksBinding.bind(view)


		// attach adapter and layout manager to RV
		val tasksAdapter = TasksAdapter()
		binding.tasksRecyclerView.apply {
			adapter = tasksAdapter
			layoutManager = LinearLayoutManager(requireContext())
			setHasFixedSize(true)   // Optimise RV
		}

		// observe livedata accepts a lambda/observer which gets passed the list by the ViewModel
		// observer simply calls submitlist on the list given by livedata
		viewModel.tasksLiveData.observe(viewLifecycleOwner){
			tasksAdapter.submitList(it)

		}
		// activate the options menu
		setHasOptionsMenu(true)
	}
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu_fragment_tasks, menu)
		val searchItem = menu.findItem(R.id.action_search)
		val searchView = searchItem.actionView as SearchView



		/**
		 *  `onQueryTextChanged`: Custom Extension Function that sets onQueryTextListener for the
		 *  search view
		 *
		 *  We update the viewmodel's searchQueryFlow.value inside the trailing lambda to reflect in the
		 *  viewmodel
		 */
		searchView.onQueryTextChanged {
			/**
			 * every time the searchQueryFlow's value is updated a flatMapLatest updates the
			 * `tasksFlow` property which updates the livedata consumed by our tasksAdapter in
			 * `onCreatedView`viewmodel's livedata*/

			viewModel.searchQueryFlow.value = it
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_sort_by_name -> {
				viewModel.sortOrderFlow.value = SortOrder.BY_NAME
				true
			}
			R.id.action_sort_by_date_created -> {
				viewModel.sortOrderFlow.value = SortOrder.BY_DATE
				true
			}
			R.id.action_delete_all_completed -> {

				true
			}
			R.id.action_hide_complted_tesks -> {
				val isChecked = item.isChecked
				// update the checkbox
				item.isChecked = !isChecked
				// update viewmodel filter
				viewModel.hideCompletedFlow.value = !isChecked

				true
			}
			else -> super.onOptionsItemSelected(item)
		}
		return super.onOptionsItemSelected(item)
	}

}