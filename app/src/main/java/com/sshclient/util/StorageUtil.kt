package com.sshclient.util

import android.content.Context
import com.sshclient.model.ConnectionConfig
import java.io.File

object StorageUtil {

    fun getConnectionsFile(context: Context): File {
        return File(context.filesDir, "connections.json")
    }

    fun getKeysDir(context: Context): File {
        val dir = File(context.filesDir, "keys")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPrivateKeyPath(context: Context, filename: String): File {
        return File(getKeysDir(context), filename)
    }

    fun saveConnections(context: Context, connections: List<ConnectionConfig>) {
        val file = getConnectionsFile(context)
        file.writeText(ConnectionConfigSerializer.serialize(connections))
    }

    fun loadConnections(context: Context): List<ConnectionConfig> {
        val file = getConnectionsFile(context)
        if (!file.exists()) return emptyList()
        return ConnectionConfigSerializer.deserialize(file.readText())
    }
}

object ConnectionConfigSerializer {

    fun serialize(configs: List<ConnectionConfig>): String {
        val sb = StringBuilder("[")
        configs.forEachIndexed { i, c ->
            if (i > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":\"${escape(c.id)}\",")
            sb.append("\"name\":\"${escape(c.name)}\",")
            sb.append("\"host\":\"${escape(c.host)}\",")
            sb.append("\"port\":${c.port},")
            sb.append("\"username\":\"${escape(c.username)}\",")
            sb.append("\"authMethod\":\"${c.authMethod}\",")
            sb.append("\"password\":\"${escape(c.password)}\",")
            sb.append("\"privateKeyPath\":\"${escape(c.privateKeyPath)}\",")
            sb.append("\"passphrase\":\"${escape(c.passphrase)}\",")
            sb.append("\"characterSet\":\"${escape(c.characterSet)}\",")
            sb.append("\"encoding\":\"${escape(c.encoding)}\",")
            sb.append("\"compress\":${c.compress},")
            sb.append("\"keepAliveInterval\":${c.keepAliveInterval},")
            sb.append("\"timeout\":${c.timeout}")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun deserialize(json: String): List<ConnectionConfig> {
        val configs = mutableListOf<ConnectionConfig>()
        // Simple JSON parser for our format
        val items = json.trim().removePrefix("[").removeSuffix("]").split("},{")
        items.forEach { item ->
            var clean = item.trim().trim('{', '}')
            val map = mutableMapOf<String, String>()
            clean.split(",").forEach { field ->
                val (key, value) = field.split(":", limit = 2)
                map[key.trim().trim('"')] = value.trim().trim('"')
            }
            configs.add(ConnectionConfig(
                id = map["id"] ?: "",
                name = map["name"] ?: "",
                host = map["host"] ?: "",
                port = map["port"]?.toIntOrNull() ?: 22,
                username = map["username"] ?: "",
                authMethod = try { ConnectionConfig.AuthMethod.valueOf(map["authMethod"] ?: "PASSWORD") } catch (_: Exception) { ConnectionConfig.AuthMethod.PASSWORD },
                password = map["password"] ?: "",
                privateKeyPath = map["privateKeyPath"] ?: "",
                passphrase = map["passphrase"] ?: "",
                characterSet = map["characterSet"] ?: "UTF-8",
                encoding = map["encoding"] ?: "UTF-8",
                compress = map["compress"]?.toBoolean() ?: false,
                keepAliveInterval = map["keepAliveInterval"]?.toIntOrNull() ?: 30,
                timeout = map["timeout"]?.toIntOrNull() ?: 10000
            ))
        }
        return configs
    }

    private fun escape(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")
}