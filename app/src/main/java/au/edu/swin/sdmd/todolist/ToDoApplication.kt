package au.edu.swin.sdmd.todolist

import android.app.Application

class ToDoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ToDoRepository.initialize(this)
    }
}