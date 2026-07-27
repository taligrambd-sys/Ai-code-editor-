package com.example.model

import java.util.UUID

enum class Sender {
    USER, AI, SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val content: String,
    val extractedCode: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)
