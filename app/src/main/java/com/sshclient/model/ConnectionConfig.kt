package com.sshclient.model

data class ConnectionConfig(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val authMethod: AuthMethod = AuthMethod.PASSWORD,
    val password: String = "",
    val privateKeyPath: String = "",
    val passphrase: String = "",
    val characterSet: String = "UTF-8",
    val encoding: String = "UTF-8",
    val compress: Boolean = false,
    val keepAliveInterval: Int = 30,
    val timeout: Int = 10000
) {
    enum class AuthMethod { PASSWORD, PRIVATE_KEY, AGENT }
}