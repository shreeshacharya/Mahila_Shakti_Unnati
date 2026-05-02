package com.example.mahilashakti.ui.members

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mahilashakti.R
import com.example.mahilashakti.data.entity.Member
import com.example.mahilashakti.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    viewModel: MemberViewModel = hiltViewModel(),
    onMemberClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val totalSavings by viewModel.totalGroupSavings.collectAsState()
    val weeklySavings by viewModel.weeklySavings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var memberToConfirmPayment by remember { mutableStateOf<Member?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mahila-Shakti Unnati") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            var isHovered by remember { mutableStateOf(false) }
            val fabScale by animateFloatAsState(
                if (isHovered) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "fabScale"
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .scale(fabScale)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Enter) isHovered = true
                                if (event.type == PointerEventType.Exit) isHovered = false
                            }
                        }
                    }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Group Savings", fontSize = 16.sp)
                    Text("₹${String.format("%.2f", totalSavings)}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search members...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Members", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Weekly Target: ₹150", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            val filteredMembers = members.filter {
                it.name.startsWith(searchQuery, ignoreCase = true)
            }

            LazyColumn {
                items(filteredMembers) { member ->
                    val currentTotal = weeklySavings
                        .filter { it.memberId == member.id }
                        .sumOf { it.amount }
                    
                    val isPaid = currentTotal >= 150.0

                    MemberItem(
                        member = member, 
                        isPaid = isPaid,
                        currentWeeklyAmount = currentTotal,
                        onMemberClick = onMemberClick,
                        onDelete = { viewModel.deleteMember(member) },
                        onPaidChange = { paid ->
                            if (paid) {
                                if (!isPaid) {
                                    memberToConfirmPayment = member
                                }
                            } else {
                                viewModel.toggleWeeklySavings(member.id, false)
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddMemberDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, photoUri ->
                    val savedUri = photoUri?.let {
                        FileUtils.saveImageToInternalStorage(context, it)
                    }
                    viewModel.addMember(name, phone, savedUri)
                    showAddDialog = false
                }
            )
        }

        memberToConfirmPayment?.let { member ->
            val currentTotal = weeklySavings
                .filter { it.memberId == member.id }
                .sumOf { it.amount }
            val needed = (150.0 - currentTotal).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { memberToConfirmPayment = null },
                title = { Text("Confirm Weekly Payment") },
                text = { 
                    if (currentTotal > 0) {
                        Text("${member.name} has already paid ₹${String.format("%.2f", currentTotal)}. Add remaining ₹${String.format("%.2f", needed)} to reach the ₹150 goal?")
                    } else {
                        Text("Has ${member.name} paid the weekly amount of ₹150?")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.toggleWeeklySavings(member.id, true, 150.0)
                        memberToConfirmPayment = null
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToConfirmPayment = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MemberItem(
    member: Member, 
    isPaid: Boolean,
    currentWeeklyAmount: Double,
    onMemberClick: (Long) -> Unit,
    onDelete: () -> Unit,
    onPaidChange: (Boolean) -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isHovered) 1.08f else 1.0f, 
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "itemScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        label = "itemBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Enter) isHovered = true
                        if (event.type == PointerEventType.Exit) isHovered = false
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMemberClick(member.id) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (member.photoUri != null) {
                AsyncImage(
                    model = member.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.shakthi),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (!member.phoneNumber.isNullOrBlank()) {
                    Text(member.phoneNumber, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Text(
                    text = "Weekly: ₹${String.format("%.2f", currentWeeklyAmount)} / ₹150",
                    fontSize = 12.sp,
                    color = if (isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = isPaid,
                    onCheckedChange = onPaidChange,
                    modifier = Modifier.graphicsLayer(scaleX = 0.8f, scaleY = 0.8f)
                )
                Text(
                    text = if (isPaid) "Paid" else "Pending",
                    fontSize = 10.sp,
                    color = if (isPaid) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Member",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onAdd: (String, String, Uri?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        photoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Member") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Photo Picker
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { 
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add Photo", fontSize = 10.sp)
                        }
                    }
                }
                Text("(Optional)", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                
                Spacer(modifier = Modifier.height(16.dp))

                RobustDialogHoverField(value = name, onValueChange = { name = it }, label = "Name")
                Spacer(modifier = Modifier.height(12.dp))
                RobustDialogHoverField(
                    value = phone, 
                    onValueChange = { 
                        if (it.length <= 10) {
                            phone = it
                            phoneError = null
                        }
                    }, 
                    label = "Phone Number",
                    isError = phoneError != null,
                    supportingText = if (phoneError != null) phoneError else "${phone.length}/10"
                )
            }
        },
        confirmButton = {
            var isHovered by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(if (isHovered) 1.1f else 1.0f, label = "btnScale")
            
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (phone.length == 10) {
                            onAdd(name, phone, photoUri)
                        } else {
                            phoneError = "Phone number must be exactly 10 digits"
                        }
                    }
                },
                modifier = Modifier
                    .scale(scale)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Enter) isHovered = true
                                if (event.type == PointerEventType.Exit) isHovered = false
                            }
                        }
                    }
            ) {
                Text("Add Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RobustDialogHoverField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.08f else 1.0f, label = "fieldScale")
    val bgColor by animateColorAsState(if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent, label = "fieldBg")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(bgColor, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Enter) isHovered = true
                        if (event.type == PointerEventType.Exit) isHovered = false
                    }
                }
            }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
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
