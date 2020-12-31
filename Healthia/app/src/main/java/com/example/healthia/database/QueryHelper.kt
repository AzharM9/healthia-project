package com.example.healthia.database

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

class QueryHelper(context: Context) {

    companion object{
        private lateinit var databaseHelper: DatabaseHelper
        private var INSTANCE: QueryHelper? = null

        fun getInstance(context: Context):QueryHelper =
            INSTANCE ?: synchronized(this){
                INSTANCE ?: QueryHelper(context)
            }
        private lateinit var database: SQLiteDatabase
    }

    init {
        databaseHelper = DatabaseHelper(context)
    }

    @Throws(SQLException::class)
    fun open(){
        database = databaseHelper.writableDatabase
    }

    fun close(){
        databaseHelper.close()

        if (database.isOpen)
            database.close()
    }
}