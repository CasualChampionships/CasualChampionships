package net.casual.championships.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseLogin(
    val name: String = "",
    val url: String = "",
    val username: String = "",
    val password: String = ""
)