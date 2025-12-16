package com.example.drawit.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawit.ui.theme.DrawitTheme
import com.example.drawit.ui.viewmodels.auth.AuthenticationViewmodel

@Preview
@Composable
fun AuthenticationScreen(viewmodel: AuthenticationViewmodel? = null) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val isInLogin = remember { mutableStateOf(true) }

    LaunchedEffect(viewmodel) {
        viewmodel?.events?.collect { event ->
            loading.value = false
            error.value = null
            when (event) {
                is AuthenticationViewmodel.UiEvent.Loading -> {
                    loading.value = true
                }

                is AuthenticationViewmodel.UiEvent.ShowError -> {
                    error.value = event.message
                }

                is AuthenticationViewmodel.UiEvent.Success -> {
                }
            }
        }
    }

    // preview support
    DrawitTheme {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryTabRow(
                    tabs = {
                        Tab(
                            selected = isInLogin.value,
                            onClick = {
                                if (!loading.value)
                                    isInLogin.value = true
                            }
                        ) {
                            Text(
                                text = "Login",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.displaySmall
                            )
                        }

                        Tab(
                            selected = !isInLogin.value,
                            onClick = {
                                if (!loading.value)
                                    isInLogin.value = false
                            }
                        ) {
                            Text(
                                text = "Register",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    },
                    selectedTabIndex = if (isInLogin.value) 0 else 1,
                    modifier = Modifier
                        .height(40.dp),
                    containerColor = Color(0x00000000)
                )

                if ( error.value != null ) {
                    Row(
                        modifier = Modifier
                            .padding(0.dp, 16.dp, 0.dp, 8.dp)
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    8.dp, 8.dp, 8.dp, 8.dp
                                )
                            )
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding( 8.dp ),

                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Text(
                            text = error.value ?: "Unknown error occured.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium.copy( fontSize = 20.sp ),
                            modifier = Modifier
                                .padding(0.dp, 8.dp, 0.dp, 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }


                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.displaySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp, 0.dp, 8.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displaySmall
                )

                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.displaySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp, 0.dp, 8.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displaySmall
                )

                Button(
                    onClick = {
                        if (!loading.value) {
                            if (isInLogin.value) {
                                viewmodel?.onLoginClicked(email.value, password.value)
                            } else {
                                viewmodel?.onRegisterClicked(email.value, password.value)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    ),
                    enabled = !loading.value
                ) {
                    Crossfade(targetState = loading.value) { isLoading ->
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = if (isInLogin.value) "Login" else "Register",
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                }
            }
        }
    }
}