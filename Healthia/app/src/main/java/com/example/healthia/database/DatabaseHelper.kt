package com.example.healthia.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dbhealthia"
        private const val DATABASE_VERSION = 1
        private const val CREATE_TABLE_USER =
            "CREATE TABLE ${DatabaseContract.UserColumns.tableName}" +
                    "(${DatabaseContract.UserColumns.userId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.UserColumns.firstName} TEXT NOT NULL," +
                    "${DatabaseContract.UserColumns.lastName} TEXT NOT NULL," +
                    "${DatabaseContract.UserColumns.email} TEXT NOT NULL," +
                    "${DatabaseContract.UserColumns.password} TEXT NOT NULL," +
                    "${DatabaseContract.UserColumns.healthInformation} TEXT)"
        private const val CREATE_TABLE_PANIC_BROADCAST =
            "CREATE TABLE ${DatabaseContract.PanicBroadcastColumns.tableName}" +
                    "(${DatabaseContract.PanicBroadcastColumns.broadcastId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.PanicBroadcastColumns.broadcastPermission} TEXT NOT NULL," +
                    "${DatabaseContract.PanicBroadcastColumns.broadcastMessage} TEXT NOT NULL)"
        private const val CREATE_TABLE_FEEDBACK =
            "CREATE TABLE ${DatabaseContract.FeedbackColumns.tableName}" +
                    "(${DatabaseContract.FeedbackColumns.feedbackId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.FeedbackColumns.feedbackContent} TEXT NOT NULL)"
        private const val CREATE_TABLE_DETAIL_CONTENT =
            "CREATE TABLE ${DatabaseContract.DetailContentColumns.tableName}" +
                    "(${DatabaseContract.DetailContentColumns.userRefId} INTEGER REFERENCES ${DatabaseContract.UserColumns.tableName}(${DatabaseContract.UserColumns.userId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.forumId} INTEGER REFERENCES ${DatabaseContract.ForumColumns.tableName}(${DatabaseContract.ForumColumns.forumId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.statusId} INTEGER REFERENCES ${DatabaseContract.StatusColumns.tableName}(${DatabaseContract.StatusColumns.statusId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.broadcastId} INTEGER REFERENCES ${DatabaseContract.PanicBroadcastColumns.tableName}(${DatabaseContract.PanicBroadcastColumns.broadcastId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.feedbackId} INTEGER REFERENCES ${DatabaseContract.FeedbackColumns.tableName}(${DatabaseContract.FeedbackColumns.feedbackId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.commentId} INTEGER REFERENCES ${DatabaseContract.CommentColumns.tableName}(${DatabaseContract.CommentColumns.commentId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.articleId} INTEGER REFERENCES ${DatabaseContract.ArticleColumns.tableName}(${DatabaseContract.ArticleColumns.articleId}) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "${DatabaseContract.DetailContentColumns.date} TEXT NOT NULL," +
                    "PRIMARY KEY(${DatabaseContract.DetailContentColumns.userRefId}, ${DatabaseContract.DetailContentColumns.broadcastId}, ${DatabaseContract.DetailContentColumns.feedbackId}, ${DatabaseContract.DetailContentColumns.forumId}, ${DatabaseContract.DetailContentColumns.statusId}, ${DatabaseContract.DetailContentColumns.commentId}, ${DatabaseContract.DetailContentColumns.articleId}));"
        private const val CREATE_TABLE_COMMENT =
            "CREATE TABLE ${DatabaseContract.CommentColumns.tableName}" +
                    "(${DatabaseContract.CommentColumns.commentId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.CommentColumns.commentContent} TEXT NOT NULL)"
        private const val CREATE_TABLE_FORUM =
            "CREATE TABLE ${DatabaseContract.ForumColumns.tableName}" +
                    "(${DatabaseContract.ForumColumns.forumId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.ForumColumns.forumTitle} TEXT NOT NULL," +
                    "${DatabaseContract.ForumColumns.forumPhoto} TEXT," +
                    "${DatabaseContract.ForumColumns.forumContent} TEXT NOT NULL)"
        private const val CREATE_TABLE_STATUS =
            "CREATE TABLE ${DatabaseContract.StatusColumns.tableName}" +
                    "(${DatabaseContract.StatusColumns.statusId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.StatusColumns.statusPhoto} TEXT," +
                    "${DatabaseContract.StatusColumns.StatusContent} TEXT NOT NULL)"
        private const val CREATE_TABLE_ARTICLE =
            "CREATE TABLE ${DatabaseContract.ArticleColumns.tableName}" +
                    "(${DatabaseContract.ArticleColumns.articleId} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DatabaseContract.ArticleColumns.articleTitle} TEXT NOT NULL," +
                    "${DatabaseContract.ArticleColumns.articlePhoto} TEXT," +
                    "${DatabaseContract.ArticleColumns.articleContent} TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL(CREATE_TABLE_USER)
        db.execSQL(CREATE_TABLE_PANIC_BROADCAST)
        db.execSQL(CREATE_TABLE_FEEDBACK)
        db.execSQL(CREATE_TABLE_DETAIL_CONTENT)
        db.execSQL(CREATE_TABLE_COMMENT)
        db.execSQL(CREATE_TABLE_FORUM)
        db.execSQL(CREATE_TABLE_STATUS)
        db.execSQL(CREATE_TABLE_ARTICLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVer: Int, newVer: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.UserColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.PanicBroadcastColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.FeedbackColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.DetailContentColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.CommentColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.ForumColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.StatusColumns.tableName}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.ArticleColumns.tableName}")
    }
}