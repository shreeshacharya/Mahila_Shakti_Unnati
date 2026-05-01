package com.example.mahilashakti.utils

object MathUtils {

    /**
     * Calculates Simple Interest.
     * Formula: SI = (P * R * T) / 100
     * @param principal The loan amount
     * @param ratePerAnnum The interest rate per year in percentage
     * @param durationMonths The duration of the loan in months
     * @return The calculated simple interest
     */
    fun calculateSimpleInterest(principal: Double, ratePerAnnum: Double, durationMonths: Int): Double {
        val timeInYears = durationMonths / 12.0
        return (principal * ratePerAnnum * timeInYears) / 100.0
    }

    /**
     * Calculates total payable amount (Principal + Interest)
     */
    fun calculateTotalPayable(principal: Double, ratePerAnnum: Double, durationMonths: Int): Double {
        val interest = calculateSimpleInterest(principal, ratePerAnnum, durationMonths)
        return principal + interest
    }
}
