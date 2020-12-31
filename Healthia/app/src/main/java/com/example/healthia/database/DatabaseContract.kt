package com.example.healthia.database

import android.provider.BaseColumns

internal class DatabaseContract {

    internal class UserColumns : BaseColumns {
        companion object {
            const val tableName = "User"
            const val userId = "UserId"
            const val firstName = "FirstName"
            const val lastName = "LastName"
            const val email = "Email"
            const val password = "Password"
            const val healthInformation = "HealthInformation"
        }
    }

    internal class PanicBroadcastColumns: BaseColumns{
        companion object{
            const val tableName = "PanicBroadcast"
            const val broadcastId = "BroadcastId"
            const val broadcastPermission = "BroadcastPermission"
            const val broadcastMessage = "BroadcastMessage"
        }
    }

    internal class FeedbackColumns: BaseColumns{
        companion object{
            const val tableName = "Feedback"
            const val feedbackId = "FeedbackId"
            const val feedbackContent = "FeedbackContent"
        }
    }

    internal class DetailContentColumns: BaseColumns{
        companion object{
            const val tableName = "DetailContent"
            const val userRefId = "UserRefId"
            const val forumId = "ForumId"
            const val statusId = "StatusId"
            const val broadcastId = "BroadcastId"
            const val feedbackId = "FeedbackId"
            const val commentId = "CommentId"
            const val articleId = "ArticleId"
            const val date = "Date"
        }
    }

    internal class CommentColumns:  BaseColumns{
        companion object{
            const val tableName = "Comment"
            const val commentId = "CommentId"
            const val commentContent = "CommentContent"
        }
    }

    internal class ForumColumns: BaseColumns{
        companion object{
            const val tableName = "Forum"
            const val forumId = "ForumId"
            const val forumTitle = "ForumTitle"
            const val forumPhoto = "Forum Photo"
            const val forumContent = "ForumContent"
        }
    }

    internal class StatusColumns: BaseColumns{
        companion object{
            const val tableName = "Status"
            const val statusId = "StatusId"
            const val statusPhoto = "StatusPhoto"
            const val StatusContent = "StatusContent"
        }
    }

    internal class ArticleColumns: BaseColumns{
        companion object{
            const val tableName = "Article"
            const val articleId = "ArticleId"
            const val articleTitle = "ArticleTitle"
            const val articlePhoto = "ArticlePhoto"
            const val articleContent = "ArticleContent"
        }
    }
}