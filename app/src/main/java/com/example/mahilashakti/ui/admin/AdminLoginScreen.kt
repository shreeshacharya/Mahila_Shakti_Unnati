package com.example.mahilashakti.ui.admin

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the logo image prominently
            Image(
                painter = painterResource(id = R.drawable.mahilashakti),
                contentDescription = "Mahila Shakti Unnati Logo",
                modifier = Modifier
                    .size(320.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFDF7FD) // Very light purple tint
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Admin Login",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E248D) // Deep purple matching the logo
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))

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

                    Spacer(modifier = Modifier.height(32.dp))

                    var isBtnHovered by remember { mutableStateOf(false) }
                    val btnScale by animateFloatAsState(
                        targetValue = if (isBtnHovered) 1.05f else 1.0f,
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
                            .height(56.dp)
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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8E248D) // Deep purple matching the logo
                        )
                    ) {
                        Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "fieldScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
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
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color(0xFF8E248D),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                cursorColor = Color(0xFF8E248D)
            )
        )
    }
}
