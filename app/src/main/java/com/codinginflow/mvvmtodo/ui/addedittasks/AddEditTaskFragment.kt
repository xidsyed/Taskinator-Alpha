package com.codinginflow.mvvmtodo.ui.addedittasks

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.AddEditTaskFragmentBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.getSnackBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.add_edit_task_fragment){
	private val viewModel: AddEditTaskViewModel by viewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val binding = AddEditTaskFragmentBinding.bind(view)

		binding.apply {
			editTask.setText(viewModel.taskName)
			importantCheckbox.isChecked = viewModel.taskImportance
			//importantCheckbox.jumpDrawablesToCurrentState()     //skips animating checkbox
			dateCreatedTextView.isVisible = (viewModel.task != null)   //no date created for new task
			dateTextView.isVisible = (viewModel.task != null)   //no date created for new task
			dateIcon.isVisible = (viewModel.task != null)   //no date created icon for new task
			dateTextView.text = "${viewModel.task?.createdDateFormatted}"

			editTask.addTextChangedListener {
				viewModel.taskName = it.toString()
			}

			importantCheckbox.setOnCheckedChangeListener { _, isChecked ->
				viewModel.taskImportance = isChecked
			}

			fabSaveTask.setOnClickListener {
				viewModel.onSaveClick()
			}

		}
		viewLifecycleOwner.lifecycleScope.launchWhenStarted {
			viewModel.aeTaskEventsFlow.collect { event ->
				when (event) {
					is AddEditTaskViewModel.AETaskEvents.NavigateBackWithResult -> {
						binding.editTask.clearFocus()
						setFragmentResult(
							"add_edit_request",
							bundleOf("add_edit_result" to event.result)
						)
						findNavController().popBackStack()
					}
					is AddEditTaskViewModel.AETaskEvents.ShowInvalidInputMessage -> {

						val snackbar = getSnackBar(requireView(), event.message, Snackbar.LENGTH_SHORT)
						snackbar.show()
					}
				}.exhaustive
			}
		}



	}

}