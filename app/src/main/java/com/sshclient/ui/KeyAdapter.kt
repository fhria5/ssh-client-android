package com.sshclient.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sshclient.R
import java.io.File

class KeyAdapter(
    private val keys: MutableList<File>,
    private val onClick: (File, String) -> Unit
) : RecyclerView.Adapter<KeyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKey: TextView = view.findViewById(R.id.tvKey)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_key, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = keys[position]
        holder.tvKey.text = key.name
        holder.btnEdit.setOnClickListener { onClick(key, "edit") }
        holder.btnDelete.setOnClickListener { onClick(key, "delete") }
    }

    override fun getItemCount() = keys.size
}
