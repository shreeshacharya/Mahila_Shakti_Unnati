package com.example.mahilashakti.ui.memberdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
                .padding(16.dp)
        ) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons with Hover Expansion
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                HoverButton(onClick = { showAddSavings = true }, text = "Add Savings")
                HoverButton(onClick = { showRequestLoan = true }, text = "Request Loan")
            }

            if (loanError != null) {
                Text(text = loanError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                Button(onClick = { viewModel.clearLoanError() }) { Text("Dismiss Error") }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Active Loans", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            val activeLoans = memberWithLoans?.loans?.filter { !it.isPaid } ?: emptyList()
            if (activeLoans.isEmpty()) {
                Text("No active loans.", modifier = Modifier.padding(vertical = 8.dp))
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
fun HoverButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.scale(scale).hoverable(interactionSource),
        interactionSource = interactionSource
    ) {
        Text(text)
    }
}

@Composable
fun LoanItem(loan: Loan, onRepay: (Double) -> Unit) {
    var showRepayDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1.0f, label = "loanScale")

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Principal: ₹${loan.amount} @ ${loan.interestRate}% for ${loan.durationMonths}m")
            Text("Remaining Balance: ₹${String.format("%.2f", loan.remainingBalance)}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            HoverButton(onClick = { showRepayDialog = true }, text = "Repay Installment")
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
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(if (isHovered) 1.08f else 1.0f, label = "fieldScale")
    val bgColor by animateColorAsState(if (isHovered) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, label = "fieldBg")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().scale(scale).hoverable(interactionSource),
                interactionSource = interactionSource,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = bgColor,
                    focusedContainerColor = bgColor
                )
            )
        },
        confirmButton = {
            HoverButton(onClick = {
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
                HoverTextField(value = amountStr, onValueChange = { amountStr = it }, label = "Principal Amount (₹)")
                Spacer(modifier = Modifier.height(12.dp))
                HoverTextField(value = rateStr, onValueChange = { rateStr = it }, label = "Interest Rate (% per year)")
                Spacer(modifier = Modifier.height(12.dp))
                HoverTextField(value = durationStr, onValueChange = { durationStr = it }, label = "Duration (Months)")
            }
        },
        confirmButton = {
            HoverButton(onClick = {
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
fun HoverTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (isHovered) 1.08f else 1.0f, label = "hScale")
    val bgColor by animateColorAsState(if (isHovered) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, label = "hBg")

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().scale(scale).hoverable(interactionSource),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = bgColor,
            focusedContainerColor = bgColor
        )
    )
}
