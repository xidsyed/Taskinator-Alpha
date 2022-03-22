package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "PreferencesRepository"

enum class SortOrder {BY_NAME, BY_DATE}

data class FilterPreferences (val sortOrder: SortOrder, val hideCompleted:Boolean)

/**
 * Singleton - cuz we need one for the whole app
 *
 * Inject - dagger will need to inject the constructor in places
 *
 * Application Context - It can be accessed from the entire applications lifecycle
 *
 * Preferences Repository is our custom abstraction class where we handle all methods and call to
 * Jetpack DataStore
 * */

@Singleton
class PreferencesRepository @Inject constructor(@ApplicationContext context: Context){
	/**
	* **`DataStore`** provides a safe and durable way to store small amounts of data, such as
	 * preferences and application state. No partial updates, for that, consider the Room API (SQLite).
	 * DataStore provides ACID guarantees. It is thread-safe, and non-blocking. In particular,
	 * it addresses many shortcomings of the SharedPreferences API
	 *
	 * **`Preferences`** and `MutablePreferences` are a lot like a generic Map and MutableMap keyed by the
	 * Preferences.Key class. These are intended for use with DataStore.
	 * Construct a DataStore instance using `PreferenceDataStoreFactory.createDataStore()`.
	 *
	 * **`createDataStore()`** Creates an instance of SingleProcessDataStore of Preferences. The user is
	 * responsible for ensuring that there is never more than one instance of SingleProcessDataStore acting
	 * on a file at a time. It returns a `Datastore<Preferences>`
	 *
	 * ***createDataStore params***
	 *
	 * `name`:
	 * The name of the preferences. The preferences will be stored in a file obtained
	 * by calling: File(context.filesDir, "datastore/" + name + ".preferences_pb")
	 *
	 * `corruption handler`: The corruptionHandler is invoked if DataStore encounters a
	 * CorruptionException when attempting to read data. CorruptionExceptions are thrown by
	 * serializers when data can not be de-serialized.
	 *
	 * `migrations`: migrations - are run before any access to data can occur. Each producer and
	 * migration may be run more than once whether or not it already succeeded
	 * (potentially because another migration failed or a write to disk failed.)
	 *
	 * `scope` - The coroutine scope in which IO operations and transform functions will execute.
	 *
	* */

	private val datastore = context.createDataStore("user_preferences")

	/** `datastore.data` provides efficient and cached access to persisted preferences data
	 * using Flow. returns a `Flow<Preferences>`
	 * */
	val preferencesFlow = datastore.data
		.catch { exception ->
			if(exception is IOException) {
				// display error and emit empty preference
				Log.e(TAG, "error reading preferences ")
				emit(emptyPreferences())
			} else throw exception
		}
		.map { preferences ->
			/**
			 * We intend to use map function from `datastore.data` and transform the
			 * Flow<Preferences> to Flow<FilterPreferences> for easier consumption by our VIEWMODEL
			 *
			 * `valueOf()` is an enum function, that returns enum, from a string value
			 *
			 * `.name` returns the name of the enum as string
			 *
			 * We try to access the value stored in the `preferences` map, if not available,
			 * we return the default value as BY_DATE and false
			 *
			 * */
			val sortOrder =  SortOrder.valueOf(
				preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
			)
			val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: true

			/**
			 * return `sortOrder` and `hideCompleted` in a dataclass, to `preferencesFlow`\
			 * for consumption by ViewModel
			 * */
			FilterPreferences(sortOrder, hideCompleted)
		}


	/**
	 * Suspend Functions to modify preferences. Functions have to be suspend functions because
	 * here we are not modifying a flow (which takes care of suspension internally).
	 *
	 * Instead `.edit()` is called on `DataStore<Preferences>`directly
	 * */
	suspend fun updateSortOrder(sortOrder: SortOrder){
		datastore.edit { mutablePreferences ->
			mutablePreferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
		}
	}

	suspend fun updateHideCompleted(hideCompleted: Boolean) {
		datastore.edit { mutablePreferences ->
			mutablePreferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
		}
	}

	/**
	 * We create a custom object `PreferencesKeys`
	 * (we dont need to, but better readability and organisation) and store our `Preference.Key`
	 * objects which are like keys in a MutableMap, used to access the elements inside it.
	 *
	 * `preferencesKey<T>()` returns: the `Preference.Key` object for your preference
	 * You should not have multiple keys with the same name (for use with the same Preferences)
	 * */
	private object PreferencesKeys {
		val SORT_ORDER = preferencesKey<String>("sort_order")
		val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
	}





}
