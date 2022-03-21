package com.codinginflow.mvvmtodo.di

import android.app.Application
import android.app.SharedElementCallback
import androidx.room.Room
import com.codinginflow.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 *  We could also use a class, but object makes the generated code
 * more efficient. Check Documentation
 *
 * Here we tell dagger how to create the dependencies we need. How to createa
 * a task database and a task DAO
 *
 * Called 'AppModule' since the scope reaches the whole app
 * */

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

	/** Annotates methods of a module to create a provider method binding
	 *
	 * The method's return type is bound to its returned value.
	 *
	 * The component implementation will pass dependencies to the method as parameters.*/
	@Provides
	@Singleton
	fun provideDatabase(
		app: Application,
		callback: TaskDatabase.Callback
	) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
			.fallbackToDestructiveMigration()	// destroy+recreate db if update schema wo migration
			.addCallback(callback)
			.build()							// creates and init db with callback

	/**
	 * When we call dagger asking for a taskDao, it knows it has a function with a taskDao return type
	 *
	 * provideTaskDao accepts a TaskDatabase object. Dagger Module constrvutors do their own
	 * dependency injections! (Dagger will call itself?) and it will find
	 * that it has a provideDatabase method with a TaskDatabase Return Type. So it will create a
	 * TaskDatabase object if required.
	 *
	 * But since provideDatabase is a singleton. So it has only one instance available to the whole
	 * app (because of the @Installin which sets the appComponent scope thingy).
	 * So it will simply access the TaskDatabase object and inject that dependency, into the
	 * dependency injector method provideTaskDao which will inject the dependency wherever
	 * it is called
	 *
	 * We don't need a '@Singleton' for provideTaskDao since it is a Singleton automatically,
	 * By Room's own logic
	 * */
	@Provides
	fun provideTaskDao(db: TaskDatabase) = db.taskDao()

	/**
	 * provide a coroutine scope
	 * Singleton, since we need only one static, throughout the app
	 *
	 * Coroutines Cancel when a child fails, So we pass it a SupervisorJob.
	 * Children of a supervisor job can fail independently of each other.
	 * */
	@ApplicationScope
	@Singleton
	@Provides
	fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

/**
 * Allows ApplicationScope to be a Dagger Annotation,
 * Now you can specify what Scope Class you want, in case you wanted some other
 * coroutines scope, simply create a qualifier for it too, and inject it wherever
 * you want*/
@Retention(AnnotationRetention.RUNTIME)	// Annotation stored in binary & visible for reflection. needed for Qualifier annotation
@Qualifier								// Create your own Qualifier Annotations!!
annotation class ApplicationScope