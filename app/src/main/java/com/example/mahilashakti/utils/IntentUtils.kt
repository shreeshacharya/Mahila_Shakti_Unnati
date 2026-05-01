package com.example.mahilashakti.utils

import android.content.Context
import android.content.Intent

object IntentUtils {

    /**
     * Shares a text message via WhatsApp if installed, or falls back to standard share sheet.
     */
    fun shareViaWhatsApp(context: Context, textToShare: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
            // Optionally, target WhatsApp package specifically:
            // setPackage("com.whatsapp")
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateMemberSummary(
        memberName: String,
        totalSavings: Double,
        unpaidLoansBalance: Double
    ): String {
        return """
            Mahila-Shakti Unnati: Member Summary
            -----------------------------------
            Name: $memberName
            Total Savings: ₹$totalSavings
            Unpaid Loan Balance: ₹$unpaidLoansBalance
            
            Generated via App.
        """.trimIndent()
    }
}
