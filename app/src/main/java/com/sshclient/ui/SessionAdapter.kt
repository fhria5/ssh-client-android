package com.sshclient.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SessionAdapter(
    private val items: MutableList<SessionItem>,
    private val onClick: (String, String) -> Unit
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val btnConnect: Button = view.findViewById(R.id.btnConnect)
        val btnSftp: Button = view.findViewById(R.id.btnSftp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.btnConnect.setOnClickListener { onClick(item.id, "connect") }
        holder.btnSftp.setOnClickListener { onClick(item.id, "sftp") }
    }

    override fun getItemCount() = items.size

    fun appendOutput(sessionId: String, data: String) {
        // noop for now
    }

    fun updateStatus(sessionId: String, connected: Boolean) {
        // noop for now
    }

    fun updateItems(newItems: List<SessionItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
