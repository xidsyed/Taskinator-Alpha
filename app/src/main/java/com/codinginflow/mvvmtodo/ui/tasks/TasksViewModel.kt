package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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
	 * ## FLows
	 * `searchQueryFlow` : represents the search query entered by the user & is a MutableStateFlow
	 * object
	 *
	 * `hideCompletedFlow` : represents the hide completed tasks filter's state as a Boolean Flow
	 *
	 * `sortOrderFlow` : represents the sort order filter's state as a SortOrder Enum Flow
	 *
	 * ## What is MutableStateFlow
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

	//  MutableStateFlow search query Filters initialised with default args
	val sortOrderFlow = MutableStateFlow(SortOrder.BY_DATE)
	val hideCompletedFlow = MutableStateFlow(false)


	/**
	 *
	 * `tasksFLow` : updated Flow Object that uses `FLOW.flatMapLatest` to stay updated
	 *
	 * `combine` : Basically combines multiple flows, and returns a single flow. You get a
	 * transform lambda function to pass to  dictate how that happens. Here we combine the flows
	 * using Kotlin's wrapper function (Data Class) `Triple`, used to return multiple values
	 * from functions
	 *
	 * flatMapLatest is a method of `Flow` , which monitors the it flow for changes if there is,
	 * it executes the `transform` function (lambda function we pass to it that uses
	 * `getTasks()` method and passes updated search filters and query which returns a new flow
	 * from the room databse) to return a new flow to `tasksFlow` Flow Object.
	 *
	 * `flatMapLatest` equivalent in `LiveData` is `switchMap`
	 *
	 * So in short: **Monitor Flow, If Changed, Get New Flow based on lambda function**
	 *
	 * *flatMapLatest : Returns a flow that switches to a new flow produced by `transform`
	 * (which is the lambda) function passed to it, every time
	 * the original flow emits a value. When the original flow emits a new value,
	 * the previous flow produced by transform block is cancelled.* */
	private val tasksFlow = combine(
		searchQueryFlow,
		sortOrderFlow,
		hideCompletedFlow
	) { query, sortOrder, hideCompleted ->
		Triple(query, sortOrder, hideCompleted)
	}.flatMapLatest { (query, sortOrder, hideCompleted) ->  // KOTLIN DESTRUCTURING DECLARATION
		taskDao.getTasks(query, sortOrder, hideCompleted)
	}




	/**`tasks` : LiveData object derived from `tasksFlow`*/
	val tasksLiveData = tasksFlow.asLiveData()


}


enum class SortOrder {BY_NAME, BY_DATE}