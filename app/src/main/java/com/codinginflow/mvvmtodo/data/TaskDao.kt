package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Objects are the main classes where you define your database interactions.
 * They can include a variety of query methods.The class marked with @Dao should either
 * be an interface or an abstract class. At compile time, Room will generate an implementation
 * of this class when it is referenced by a Database.
 * */
@Dao
interface TaskDao {

	/**
	 * `getTasks`: Accepts a searchQuery String and returns a Flow<List<Task>>
	 *
	 * Flow here don't need suspend modifier because Flow can only be used or collected
	 * in a coroutine. All suspension and threading happens within the flow itself.
	 * LiveData is pretty similar*/
	@Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ")
	fun getTasks(searchQuery: String) : Flow<List<Task>>

	// Replace task if conflict
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert (task : Task)

	@Update
	suspend fun update (task: Task)

	@Delete
	suspend fun delete (task: Task)

}