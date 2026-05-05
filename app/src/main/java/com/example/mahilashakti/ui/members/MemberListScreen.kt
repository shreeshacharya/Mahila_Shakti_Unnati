package com.example.mahilashakti.ui.members

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mahilashakti.R
import com.example.mahilashakti.data.entity.Member
import com.example.mahilashakti.utils.FileUtils
import kotlinx.coroutines.launch

enum class MemberFilter {
    ALL, DEFAULTERS, RECOVERIES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    viewModel: MemberViewModel = hiltViewModel(),
    onMemberClick: (Long) -> Unit
) {
    val members by viewModel.members.collectAsState()
    val totalSavings by viewModel.totalGroupSavings.collectAsState()
    val weeklySavings by viewModel.weeklySavings.collectAsState()
    val unpaidLoanMemberIds by viewModel.unpaidLoanMemberIds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var currentFilter by remember { mutableStateOf(MemberFilter.ALL) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.5f),
                drawerContainerColor = Color.White,
                drawerShape = RectangleShape
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = CircleShape,
                        color = Color(0xFFFDF7FD),
                        border = BorderStroke(1.dp, Color(0xFF8E248D).copy(alpha = 0.1f))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mahilashakti),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Admin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E248D)
                    )
                    Text(
                        "Mahila-Shakti Unnati",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFF8E248D).copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    label = { Text("All Members", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    selected = currentFilter == MemberFilter.ALL,
                    onClick = {
                        currentFilter = MemberFilter.ALL
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    shape = RectangleShape,
                    modifier = Modifier.padding(vertical = 1.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF8E248D).copy(alpha = 0.1f),
                        selectedIconColor = Color(0xFF8E248D),
                        selectedTextColor = Color(0xFF8E248D),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationDrawerItem(
                    label = { Text("Defaulters", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    selected = currentFilter == MemberFilter.DEFAULTERS,
                    onClick = {
                        currentFilter = MemberFilter.DEFAULTERS
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    shape = RectangleShape,
                    modifier = Modifier.padding(vertical = 1.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Red.copy(alpha = 0.1f),
                        selectedIconColor = Color.Red,
                        selectedTextColor = Color.Red,
                        unselectedIconColor = Color.Red.copy(alpha = 0.6f),
                        unselectedTextColor = Color.Red.copy(alpha = 0.6f)
                    )
                )
                NavigationDrawerItem(
                    label = { Text("Loan Recoveries", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    selected = currentFilter == MemberFilter.RECOVERIES,
                    onClick = {
                        currentFilter = MemberFilter.RECOVERIES
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    shape = RectangleShape,
                    modifier = Modifier.padding(vertical = 1.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF8E248D).copy(alpha = 0.1f),
                        selectedIconColor = Color(0xFF8E248D),
                        selectedTextColor = Color(0xFF8E248D),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.mahilashakti),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Mahila-Shakti", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF8E248D),
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF8E248D),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Member", modifier = Modifier.size(28.dp))
                }
            }
        ) { padding ->
            if (showAddDialog) {
                AddMemberDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, phone, photoUri ->
                        viewModel.addMember(name, phone, photoUri)
                        showAddDialog = false
                    }
                )
            }

            if (memberToDelete != null) {
                AlertDialog(
                    onDismissRequest = { memberToDelete = null },
                    title = { Text("Delete Member") },
                    text = { Text("Are you sure you want to delete ${memberToDelete?.name}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            memberToDelete?.let { viewModel.deleteMember(it) }
                            memberToDelete = null
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { memberToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF7FD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Group Savings", fontSize = 16.sp, color = Color(0xFF8E248D))
                        Text("₹${String.format("%.2f", totalSavings)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8E248D))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search members...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E248D)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF8E248D),
                        unfocusedBorderColor = Color(0xFF8E248D).copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val titleText = when(currentFilter) {
                        MemberFilter.ALL -> "Members"
                        MemberFilter.DEFAULTERS -> "Defaulters"
                        MemberFilter.RECOVERIES -> "Loan Recoveries"
                    }
                    Text(titleText, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E248D))
                    Surface(
                        color = Color(0xFF8E248D).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Weekly Target: ₹150", 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8E248D)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                val filteredMembers = members.filter { member ->
                    val matchesSearch = member.name.startsWith(searchQuery, ignoreCase = true)
                    val matchesFilter = when(currentFilter) {
                        MemberFilter.ALL -> true
                        MemberFilter.DEFAULTERS -> {
                            val currentTotal = weeklySavings
                                .filter { it.memberId == member.id }
                                .sumOf { it.amount }
                            currentTotal < 150.0
                        }
                        MemberFilter.RECOVERIES -> {
                            unpaidLoanMemberIds.contains(member.id)
                        }
                    }
                    matchesSearch && matchesFilter
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
                            onDelete = { memberToDelete = member }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            uri?.let {
                val savedUri = FileUtils.saveImageToInternalStorage(context, it)
                selectedImageUri = savedUri
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Member", fontWeight = FontWeight.Bold, color = Color(0xFF8E248D)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDF7FD))
                        .border(2.dp, Color(0xFF8E248D).copy(alpha = 0.2f), CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Color(0xFF8E248D),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (Compulsory)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 10) phone = it },
                    label = { Text("Phone Number (Compulsory - 10 digits)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone, selectedImageUri) },
                enabled = name.isNotBlank() && phone.length == 10,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E248D))
            ) {
                Text("Add Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun MemberItem(
    member: Member,
    isPaid: Boolean,
    currentWeeklyAmount: Double,
    onMemberClick: (Long) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onMemberClick(member.id) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = member.photoUri,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(id = R.drawable.ic_launcher_foreground)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Saved: ₹$currentWeeklyAmount", 
                    fontSize = 14.sp, 
                    color = Color.Gray
                )
            }
            if (isPaid) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = Color.Green)
            } else {
                Icon(Icons.Default.Error, contentDescription = "Not Paid", tint = Color.Red)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}
