package com.sshclient.ui

import android.os.Bundle
import android.content.Intent
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sshclient.R
import com.sshclient.util.StorageUtil
import java.io.File

class KeyManager : AppCompatActivity() {

    private val keys = mutableList<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_manager)

        val recycler = findViewById<RecyclerView>(R.id.recyclerKeys)
        val fabAdd = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddKey)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = KeyAdapter(keys) { key, action ->
            handleKeyAction(key, action)
        }

        fabAdd.setOnClickListener {
            showKeyDialog(null)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshKeys()
    }

    private fun refreshKeys() {
        keys.clear()
        keys.addAll(StorageUtil.getKeysDir(this).listFiles()?.toList() ?: emptyList())
        recycler.adapter?.notifyDataSetChanged()
    }

    private fun handleKeyAction(key: File, action: String) {
        when (action) {
            "delete" -> {
                AlertDialog.Builder(this)
                    .setTitle("Delete ${key.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        key.delete()
                        refreshKeys()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            "view" -> {
                // Show key details
                AlertDialog.Builder(this)
                    .setTitle(key.name)
                    .setMessage("Path: ${key.absolutePath}\nSize: ${key.length()} bytes\nModified: ${key.lastModified()}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showKeyDialog(existingKey: File?) {
        val isEdit = existingKey != null
        val dialogView = layoutInflater.inflate(R.layout.dialog_key, null)

        val etName = dialogView.findViewById<EditText>(R.id.etKeyName)
        val etPassphrase = dialogView.findViewById<EditText>(R.id.etPassphrase)
        val btnGenerate = dialogView.findViewById<Button>(R.id.btnGenerateKey)
        val btnImport = dialogView.findViewById<Button>(R.id.btnImportKey)

        if (isEdit) {
            etName.setText(existingKey.name)
        }

        btnGenerate.setOnClickListener {
            generateKey(etName.text.toString())
        }

        btnImport.setOnClickListener {
            // Import key from file picker
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(intent, 100)
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Edit Key" else "Add Key")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generateKey(name: String) {
        // Generate SSH key pair using Java KeyPairGenerator
        // Implementation would use SSHJ key generation
        Toast.makeText(this, "Key generation: $name", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Handle imported key
            Toast.makeText(this, "Key imported", Toast.LENGTH_SHORT).show()
        }
    }
}