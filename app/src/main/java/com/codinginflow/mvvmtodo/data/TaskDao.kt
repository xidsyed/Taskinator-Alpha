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
	 * `getTasks`: Accepts multiple query parameters from the user. namely a search query,
	 * the states of the filters sortOrder and hidCompleted
	 *
	 * No @Query Annoation for this function, since it is not a Query method, but a wrapper for
	 * query methods.
	 *
	 * Wrapper was needed, since we could not send a dynamic column value (created / name) as a
	 * sqlite query since that would not be compile time safe if the name changed.*/

	fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean) : Flow<List<Task>> =
		when (sortOrder) {
			SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
			SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
		}

	/** Query methods don't need suspend modifier because Flow can only be used or collected
	 * in a coroutine and All suspension and threading happens within the flow itself. without
	 * having to create a coroutine scope first. LiveData is pretty similar to flows*/
	@Query(
		"SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) " +
				"AND name LIKE '%' || :searchQuery || '%' " +
				"ORDER BY important DESC, created"
	)
	fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean) : Flow<List<Task>>

	@Query(
		"SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) " +
				"AND name LIKE '%' || :searchQuery || '%' " +
				"ORDER BY important DESC, name"
	)
	fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean) : Flow<List<Task>>

	// Replace task if conflict
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert (task : Task)

	@Update
	suspend fun update (task: Task)

	@Delete
	suspend fun delete (task: Task)

	@Query("DELETE FROM task_table WHERE completed = 1")
	suspend fun deleteCompletedTasks()

}