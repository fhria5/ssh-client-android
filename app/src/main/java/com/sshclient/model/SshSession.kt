package com.sshclient.model

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

data class SshSession(
    val config: ConnectionConfig,
    val sshClient: SSHClient,
    val session: Session,
    val stdin: OutputStream,
    val stdout: InputStream,
    val stderr: InputStream,
    var sftpClient: SFTPClient? = null
) : Closeable {

    override fun close() {
        try { sftpClient?.close() } catch (_: Exception) {}
        try { stdin.close() } catch (_: Exception) {}
        try { stdout.close() } catch (_: Exception) {}
        try { stderr.close() } catch (_: Exception) {}
        try { session.close() } catch (_: Exception) {}
        try { sshClient.disconnect() } catch (_: Exception) {}
    }
}