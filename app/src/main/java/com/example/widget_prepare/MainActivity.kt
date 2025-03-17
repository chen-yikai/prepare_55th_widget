package com.example.widget_prepare

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Column {
                ApiFetcher()
                NetworkImage()
            }
        }
    }
}

data class Station(
    val station: String,
    @SerializedName("current_time") val currentTime: String,
    val schedule: List<Schedule>,
)

data class Schedule(
    val id: Int,
    val line: String,
    val destination: String,
    val status: String,
    @SerializedName("arrival_time") val arrivalTime: String,
    val departure: String
)

@Composable
fun NetworkImage() {
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            imageBitmap = URL("https://eliaschen.dev/eliaschen.jpg").openStream()
                .use { BitmapFactory.decodeStream(it) } ?: null
        }
    }
    imageBitmap?.asImageBitmap()?.let {
        Image(bitmap = it, contentDescription = "")
    }
}

@Composable
fun ApiPrepare() {
    var data by remember { mutableStateOf<List<Station>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(
                "https://skills-flutter-test-api.eliaschen.dev/"
            ).build()
            data = client.newCall(request).execute().use {
                Gson().fromJson(it.body?.string(), object : TypeToken<List<Station>>() {}.type)
            }
        }
    }


}

@Composable
fun ApiFetcher() {
    var data by remember { mutableStateOf<List<Station>>(emptyList()) }

    LaunchedEffect(Unit) {
        val client = OkHttpClient()
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://skills-flutter-test-api.eliaschen.dev/")
                .build()
            data = client.newCall(request).execute().use { response ->
                Gson().fromJson(
                    response.body?.string(),
                    object : TypeToken<List<Station>>() {}.type
                )
            }
        }
    }

    if (data.isNotEmpty()) {
        LazyColumn {
            items(data[0].schedule) { schedule ->
                Text(schedule.destination)
            }
        }
    } else {
        Text("Loading...")
    }
}