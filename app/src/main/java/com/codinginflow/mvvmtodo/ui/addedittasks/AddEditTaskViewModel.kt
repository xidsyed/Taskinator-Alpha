package com.codinginflow.mvvmtodo.ui.addedittasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
	@Assisted private val state : SavedStateHandle,
	private val taskDao: TaskDao
) : ViewModel()
{

	// try and get a task from savedstatehandle
	val task = state.get<Task>("task")

	/**
	 * Order of retrieval
	 * 1. SavedStatehandler taskName (in case of processdeath)
	 * 2. SavedStateHandler task (in case of editask)
	 * 3. empty string (fallback / in case of add task)
	 *
	 * setter function updates savedstatehandler. so everytime, taskname gets assingeed
	 * it also gets saved to savedstatehandler
	 * */



	var taskName : String = state.get<String>("taskName") ?: task?.name ?: ""
		set(value) {
			field = value
			state.set("taskName", value)
		}
	var taskImportance : Boolean = state.get<Boolean>("taskImportance") ?: task?.important ?: false
		set(value) {
			field = value
			state.set("taskImportance", value)
		}

	fun onSaveClick() {
		if(taskName.isBlank()) {
			// Show invalid task name
			showErrorMessage("Task Name Cannot Be Empty")
			return
		}

		if (task != null) {
			val updatedTask = task.copy(name = taskName, important = taskImportance)
			updateTaskAndNavigateBack(updatedTask)
			// navigate back
		} else {
			// create new task
			val newTask = Task(name = taskName, completed = false, important = taskImportance)
			createTaskAndNavigateBack(newTask)
		}
	}


	private val aeTaskEventsChannel = Channel<AETaskEvents>()
	val aeTaskEventsFlow = aeTaskEventsChannel.receiveAsFlow()


	fun showErrorMessage(errMessage : String) = viewModelScope.launch {
		aeTaskEventsChannel.send(AETaskEvents.ShowInvalidInputMessage(errMessage))
	}

	fun updateTaskAndNavigateBack(task: Task) = viewModelScope.launch {
		taskDao.update(task)
		// navigate back
		aeTaskEventsChannel.send(AETaskEvents.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
	}

	fun createTaskAndNavigateBack(task: Task) = viewModelScope.launch{
		taskDao.insert(task)
		// navigate back
		aeTaskEventsChannel.send(AETaskEvents.NavigateBackWithResult(ADD_TASK_RESULT_OK))
	}

	// AddEditTaskEvents Class
	sealed class AETaskEvents {
		data class ShowInvalidInputMessage (val message: String) :  AETaskEvents()
		data class NavigateBackWithResult (val result : Int) : AETaskEvents()
	}


}