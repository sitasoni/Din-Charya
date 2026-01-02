package com.ss.dincharya.Todo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodo(todo : TodoPojo)

    @Delete
    fun deleteTodo(todo: TodoPojo)

    @Update
    fun updateTodo(todo: TodoPojo)

    @Query("SELECT * FROM todo ORDER BY ID ASC")
    fun getAllTodo() : List<TodoPojo>

    @Query("SELECT * FROM todo")
    fun getTodos() : LiveData<List<TodoPojo>>
}