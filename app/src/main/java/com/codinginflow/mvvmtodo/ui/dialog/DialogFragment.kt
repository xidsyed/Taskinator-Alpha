package com.codinginflow.mvvmtodo.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.ui.dialog.DialogFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint

class DialogFragment  : DialogFragment(){

	// init viewmodel
	private val viewModel : DialogFragmentViewModel by viewModels()



	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		viewLifecycleOwner.lifecycleScope.launchWhenStarted {
			viewModel.DialogEventsFlow.collect { event ->
				when(event) {
					is DialogFragmentViewModel.DialogEvents.NavigateBack ->
						findNavController().popBackStack()
				}
			}
		}
	}



	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
		AlertDialog.Builder(requireContext())
			.setTitle(viewModel.dialogTitle)
			.setMessage(viewModel.dialogMessage)
			.setNegativeButton(viewModel.negativeButton, null)
			.setPositiveButton(viewModel.positiveButton) { _, _ ->
				viewModel.onConfirmClick()
			}
			.create()




}