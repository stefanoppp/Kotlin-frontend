package um.edu.ar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import frontapi.composeapp.generated.resources.Res
import frontapi.composeapp.generated.resources.compose_multiplatform
import io.ktor.client.request.get
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import um.edu.ar.network.NetworkUtils
import um.edu.ar.network.NetworkUtils.httpClient

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var urlDispositivos = "http://10.0.2.2:8080/api/dispositivos"
        var response by remember { mutableStateOf("Ver dispositivos") }
        val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTcyOTg4MjY1NCwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzI5Nzk2MjU0fQ.Dm6dSnk9dp4MlSIBeyPRSxLxmG30XLnjQWPWl-XzEOTIzGXgGGvakTGM4N_yWne2l5F7ds6wMe1tqWzB77uOQg"

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                response = "Cargando"
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Realizamos la petición GET con el token en los headers
                        val test = httpClient.get(urlDispositivos) {
                            headers {
                                append(HttpHeaders.Authorization, "Bearer $token")
                            }
                        }
                        response = test.bodyAsText()
                    } catch (e: Exception) {
                        response = "Error: ${e.message}"
                    }
                }
            }) {
                Text("Go!")
            }
            Text(response)
        }
    }
}
