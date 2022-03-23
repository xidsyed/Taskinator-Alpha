package com.codinginflow.mvvmtodo.util

// turn a statement into an expression
val <T> T.exhaustive  : T
	get() = this