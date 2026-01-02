package com.ss.dincharya.Todo

interface TodoInterface {
    fun onItemClick(position :Int)
    fun onItemInsert(pojo: TodoPojo)
    fun onItemEdit(pojo: TodoPojo)
    fun onItemDelete(position :Int)
}