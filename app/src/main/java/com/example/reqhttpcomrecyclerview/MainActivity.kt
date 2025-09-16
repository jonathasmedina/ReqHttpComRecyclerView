package com.example.reqhttpcomrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val postsList = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = PostAdapter(postsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val btnGetData = findViewById<Button>(R.id.btnGetData)
        btnGetData.setOnClickListener {
            carregarPosts()
        }
    }

    private fun carregarPosts() {
        thread { // usar thread para executar a requisição em background
            try {
                val url = URL("https://jsonplaceholder.typicode.com/posts")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) { // se a requisição foi bem sucedida
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String? // variável para armazenar cada linha da resposta ? para não dar erro de compilação
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    // Parse JSON
                    val jsonArray = JSONArray(response.toString())
                    postsList.clear()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val post = Post(
                            userId = obj.getInt("userId"),
                            id = obj.getInt("id"),
                            title = obj.getString("title"),
                            body = obj.getString("body")
                        )
                        postsList.add(post)
                    }

                    // Atualiza a RecyclerView na UI thread
                    // se fizesse sem thread, não seria possível atualizar a RecyclerView
                    // pois a atualização é feita na thread de background
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}