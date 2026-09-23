package com.sshclient.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.sshclient.R
import com.sshclient.model.ConnectionConfig
import com.sshclient.model.SshSession
import com.sshclient.ui.MainActivity
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class SshService : android.app.Service() {

    companion object {
        const val CHANNEL_ID = "ssh_client"
        const val NOTIF_ID = 1001
        const val ACTION_CONNECT = "com.sshclient.CONNECT"
        const val ACTION_DISCONNECT = "com.sshclient.DISCONNECT"
        const val ACTION_SEND = "com.sshclient.SEND"
        const val EXTRA_CONFIG = "config"
        const val EXTRA_DATA = "data"
        const val EXTRA_SESSION_ID = "session_id"

        private val sessions = mutableMapOf<String, SshSession>()
        private val wakeLocks = mutableMapOf<String, PowerManager.WakeLock>()
        private val keepAliveHandlers = mutableMapOf<String, Handler>()
        private var notifManager: NotificationManager? = null

        fun getSession(id: String): SshSession? = sessions[id]
        fun getActiveSessions(): Map<String, SshSession> = sessions.toMap()
        fun removeSession(id: String) {
            sessions.remove(id)
            wakeLocks.remove(id)?.release()
            keepAliveHandlers.remove(id)?.removeCallbacksAndMessages(null)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect(intent)
            ACTION_DISCONNECT -> disconnect(intent)
            ACTION_SEND -> sendData(intent)
        }
        return START_STICKY
    }

    private fun connect(intent: Intent) {
        val config = intent.getParcelableExtra<ConnectionConfig>(EXTRA_CONFIG) ?: return
        val sessionId = config.id.ifEmpty { "${config.host}:${config.port}" }

        startForeground(NOTIF_ID, buildNotification("Connecting to ${config.host}...").build())

        Thread {
            tryConnect(config, sessionId)
        }.start()
    }

    private fun tryConnect(config: ConnectionConfig, sessionId: String, retryCount: Int = 0) {
        var sshClient: SSHClient? = null
        try {
            sshClient = SSHClient()
            sshClient.addHostKeyVerifier(PromiscuousVerifier())
            sshClient.setConnectTimeout(config.timeout)
            sshClient.connect(config.host, config.port)

            when (config.authMethod) {
                ConnectionConfig.AuthMethod.PASSWORD -> {
                    sshClient.authPassword(config.username, config.password)
                }
                ConnectionConfig.AuthMethod.PRIVATE_KEY -> {
                    sshClient.authPublickey(config.username, config.privateKeyPath)
                }
                else -> {}
            }

            val session = sshClient.startSession()

            // Execute a shell command to get interactive shell
            val command = session.exec("/bin/sh")
            val stdoutStream = command.getInputStream()
            val stderrStream = command.getErrorStream()
            val stdinStream = command.getOutputStream()

            // ACQUIRE WAKE LOCK
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SSHClient::KeepAlive")
            wakeLock.acquire(60 * 60 * 1000L)
            wakeLocks[sessionId] = wakeLock

            // SSH keep-alive (no-op - using background thread instead)

            // Read stdout
            Thread {
                val buffer = ByteArray(4096)
                try {
                    while (true) {
                        try {
                            val available = stdoutStream.available()
                            if (available > 0) {
                                val n = stdoutStream.read(buffer, 0, minOf(available, buffer.size))
                                if (n < 0) { break } else if (n > 0) {
                                    val data = String(buffer, 0, n, java.nio.charset.Charset.forName(config.characterSet))
                                    broadcastOutput(sessionId, data)
                                }
                            }
                            Thread.sleep(50)
                        } catch (_: Exception) { break }
                    }
                } catch (_: Exception) {}
            }.start()

            val sshSession = SshSession(
                config, sshClient, session,
                stdinStream, stdoutStream, stderrStream
            )
            sessions[sessionId] = sshSession

            broadcastStatus(sessionId, true)
            broadcastOutput(sessionId, "\r\nConnected to ${config.host}\r\n")

            handlerPost {
                notifManager?.notify(NOTIF_ID, buildNotification("Connected: ${config.name}").build())
            }
        } catch (e: Exception) {
            if (retryCount < 3) {
                handlerPostDelayed({ tryConnect(config, sessionId, retryCount + 1) }, 5000L)
            } else {
                broadcastError(sessionId, "Failed after 3 retries: ${e.message}")
                releaseWakeLock(sessionId)
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun sendData(intent: Intent) {
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        val data = intent.getStringExtra(EXTRA_DATA) ?: return
        val session = sessions[sessionId] ?: return
        try {
            session.stdin.write(data.toByteArray(java.nio.charset.Charset.forName(session.config.characterSet)))
            session.stdin.flush()
        } catch (_: Exception) {}
    }

    private fun disconnect(intent: Intent) {
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        releaseWakeLock(sessionId)
        keepAliveHandlers.remove(sessionId)?.removeCallbacksAndMessages(null)
        sessions[sessionId]?.close()
        sessions.remove(sessionId)
        broadcastStatus(sessionId, false)
        if (sessions.isEmpty()) stopForeground(true)
    }

    private fun releaseWakeLock(sessionId: String) {
        try { wakeLocks.remove(sessionId)?.release() } catch (_: Exception) {}
    }

    private fun broadcastOutput(sessionId: String, data: String) {
        sendBroadcast(Intent("SSH_OUTPUT").putExtra("session_id", sessionId).putExtra("data", data))
    }

    private fun broadcastStatus(sessionId: String, connected: Boolean) {
        sendBroadcast(Intent("SSH_STATUS").putExtra("session_id", sessionId).putExtra("connected", connected))
    }

    private fun broadcastError(sessionId: String, error: String) {
        sendBroadcast(Intent("SSH_ERROR").putExtra("session_id", sessionId).putExtra("error", error))
    }

    private fun handlerPost(action: Runnable) {
        Handler(Looper.getMainLooper()).post(action)
    }

    private fun handlerPostDelayed(action: Runnable, delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }

    private fun buildNotification(text: String): NotificationCompat.Builder {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SSH Client").setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent).setOngoing(true)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "SSH Client", NotificationManager.IMPORTANCE_LOW)
            notifManager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
