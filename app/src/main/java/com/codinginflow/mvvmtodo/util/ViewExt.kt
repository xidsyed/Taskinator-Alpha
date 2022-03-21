package com.codinginflow.mvvmtodo.util

import androidx.appcompat.widget.SearchView

/**
 * Pass a lambda function to our SearchView Extension function
 * which will be called in our setOnQueryTextListener, onQueryTextChange with the
 * newText Entered as the parameter*/
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
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