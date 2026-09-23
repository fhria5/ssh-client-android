package com.sshclient.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sshclient.R
import com.sshclient.model.SshSession
import com.sshclient.service.SshService
import net.schmizz.sshj.sftp.SFTPClient
import java.io.File

class SftpBrowser : AppCompatActivity() {

    private var sessionId: String = ""
    private var currentPath: String = "/"
    private var sftpClient: SFTPClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sftp)

        sessionId = intent.getStringExtra("session_id") ?: ""
        val tvPath = findViewById<TextView>(R.id.tvPath)
        val btnUp = findViewById<Button>(R.id.btnUp)
        val recycler = findViewById<RecyclerView>(R.id.recyclerFiles)

        recycler.layoutManager = LinearLayoutManager(this)

        val session = SshService.getSession(sessionId)
        sftpClient = session?.sftpClient ?: run {
            Thread {
                try {
                    val sftp = session?.sshClient?.newSFTPClient()
                    session?.sftpClient = sftp
                    runOnUiThread { refreshFileList(tvPath, recycler) }
                } catch (e: Exception) {
                    runOnUiThread { Toast.makeText(this, "SFTP error: ${e.message}", Toast.LENGTH_LONG).show() }
                }
            }.start()
        }

        btnUp.setOnClickListener {
            val parent = File(currentPath).parent ?: "/"
            currentPath = parent
            refreshFileList(tvPath, recycler)
        }
    }

    private fun refreshFileList(tvPath: TextView, recycler: RecyclerView) {
        tvPath.text = currentPath
        Thread {
            try {
                val rawFiles = sftpClient?.ls(currentPath) ?: emptyList()
                val files = rawFiles.map { file ->
                    mapOf("name" to file.toString(), "isDirectory" to false)
                }
                runOnUiThread {
                    recycler.adapter = SftpAdapter(files) { file ->
                        val name = file["name"] as? String ?: ""
                        Toast.makeText(this, if (file["isDirectory"] == true) "Open: $name" else "Download: $name", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }
}
