package com.example.mahilashakti.ui.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mahilashakti.R

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val usernameInteractionSource = remember { MutableInteractionSource() }
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val loginButtonInteractionSource = remember { MutableInteractionSource() }

    val isUsernameHovered by usernameInteractionSource.collectIsHoveredAsState()
    val isPasswordHovered by passwordInteractionSource.collectIsHoveredAsState()
    val isLoginButtonHovered by loginButtonInteractionSource.collectIsHoveredAsState()

    // Highly noticeable 10% expansion
    val usernameScale by animateFloatAsState(if (isUsernameHovered) 1.1f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "uScale")
    val passwordScale by animateFloatAsState(if (isPasswordHovered) 1.1f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pScale")
    val buttonScale by animateFloatAsState(if (isLoginButtonHovered) 1.1f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "bScale")

    // Distinct background highlight
    val hoverColor = MaterialTheme.colorScheme.primaryContainer
    val usernameBg by animateColorAsState(if (isUsernameHovered) hoverColor else Color.Transparent, label = "uBg")
    val passwordBg by animateColorAsState(if (isPasswordHovered) hoverColor else Color.Transparent, label = "pBg")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mahila),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Mahila-Shakti Admin Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().scale(usernameScale).hoverable(usernameInteractionSource),
            interactionSource = usernameInteractionSource,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = usernameBg,
                focusedContainerColor = usernameBg,
                unfocusedBorderColor = if (isUsernameHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().scale(passwordScale).hoverable(passwordInteractionSource),
            interactionSource = passwordInteractionSource,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = passwordBg,
                focusedContainerColor = passwordBg,
                unfocusedBorderColor = if (isPasswordHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        )

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (username == "admin" && password == "admin123") onLoginSuccess() else error = "Invalid username or password" },
            modifier = Modifier.fillMaxWidth().scale(buttonScale).hoverable(loginButtonInteractionSource),
            interactionSource = loginButtonInteractionSource,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
        }
    }
}
