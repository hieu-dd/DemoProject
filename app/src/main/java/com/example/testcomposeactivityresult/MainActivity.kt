package com.example.testcomposeactivityresult

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.content.RestrictionsManager.RESULT_ERROR
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.testcomposeactivityresult.ui.theme.TestComposeActivityResultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestComposeActivityResultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val launcher =
                        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) { result ->
                            val data: Intent? = result.data
                            when (result.resultCode) {
                                Activity.RESULT_OK -> {
                                    data?.let {


                                    }
                                }
                                Activity.RESULT_CANCELED -> {
                                    // The user canceled the operation.
                                }
                            }
                        }
                    Greeting("Android")
                    Button(onClick = {
                        val intent = Intent(this, TestActivity::class.java);
                        launcher.launch(
                            IntentSenderRequest.Builder(PendingIntent.getActivity(this, 0, intent, FLAG_MUTABLE))
                                .build()
                        )
                    }) {
                        Text("Start Second Activity")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TestComposeActivityResultTheme {
        Greeting("Android")
    }
}