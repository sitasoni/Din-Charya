package com.ss.dincharya.Todo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo")
data class TodoPojo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,
    val title: String,
    val description : String,
    val date : String = "",
    val time : String
){
    constructor(title: String,description: String,time: String):this(
        id = 0,
        title = title,
        description = description,
        time = time
    )
    constructor(title: String,description: String,time: String, date: String):this(
        id = 1,
        title = title,
        description = description,
        time = time,
        date = date
    )
}