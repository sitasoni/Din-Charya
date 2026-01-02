package com.ss.dincharya.DbHandler

import androidx.lifecycle.ViewModel
import com.ss.dincharya.Todo.TodoPojo
import com.ss.dincharya.Todo.TodoRepo
import java.util.concurrent.Executors

class TodoViewModel() : ViewModel() {
    private lateinit var repo: TodoRepo
    private val executor = Executors.newSingleThreadExecutor()

    fun setRepository(repo: TodoRepo) {
        this.repo = repo
    }

    fun todoInsert(todoPojo: TodoPojo){
        executor.execute{
            repo.insert(todoPojo)
        }
    }
    fun todoUpdate(todoPojo: TodoPojo){
        executor.execute{
            repo.update(todoPojo)
        }
    }
    fun todoDelete(todoPojo: TodoPojo){
        executor.execute{
            repo.delete(todoPojo)
        }
    }
    fun getAllTodos(callback : (List<TodoPojo>) -> Unit){
        executor.execute{
            callback(repo.getAllTodo())
        }
    }
}