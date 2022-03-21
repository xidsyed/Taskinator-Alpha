package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
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
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("GSOC Weekly Task Complete", important = true))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("App Previews for PlayStore", completed = true))
				dao.insert(Task("Deploy Update to Personal Blog"))
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("GSOC Weekly Task Complete", important = true))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("App Previews for PlayStore", completed = true))
				dao.insert(Task("Deploy Update to Personal Blog"))
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("GSOC Weekly Task Complete", important = true))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("App Previews for PlayStore", completed = true))
				dao.insert(Task("Deploy Update to Personal Blog"))
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("GSOC Weekly Task Complete", important = true))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("App Previews for PlayStore", completed = true))
				dao.insert(Task("Deploy Update to Personal Blog"))
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("GSOC Weekly Task Complete", important = true))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("App Previews for PlayStore", completed = true))
				dao.insert(Task("Deploy Update to Personal Blog"))
				dao.insert(Task("Build Widget Prototype for HN App"))
				dao.insert(Task("GSOC Weekly Task Complete", important = true))
				dao.insert(Task("Revise Leetcode Questions", completed = true))
				dao.insert(Task("App Previews for PlayStore", completed = true))
				dao.insert(Task("Deploy Update to Personal Blog"))
			}
		}
	}
}