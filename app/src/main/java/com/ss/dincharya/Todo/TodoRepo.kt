package com.ss.dincharya.Todo

class TodoRepo(private val dao: TodoDao) {

    fun insert(todoPojo: TodoPojo){
        dao.insertTodo(todoPojo)
    }
    fun update(todoPojo: TodoPojo){
        dao.updateTodo(todoPojo)
    }
    fun delete(todoPojo: TodoPojo){
        dao.deleteTodo(todoPojo)
    }
    fun getAllTodo() : List<TodoPojo>{
        return dao.getAllTodo()
    }
}