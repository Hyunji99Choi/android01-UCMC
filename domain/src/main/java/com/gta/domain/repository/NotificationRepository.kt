package com.gta.domain.repository

import com.gta.domain.model.Notification

interface NotificationRepository {
    suspend fun sendNotification(notification: Notification, receiverId: String): Boolean
    suspend fun saveNotification(notification: Notification, userId: String): Boolean
}
