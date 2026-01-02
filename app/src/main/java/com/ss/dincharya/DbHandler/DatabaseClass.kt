package com.ss.dincharya.DbHandler

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ss.dincharya.Todo.TodoDao
import com.ss.dincharya.Todo.TodoPojo

@Database(
    entities = [TodoPojo::class],
    version = 1,
    exportSchema = false
)
abstract class DatabaseClass :RoomDatabase() {
    abstract fun todoDao(): TodoDao
}