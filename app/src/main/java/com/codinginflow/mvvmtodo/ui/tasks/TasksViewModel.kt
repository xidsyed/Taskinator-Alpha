package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Identifies a androidx.lifecycle.ViewModel's constructor for injection.
 * Similar to javax.inject.Inject, a ViewModel containing a constructor annotated with
 * ViewModelInject will have its dependencies defined in the constructor parameters
 * injected by Dagger's Hilt.
 *
 * ViewModel must never hold references to Views. Memory Leak
 * So we use Reactive Data Sources like Flow
 *
 * LiveData is very similar to Flow, except it has the latest value, instead of the whole
 * stream of values.
 *
 * Traditionally use Flow all the way from room to Fragment  then LiveData from
 * */
class TasksViewModel @ViewModelInject constructor(
	private val taskDao: TaskDao
): ViewModel() {

	/**
	 * `searchQuery` : is a MutableStateFlow object
	 *
	 *  `MutableStateFLow`:
	 * Like Mutable LiveData, because It can  hold a single value, not like normal flow
	 * that holds stream of values but this is still a flow.
	 *
	 * *StateFlow is a state-holder observable flow that emits the current and new state
	 * updates to its collectors. The current state value can also be read through its value
	 * property. To update state and send it to the flow, assign a new value to the value
	 * property of the MutableStateFlow class.*

	 */
	val searchQueryFlow = MutableStateFlow("")

	/**
	 *
	 *
	 * `tasksFLow` : updated Flow Object that uses `searchQuery.flatMapLatest` to stay updated?
	 *
	 * flatMapLatest is a method of searchQuery (which is a MutableStateFlow Object), which
	 * monitors the searchQuery flow for changes if there is, it executes the `transform` function
	 * (lambda function we pass to it that uses `getTasks(searchQuery.value)` which returns a new flow
	 * form the database that matches the new `searchQuery` ) to return a new flow to
	 * `tasksFlow` Flow Object.
	 *
	 * `flatMapLatest` equivalent in `LiveData` is `switchMap`
	 *
	 * So in short: **Monitor Flow, If Changed, Get New Flow based on lambda function**
	 *
	 * *flatMapLatest : Returns a flow that switches to a new flow produced by `transform`
	 * (which is the lambda) function passed to it, every time
	 * the original flow emits a value. When the original flow emits a new value,
	 * the previous flow produced by transform block is cancelled.* */
	private val tasksFlow = searchQueryFlow.flatMapLatest {
		taskDao.getTasks(it)
	}

	/**`tasks` : LiveData object derived from `tasksFlow`*/
	val tasksLiveData = tasksFlow.asLiveData()


}