package com.example.drawit.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.drawit.DrawItApplication
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.data.remote.repository.Friend
import com.example.drawit.data.remote.repository.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as DrawItApplication
    val repo = app.firestorePaintingsRepository

    val currentUser = app.authenticationRepository.getCurrentUser()!!

    var currentUsername by remember { mutableStateOf<String?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser.uid) {
        when (val res = repo.getUsernameById(currentUser.uid)) {
            is NetworkResult.Success -> currentUsername = res.data
            is NetworkResult.Error -> android.util.Log.e("FriendsScreen", "Error fetching username: ${res.message}")
            else -> currentUsername = null
        }
    }

    var sentRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var receivedRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }

    suspend fun refreshRelationships() {
        // sent
        when (val sent = repo.getSentFriendRequestsFromUser(currentUser.uid)) {
            is NetworkResult.Success -> sentRequests = sent.data
            is NetworkResult.Error -> android.util.Log.e("FriendsScreen", "Error sent: ${sent.message}")
            else -> {}
        }
        // received
        when (val rec = repo.getFriendRequestsForUser(currentUser.uid)) {
            is NetworkResult.Success -> receivedRequests = rec.data
            is NetworkResult.Error -> android.util.Log.e("FriendsScreen", "Error received: ${rec.message}")
            else -> {}
        }
        // friends
        when (val fr = repo.getFriendsForUser(currentUser.uid)) {
            is NetworkResult.Success -> friends = fr.data
            is NetworkResult.Error -> android.util.Log.e("FriendsScreen", "Error friends: ${fr.message}")
            else -> {}
        }
    }

    LaunchedEffect(searchQuery.value) {
        if (searchQuery.value.isBlank()) {
            users = emptyList()
            error.value = null
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        error.value = null

        delay(500)

        when (val result = repo.searchForUser(searchQuery.value)) {
            is NetworkResult.Success -> users = result.data
            is NetworkResult.Error -> {
                error.value = result.message
                users = emptyList()
            }
            else -> users = emptyList()
        }

        refreshRelationships()

        isLoading = false
    }

    // fetch friends and friend reqs anyway
    LaunchedEffect(Unit) {
        refreshRelationships()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.Center,
            text = "Friends",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            value = searchQuery.value,
            onValueChange = { v -> searchQuery.value = v },
            label = { Text("Search friends") }
        )

        if (error.value != null) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Error: ${error.value}",
                color = MaterialTheme.colorScheme.error
            )
        }

        if (isLoading && users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // search results
        if (users.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Search results",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            ) {
                items(users) { user ->
                    if (user.userId == currentUser.uid) {
                        // Skip self
                        return@items
                    }

                    // compute relationship state
                    val isFriend = friends.any { doc ->
                        val ids = doc.ids as? List<*>
                        ids?.contains(user.userId) == true
                    }

                    val sentRequest = sentRequests.firstOrNull { doc ->
                        doc["toId"] == user.userId
                    }
                    val receivedRequest = receivedRequests.firstOrNull { doc ->
                        doc["fromId"] == user.userId
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val scope = rememberCoroutineScope( )
                                Text(
                                    text = when {
                                        isFriend -> "Friend"
                                        sentRequest != null -> "Request sent"
                                        receivedRequest != null -> "Request received"
                                        else -> "Not friend"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Button(
                                    onClick = {
                                        scope.launch {
                                            when {
                                                isFriend -> {
                                                    when (val res = repo.removeFriend(currentUser.uid, user.userId)) {
                                                        is NetworkResult.Success -> refreshRelationships()
                                                        is NetworkResult.Error ->
                                                            android.util.Log.e("FriendsScreen", "removeFriend failed: ${res.message}")
                                                        else -> {}
                                                    }
                                                }

                                                sentRequest != null -> {
                                                    when (val res = repo.declineFriendRequest(
                                                        fromUserId = currentUser.uid,
                                                        toUserId = user.userId
                                                    )) {
                                                        is NetworkResult.Success -> refreshRelationships()
                                                        is NetworkResult.Error ->
                                                            android.util.Log.e("FriendsScreen", "declineFriendRequest failed: ${res.message}")
                                                        else -> {}
                                                    }
                                                }

                                                receivedRequest != null -> {
                                                    val requestId = "${user.userId}_to_${currentUser.uid}"
                                                    when (val res = repo.acceptFriendRequest(requestId)) {
                                                        is NetworkResult.Success -> refreshRelationships()
                                                        is NetworkResult.Error ->
                                                            android.util.Log.e("FriendsScreen", "acceptFriendRequest failed: ${res.message}")
                                                        else -> {}
                                                    }
                                                }

                                                else -> {
                                                    val fromName = currentUsername ?: currentUser.displayName ?: "Unknown"
                                                    when (val res = repo.sendFriendRequest(
                                                        fromUserId = currentUser.uid,
                                                        fromUsername = fromName,
                                                        toUserId = user.userId,
                                                        toUsername = user.username
                                                    )) {
                                                        is NetworkResult.Success -> refreshRelationships()
                                                        is NetworkResult.Error ->
                                                            android.util.Log.e("FriendsScreen", "sendFriendRequest failed: ${res.message}")
                                                        else -> {}
                                                    }
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            isFriend -> "Remove friend"
                                            sentRequest != null -> "Cancel"
                                            receivedRequest != null -> "Accept"
                                            else -> "Add friend"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // sent reqs, received reqs, friends
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 170.dp)
        ) {
            Text("Sent requests", style = MaterialTheme.typography.titleSmall)
            if (sentRequests.isEmpty()) {
                Text("None", style = MaterialTheme.typography.bodySmall)
            } else {
                sentRequests.forEach { req ->
                    val toUsername = req["toUsername"] as? String ?: "Unknown"
                    Text("- $toUsername", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Received requests",
                style = MaterialTheme.typography.titleSmall
            )
            if (receivedRequests.isEmpty()) {
                Text("None", style = MaterialTheme.typography.bodySmall)
            } else {
                receivedRequests.forEach { req ->
                    val fromUsername = req["fromUsername"] as? String ?: "Unknown"
                    Text("- $fromUsername", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Friends",
                style = MaterialTheme.typography.titleSmall
            )
            if (friends.isEmpty()) {
                Text("None", style = MaterialTheme.typography.bodySmall)
            } else {
                friends.forEach { fr ->
                    val ids = fr.ids as? List<*> ?: emptyList<Any>()
                    val otherId = ids.firstOrNull { it != currentUser.uid } as? String ?: "Unknown"
                    Text("- $otherId", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
