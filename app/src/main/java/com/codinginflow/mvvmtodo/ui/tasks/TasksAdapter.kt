package com.codinginflow.mvvmtodo.ui.tasks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.TaskItemBinding
import kotlin.system.measureTimeMillis

/**
 * We use ListAdapter, subclass of RecyclerViewAdapter, because we are using an immutable list.
 * We pass the viewmodel a completely new list each time theres a small change.
 *
 * ListAdapter finds the differences in the background thread, and reorganises the list
 * */
class TasksAdapter() :
	ListAdapter<Task, TasksAdapter.TaskItemViewHolder>(DiffCallback()) {

	inner class TaskItemViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root){
		// to be called onBindViewHolder
		fun bindTaskToView(task:Task) {
			binding.apply {
				taskNameTextView.text = task.name
				checkBox.isChecked = task.completed
				labelPriority.isVisible = task.important
				taskNameTextView.paint.isStrikeThruText = task.completed
			}
		}
	}

	// ViewHolder gets created for each item view very first time.
	// Function inflates the Item Layout in the ViewHolder
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
		val binding = TaskItemBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)
		return TaskItemViewHolder(binding)
	}

	// Binds the ViewHolder to the data
	override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
		 /**
		  * getItem from DiffUtil Class
		  *
		  * DiffUtil gets the list when the ListAdapter's submitList function gets called
		  * by LiveData's Observe method's lambda function*/

		holder.bindTaskToView(getItem(position))


	}

	/**
	 * DiffUtil is a utility class that calculates the difference between two lists and outputs a list of update operations that converts the first list into the second one.
	 * It can be used to calculate updates for a RecyclerView Adapter.
	 *
	 * DiffUtil uses Eugene Myer's Difference Algorithm, to find the minimum possible operations to
	 * convert one list into another.
	 *
	 * DiffUtil requires both lists to be immutable
	 *
	 * DiffUtil Knows how to handle lists, and dispatch appropriate animations and update methods.
	 */
	class DiffCallback() : DiffUtil.ItemCallback<Task>() {

		/**
		 * Functions logic need to be implemented. They inform the ListAdapter whether
		 * - Items are the same exact item (Uniquely identifiable via ID), with positions changed?
		 * - New Item with Same Content?
		 * - keeps a list inplace
		 * */
		override fun areItemsTheSame(oldItem: Task, newItem: Task)
			: Boolean = oldItem.id == newItem.id


		override fun areContentsTheSame(oldItem: Task, newItem: Task)
			: Boolean = oldItem == newItem // uses toString to if same hash. just data class things
	}


}