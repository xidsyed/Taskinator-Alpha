package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

 /**
 Room will generate the implementation of the class and methods, so they need to be abstract
 Interface cant be used coz it needs to extend RoomDatabase class
 */
 @Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

	abstract fun taskDao(): TaskDao

	/**
	 * A RoomDatabse Callback that gets called when database is created.
	 *
	 * `.addCallback()` is called on Room.databaseBuilder before `.build()` gets called to
	 * add the `callback` to the `build` process.
	 *
	 * `Callback` gets called internally after `.build()` is called so IT CAN Access the db object
	 *
	 * Here Callback is used to use DAO to insert few dummy entries to the database.
	 * So that database is not empty when created
	 *
	 * */
	class Callback @Inject constructor(
		private val database: Provider<TaskDatabase>,	// Provides TaskDatabase instance lazily
		@ApplicationScope val applicationScope: CoroutineScope	// Provided by Dagger as well
	) : RoomDatabase.Callback() {
		// Executed only the first time Database is created.
		override fun onCreate(db: SupportSQLiteDatabase) {
			super.onCreate(db)

			// db operations
			val dao = database.get().taskDao()
			//dao.insert()  //ERROR: insert is a suspend fn, needs a coroutine
			//GlobalScope.launch{}	//NOPE Bad Practice - memory leak

			applicationScope.launch {
				dao.insert(Task("Wish X Æ A-Xii A Happy Birthday"))
				dao.insert(Task("Learn Fragment Lifecycles", important = false))
				dao.insert(Task("Complete Google Foobar", completed = true))
				dao.insert(Task("Find Out why ListAdapter Causes Stutter", completed = true))
				dao.insert(Task("Deploy Blog"))
				dao.insert(Task("Go through Stack Source"))
				dao.insert(Task("Close Github Issue", important = false))
				dao.insert(Task("Check Leetcode", completed = true))
				dao.insert(Task("Finish Basic Todo App", completed = true))
				dao.insert(Task("Find Out if Stuttering on other phones"))
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("What is Systrace"))
				dao.insert(Task("Where do the threads go?"))
				dao.insert(Task("What does the fox say", important = false))
				dao.insert(Task("Answer to the Universe?", completed = true))
				dao.insert(Task("More Dummy Tasks", completed = true))
				dao.insert(Task("Way too much time"))
				dao.insert(Task("Navigation Component is Awesome"))
				dao.insert(Task("Design View Can be Buggy", important = false))
				dao.insert(Task("This App has been optimised for dark mode", completed = false, important = true))
				dao.insert(Task("These are dummy tasks", completed = true))
				dao.insert(Task("Swipe Left to Delete Task", completed = false, important = true))
				dao.insert(Task("Mark Any Task as Important", completed = false, important = true))
				dao.insert(Task("You can Apply Filters from the Options Menu", completed = false, important = true))
				dao.insert(Task("Or you could delete all tasks", completed = false, important = true))
				dao.insert(Task("You could even search for new tasks", completed = false, important = true))
				dao.insert(Task("Do big animations in list stutter for you?", completed = false, important = true))
			}
		}
	}
}