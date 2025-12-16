package com.example.drawit.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.drawit.DrawItApplication
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.data.remote.repository.User
import kotlinx.coroutines.delay


@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as DrawItApplication

    val searchQuery = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) } // replace String with your User model
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery.value) {
        isLoading = true
        error.value = null

        delay(500)
        val result = app.firestorePaintingsRepository.searchForUser(searchQuery.value)


        when ( result ) {
            is NetworkResult.Success -> {
                users = result.data
            }

            is NetworkResult.Error -> {
                error.value = result.message
                users = emptyList()
            }

            else -> {
                users = emptyList()
            }
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .then( modifier ),
            textAlign = TextAlign.Center,
            text = "Friends screen (step 5)",
            style = MaterialTheme.typography.displayMedium
        )

        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { v ->
                searchQuery.value = v
            },
            label = { Text("Search friends") }
        )

        if ( error.value != null ) {
            Text(
                modifier = Modifier
                    .padding(8.dp),
                text = "Error: ${error.value}",
                color = MaterialTheme.colorScheme.error
            )
        }

        when {
            isLoading && users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            users.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users) { user ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = user.toString()
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No users found")
                }
            }
        }
    }
}