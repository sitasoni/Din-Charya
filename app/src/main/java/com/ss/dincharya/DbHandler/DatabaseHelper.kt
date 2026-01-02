package com.ss.dincharya.DbHandler

import android.content.Context
import androidx.room.Room

class DatabaseHelper {
    fun getDatabaseInstance(ctx : Context) : DatabaseClass = Room.databaseBuilder(
        ctx.applicationContext,
        DatabaseClass::class.java,
        "activity_tracking_app"
    )
        .allowMainThreadQueries()
        .build()
}