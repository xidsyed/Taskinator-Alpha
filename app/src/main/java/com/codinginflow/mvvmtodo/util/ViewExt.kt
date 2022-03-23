package com.codinginflow.mvvmtodo.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

/**
 * Pass a lambda function to our SearchView Extension function
 * which will be called in our setOnQueryTextListener, onQueryTextChange with the
 * newText Entered as the parameter*/
inline fun SearchView.queryTextChangeListener(crossinline listener: (String) -> Unit) {
	this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
		// Don't care, cuz we dont submit.
		override fun onQueryTextSubmit(query: String?): Boolean {
			return true
		}
		// Filter list in real time.
		override fun onQueryTextChange(newText: String?): Boolean {
			listener(newText.orEmpty())
			return true     // this is why we need crossinline
		}

	})
}


/**
 * ItemTouchHelper SimpleCallback Class that is responsible for swipe gestures to recycler view
 * objects.
 *
 * Here we utilise the onChildDraw method provided to draw a red background and a trash icon
 * */
abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(
	0,
	ItemTouchHelper.LEFT
) {

	private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24)!!
	private val intrinsicWidth = deleteIcon.intrinsicWidth
	private val intrinsicHeight = deleteIcon.intrinsicHeight
	private val background = ColorDrawable()
	private val backgroundColor = Color.parseColor("#f44336")
	private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


	override fun onMove(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		target: RecyclerView.ViewHolder
	): Boolean {
		return false
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
		val itemView = viewHolder.itemView
		val itemHeight = itemView.bottom - itemView.top
		val isCanceled = dX == 0f && !isCurrentlyActive

		if (isCanceled) {
			clearCanvas(
				c,
				itemView.right + dX,
				itemView.top.toFloat(),
				itemView.right.toFloat(),
				itemView.bottom.toFloat()
			)
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
			return
		}

		// Draw the red delete background
		background.color = backgroundColor
		background.setBounds(
			itemView.right + dX.toInt(),
			itemView.top,
			itemView.right,
			itemView.bottom
		)
		background.draw(c)

		// Calculate position of delete icon
		val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
		val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
		val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
		val deleteIconRight = itemView.right - deleteIconMargin
		val deleteIconBottom = deleteIconTop + intrinsicHeight

		// Draw the delete icon
		deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
		deleteIcon.draw(c)

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
	}


	private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
		c?.drawRect(left, top, right, bottom, clearPaint)
	}
}


inline fun getSnackBar(view : View, message: String, length : Int) : Snackbar{
	val snackbar = Snackbar.make(view, message, length)
	snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
	return snackbar
}