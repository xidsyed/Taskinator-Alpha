package com.codinginflow.mvvmtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat


// Allow objects to be compared using dataclass
@Entity(tableName = "task_table")
@Parcelize
data class Task (
	val name: String,
	val important: Boolean = false,
	val completed: Boolean = false,
	val created: Long = System.currentTimeMillis(),
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0
	) : Parcelable {
	/**
	* Immutable properties are used so that app can compare easily
	 * if we use mutable object then app may not realise that property
	 * has changed, that's how comparison works. Modifying simply involves
	 * creating a new object
	 *
	 * We will be using the diffUtil Class required by ListAdapter of our Recycler View,
	 * which requires Lists being used to be immutable*/
	val createdDateFormatted : String
		get() = DateFormat.getDateTimeInstance().format(created)
}