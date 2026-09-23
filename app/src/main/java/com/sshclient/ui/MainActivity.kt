package com.sshclient.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.sshclient.R
import com.sshclient.model.ConnectionConfig
import com.sshclient.service.SshService
import com.sshclient.util.StorageUtil

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerSessions: RecyclerView
    private lateinit var btnConnect: FloatingActionButton
    private lateinit var tvStatus: TextView
    private var adapter: SessionAdapter? = null

    private val outputReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sessionId = intent?.getStringExtra("session_id") ?: return
            val data = intent.getStringExtra("data") ?: return
            adapter?.appendOutput(sessionId, data)
        }
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sessionId = intent?.getStringExtra("session_id") ?: return
            val connected = intent.getBooleanExtra("connected", false)
            adapter?.updateStatus(sessionId, connected)
            updateConnectionCount()
        }
    }

    private val errorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sessionId = intent?.getStringExtra("session_id") ?: return
            val error = intent.getStringExtra("error") ?: "Error"
            runOnUiThread {
                Toast.makeText(this, "[$sessionId] $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerSessions = findViewById(R.id.recyclerSessions)
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)

        recyclerSessions.layoutManager = LinearLayoutManager(this)
        adapter = SessionAdapter(mutableListOf()) { sessionId, action ->
            handleSessionAction(sessionId, action)
        }
        recyclerSessions.adapter = adapter

        btnConnect.setOnClickListener {
            startActivity(Intent(this, ConnectionManager::class.java))
        }

        // Load saved connections
        loadConnections()
    }

    override fun onResume() {
        super.onResume()
        loadConnections()
        registerReceiver(outputReceiver, IntentFilter("SSH_OUTPUT"))
        registerReceiver(statusReceiver, IntentFilter("SSH_STATUS"))
        registerReceiver(errorReceiver, IntentFilter("SSH_ERROR"))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(outputReceiver)
        unregisterReceiver(statusReceiver)
        unregisterReceiver(errorReceiver)
    }

    private fun loadConnections() {
        val connections = StorageUtil.loadConnections(this)
        val sessions = SshService.getActiveSessions()
        val items = connections.map { config ->
            SessionItem(
                config.id,
                config.name,
                "${config.username}@${config.host}:${config.port}",
                sessions.containsKey(config.id)
            )
        }
        adapter?.updateItems(item)
        updateConnectionCount()
    }

    private fun handleSessionAction(sessionId: String, action: String) {
        when (action) {
            "connect" -> {
                val connections = StorageUtil.loadConnections(this)
                val config = connections.find { it.id == sessionId } ?: return
                val intent = Intent(this, SshService::class.java).apply {
                    action = SshService.ACTION_CONNECT
                    putExtra(SshService.EXTRA_CONFIG, config)
                }
                startService(intent)
            }
            "disconnect" -> {
                val intent = Intent(this, SshService::class.java).apply {
                    action = SshService.ACTION_DISCONNECT
                    putExtra(SshService.EXTRA_SESSION_ID, sessionId)
                }
                startService(intent)
            }
            "send" -> {
                // Open terminal for this session
                val intent = Intent(this, TerminalView::class.java).apply {
                    putExtra("session_id", sessionId)
                }
                startActivity(intent)
            }
            "sftp" -> {
                val intent = Intent(this, SftpBrowser::class.java).apply {
                    putExtra("session_id", sessionId)
                }
                startActivity(intent)
            }
        }
    }

    private fun updateConnectionCount() {
        val count = SshService.getActiveSessions().size
        tvStatus.text = "Active: $count session(s)"
    }
}

data class SessionItem(
    val id: String,
    val name: String,
    val hostInfo: String,
    val isConnected: Boolean
)