package um.edu.ar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import um.edu.ar.network.NetworkUtils.httpClient
import um.edu.ar.network.model.Dispositivo

@Composable
@Preview
fun App() {
    MaterialTheme {
        var dispositivos by remember { mutableStateOf(listOf<Dispositivo>()) }  // Lista de dispositivos
        var isLoading by remember { mutableStateOf(false) }
        var selectedDispositivo by remember { mutableStateOf<Dispositivo?>(null) }  // Dispositivo seleccionado
        var totalPrecio by remember { mutableStateOf(0.0) }  // Precio total
        var token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTcyOTg4MjY1NCwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzI5Nzk2MjU0fQ.Dm6dSnk9dp4MlSIBeyPRSxLxmG30XLnjQWPWl-XzEOTIzGXgGGvakTGM4N_yWne2l5F7ds6wMe1tqWzB77uOQg"

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón para cargar dispositivos
            Button(onClick = {
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val urlDispositivos = "http://10.0.2.2:8080/api/dispositivos"
                        val response = httpClient.get(urlDispositivos) {
                            headers {
                                append(HttpHeaders.Authorization, "Bearer $token")
                            }
                        }
                        // Convertir el texto en una lista de Dispositivos usando kotlinx.serialization
                        dispositivos = Json.decodeFromString(response.bodyAsText())
                    } catch (e: Exception) {
                        dispositivos = listOf()
                    } finally {
                        isLoading = false
                    }
                }
            }) {
                Text("Cargar Dispositivos")
            }

            // Mostrar total de precio
            Text("Precio Total: $totalPrecio USD", style = MaterialTheme.typography.h5)

            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Mostrar dispositivos cuando se hayan cargado
                dispositivos.forEach { dispositivo ->
                    DispositivoCard(
                        dispositivo = dispositivo,
                        isSelected = dispositivo == selectedDispositivo,
                        onClick = {
                            selectedDispositivo = dispositivo
                            totalPrecio = dispositivo.precioBase // Actualizar el precio total con la selección
                        }
                    )
                }
            }
        }
    }
}

// Función para mostrar cada dispositivo
@Composable
fun DispositivoCard(dispositivo: Dispositivo, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.3f) else MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Código: ${dispositivo.codigo}", style = MaterialTheme.typography.h6)
            Text("Nombre: ${dispositivo.nombre}", style = MaterialTheme.typography.body1)
            Text("Descripción: ${dispositivo.descripcion}", style = MaterialTheme.typography.body2)
            Text("Precio: ${dispositivo.precioBase} ${dispositivo.moneda}", style = MaterialTheme.typography.body2)
            Divider()
        }
    }
}
