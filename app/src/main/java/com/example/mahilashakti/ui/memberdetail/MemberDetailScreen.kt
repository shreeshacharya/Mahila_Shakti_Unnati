package com.example.mahilashakti.ui.memberdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mahilashakti.R
import com.example.mahilashakti.data.entity.Loan
import com.example.mahilashakti.utils.IntentUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: Long,
    onBack: () -> Unit,
    viewModel: MemberDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val memberWithSavings by viewModel.memberWithSavings.collectAsState()
    val memberWithLoans by viewModel.memberWithLoans.collectAsState()
    val loanError by viewModel.loanError.collectAsState()

    var showAddSavings by remember { mutableStateOf(false) }
    var showRequestLoan by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(memberWithSavings?.member?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val totalSavings = memberWithSavings?.savings?.sumOf { it.amount } ?: 0.0
                        val unpaidLoans = memberWithLoans?.loans?.filter { !it.isPaid }?.sumOf { it.remainingBalance } ?: 0.0
                        val summary = IntentUtils.generateMemberSummary(
                            memberName = memberWithSavings?.member?.name ?: "Unknown",
                            totalSavings = totalSavings,
                            unpaidLoansBalance = unpaidLoans
                        )
                        IntentUtils.shareViaWhatsApp(context, summary)
                    }) {
                        Icon(Icons.Default.Share, "Share via WhatsApp")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Member Photo
            if (memberWithSavings?.member?.photoUri != null) {
                AsyncImage(
                    model = memberWithSavings?.member?.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.shakthi),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = memberWithSavings?.member?.name ?: "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            memberWithSavings?.member?.phoneNumber?.let {
                Text(text = it, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val totalSavings = memberWithSavings?.savings?.sumOf { it.amount } ?: 0.0
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Card(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Savings")
                        Text("₹${String.format("%.2f", totalSavings)}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                val unpaidLoanBalance = memberWithLoans?.loans?.filter { !it.isPaid }?.sumOf { it.remainingBalance } ?: 0.0
                Card(modifier = Modifier.weight(1f).padding(start = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Unpaid Loans")
                        Text("₹${String.format("%.2f", unpaidLoanBalance)}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons with working hover
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RobustHoverButton(onClick = { showAddSavings = true }, text = "Add Savings")
                RobustHoverButton(onClick = { showRequestLoan = true }, text = "Request Loan")
            }

            if (loanError != null) {
                Text(text = loanError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                Button(onClick = { viewModel.clearLoanError() }) { Text("Dismiss Error") }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Active Loans", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            
            val activeLoans = memberWithLoans?.loans?.filter { !it.isPaid } ?: emptyList()
            if (activeLoans.isEmpty()) {
                Text("No active loans.", modifier = Modifier.padding(vertical = 8.dp).align(Alignment.Start))
            } else {
                LazyColumn {
                    items(activeLoans) { loan ->
                        LoanItem(loan, onRepay = { amount -> viewModel.repayLoan(loan, amount) })
                    }
                }
            }
        }

        if (showAddSavings) {
            AmountDialog(
                title = "Add Savings",
                onDismiss = { showAddSavings = false },
                onConfirm = { amount ->
                    viewModel.addSavings(amount)
                    showAddSavings = false
                }
            )
        }

        if (showRequestLoan) {
            RequestLoanDialog(
                onDismiss = { showRequestLoan = false },
                onRequest = { amount, rate, duration ->
                    viewModel.requestLoan(amount, rate, duration)
                    showRequestLoan = false
                }
            )
        }
    }
}

@Composable
fun RobustHoverButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isHovered) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
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
        Text(text, modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
    }
}

@Composable
fun LoanItem(loan: Loan, onRepay: (Double) -> Unit) {
    var showRepayDialog by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1.0f)

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
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Principal: ₹${loan.amount} @ ${loan.interestRate}% for ${loan.durationMonths}m")
            Text("Remaining Balance: ₹${String.format("%.2f", loan.remainingBalance)}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            RobustHoverButton(onClick = { showRepayDialog = true }, text = "Repay Installment")
        }
    }

    if (showRepayDialog) {
        AmountDialog(
            title = "Repay Loan",
            onDismiss = { showRepayDialog = false },
            onConfirm = { amount ->
                onRepay(amount)
                showRepayDialog = false
            }
        )
    }
}

@Composable
fun AmountDialog(title: String, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            RobustHoverTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = "Amount (₹)",
                isNumber = true
            )
        },
        confirmButton = {
            RobustHoverButton(onClick = {
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) onConfirm(amount)
            }, text = "Confirm")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RequestLoanDialog(onDismiss: () -> Unit, onRequest: (Double, Double, Int) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    var rateStr by remember { mutableStateOf("12.0") }
    var durationStr by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Loan") },
        text = {
            Column {
                RobustHoverTextField(value = amountStr, onValueChange = { amountStr = it }, label = "Principal Amount (₹)", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                RobustHoverTextField(value = rateStr, onValueChange = { rateStr = it }, label = "Interest Rate (%)", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                RobustHoverTextField(value = durationStr, onValueChange = { durationStr = it }, label = "Duration (Months)", isNumber = true)
            }
        },
        confirmButton = {
            RobustHoverButton(onClick = {
                val amount = amountStr.toDoubleOrNull()
                val rate = rateStr.toDoubleOrNull()
                val duration = durationStr.toIntOrNull()
                if (amount != null && rate != null && duration != null) onRequest(amount, rate, duration)
            }, text = "Request")
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RobustHoverTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.08f else 1.0f)
    val bgColor by animateColorAsState(if (isHovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)

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
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
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
