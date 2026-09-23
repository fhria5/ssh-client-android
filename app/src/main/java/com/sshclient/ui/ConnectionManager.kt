package com.sshclient.ui

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sshclient.R
import com.sshclient.model.ConnectionConfig
import com.sshclient.util.StorageUtil

class ConnectionManager : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ConnectionAdapter
    private val connections = mutableListOf<ConnectionConfig>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_manager)

        recycler = findViewById(R.id.recyclerConnections)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        connections.addAll(StorageUtil.loadConnections(this))
        adapter = ConnectionAdapter(connections) { config, action ->
            handleAction(config, action)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fabAdd.setOnClickListener {
            showEditDialog(null)
        }
    }

    override fun onResume() {
        super.onResume()
        connections.clear()
        connections.addAll(StorageUtil.loadConnections(this))
        adapter.notifyDataSetChanged()
    }

    private fun handleAction(config: ConnectionConfig, action: String) {
        when (action) {
            "edit" -> showEditDialog(config)
            "delete" -> {
                AlertDialog.Builder(this)
                    .setTitle("Delete ${config.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        connections.remove(config)
                        StorageUtil.saveConnections(this, connections)
                        adapter.notifyDataSetChanged()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            "connect" -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("connect_id", config.id)
                }
                startActivity(intent)
            }
        }
    }

    private fun showEditDialog(config: ConnectionConfig?) {
        val isEdit = config != null
        val dialogView = layoutInflater.inflate(R.layout.dialog_connection, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etHost = dialogView.findViewById<EditText>(R.id.etHost)
        val etPort = dialogView.findViewById<EditText>(R.id.etPort)
        val etUser = dialogView.findViewById<EditText>(R.id.etUser)
        val etPass = dialogView.findViewById<EditText>(R.id.etPass)
        val etKeyPath = dialogView.findViewById<EditText>(R.id.etKeyPath)
        val etPassphrase = dialogView.findViewById<EditText>(R.id.etPassphrase)
        val spAuth = dialogView.findViewById<Spinner>(R.id.spAuthMethod)
        val cbCompress = dialogView.findViewById<CheckBox>(R.id.cbCompress)

        if (isEdit) {
            etName.setText(config.name)
            etHost.setText(config.host)
            etPort.setText(config.port.toString())
            etUser.setText(config.username)
            etPass.setText(config.password)
            etKeyPath.setText(config.privateKeyPath)
            etPassphrase.setText(config.passphrase)
            spAuth.setSelection(config.authMethod.ordinal)
            cbCompress.isChecked = config.compress
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Edit Connection" else "New Connection")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newConfig = ConnectionConfig(
                    id = config?.id ?: System.currentTimeMillis().toString(),
                    name = etName.text.toString(),
                    host = etHost.text.toString(),
                    port = etPort.text.toString().toIntOrNull() ?: 22,
                    username = etUser.text.toString(),
                    authMethod = ConnectionConfig.AuthMethod.entries[spAuth.selectedItemPosition],
                    password = etPass.text.toString(),
                    privateKeyPath = etKeyPath.text.toString(),
                    passphrase = etPassphrase.text.toString(),
                    compress = cbCompress.isChecked
                )
                if (isEdit) {
                    val idx = connections.indexOfFirst { it.id == config.id }
                    if (idx >= 0) connections[idx] = newConfig
                } else {
                    connections.add(newConfig)
                }
                StorageUtil.saveConnections(this, connections)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class ConnectionAdapter(
    private val items: List<ConnectionConfig>,
    private val onAction: (ConnectionConfig, String) -> Unit
) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val config = items[position]
        holder.text1.text = config.name
        holder.text2.text = "${config.username}@${config.host}:${config.port} [${config.authMethod}]"
        holder.itemView.findViewById<ImageView>(android.R.id.icon)?.let {
            it.setImageResource(
                when (config.authMethod) {
                    ConnectionConfig.AuthMethod.PASSWORD -> R.drawable.ic_key
                    ConnectionConfig.AuthMethod.PRIVATE_KEY -> R.drawable.ic_lock
                    ConnectionConfig.AuthMethod.AGENT -> R.drawable.ic_agent
                }
            )
        }

        holder.itemView.findViewById<ImageButton>(R.id.btnEdit)?.setOnClickListener {
            onAction(config, "edit")
        }
        holder.itemView.findViewById<ImageButton>(R.id.btnDelete)?.setOnClickListener {
            onAction(config, "delete")
        }
        holder.itemView.findViewById<ImageButton>(R.id.btnConnect)?.setOnClickListener {
            onAction(config, "connect")
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1: TextView = view.findViewById(android.R.id.text1)
        val text2: TextView = view.findViewById(android.R.id.text2)
    }
}