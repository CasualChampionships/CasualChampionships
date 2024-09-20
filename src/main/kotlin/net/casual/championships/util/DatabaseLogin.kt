package net.casual.championships.util

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseLogin(
    val url: String = "",
    val username: String = "",
    val password: String = ""
)