package com.example.mahilashakti.ui.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.shakthi),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mahila-Shakti Unnati",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        HoverInputField(
            value = username,
            onValueChange = { username = it; error = null },
            label = "Username"
        )

        Spacer(modifier = Modifier.height(16.dp))

        HoverInputField(
            value = password,
            onValueChange = { password = it; error = null },
            label = "Password",
            isPassword = true
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        var isBtnHovered by remember { mutableStateOf(false) }
        val btnScale by animateFloatAsState(
            targetValue = if (isBtnHovered) 1.1f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "btnScale"
        )
        
        Button(
            onClick = {
                if (username == "admin" && password == "admin123") {
                    onLoginSuccess()
                } else {
                    error = "Invalid username or password"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .scale(btnScale)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            when (event.type) {
                                PointerEventType.Enter -> isBtnHovered = true
                                PointerEventType.Exit -> isBtnHovered = false
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Login", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun HoverInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.08f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "fieldScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        label = "fieldBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(bgColor, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedBorderColor = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        )
    }
}
