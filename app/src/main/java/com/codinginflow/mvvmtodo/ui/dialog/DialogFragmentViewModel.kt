package com.codinginflow.mvvmtodo.ui.dialog

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DialogFragmentViewModel @ViewModelInject constructor(
	@Assisted private val state : SavedStateHandle,
	@ApplicationScope private val applicationScope: CoroutineScope,
	private val taskDao: TaskDao
) : ViewModel() {

	/**
	 * Retrieve
	 * Dialog Title
	 * Dialog Message
	 * Negative Button
	 * Positive Button
	 * Action? First HardCode
	 * */

	val dialogTitle : String = state.get<String>("dialogTitle")!!   // non-nullable
	val dialogMessage : String = state.get<String>("dialogMessage")!!
	val negativeButton : String = state.get<String>("negativeButton")!!
	val positiveButton : String = state.get<String>("positiveButton")!!



	private val DialogEventsChannel = Channel<DialogEvents>()
	val DialogEventsFlow = DialogEventsChannel.receiveAsFlow()

	fun onConfirmClick () = applicationScope.launch{
		// delete completed tasks
		taskDao.deleteCompletedTasks()
		// send navigateback event to dialog
		DialogEventsChannel.send(DialogEvents.NavigateBack)
	}



	sealed class DialogEvents {
		object NavigateBack : DialogEvents()
	}


}
