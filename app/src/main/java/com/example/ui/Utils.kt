package com.example.ui

import java.text.NumberFormat
import java.util.Locale

object Utils {
    // Formats a Double value into Indonesian Rupiah (Rp 1.500.000)
    fun formatRupiah(value: Double): String {
        return try {
            val localeId = Locale("id", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeId)
            formatter.maximumFractionDigits = 0
            val formatted = formatter.format(value)
            // Cleanup standard Java currency formatting variations for Indonesian Locale
            formatted.replace("Rp", "Rp ").replace("IDR", "Rp ").trim()
        } catch (e: Exception) {
            "Rp " + String.format("%,d", value.toLong()).replace(',', '.')
        }
    }

    // Formats a Double value into Indonesian Rupiah short version (e.g., Rp 1.5 Jt)
    fun formatRupiahShort(value: Double): String {
        return when {
            value >= 1_000_000 -> {
                val millions = value / 1_000_000.0
                String.format(Locale("id", "ID"), "Rp %.1f Jt", millions).replace(".0", "")
            }
            value >= 1_000 -> {
                val thousands = value / 1_000.0
                String.format(Locale("id", "ID"), "Rp %.0f K", thousands)
            }
            else -> "Rp ${value.toLong()}"
        }
    }

    // Safely parse user text input into a Double
    fun parseDouble(input: String): Double {
        if (input.isBlank()) return 0.0
        val sanitized = input.replace(Regex("[^0-9.]"), "")
        return sanitized.toDoubleOrNull() ?: 0.0
    }

    // Formats a Double with up to 2 decimal places, removing trailing zeros
    fun formatDouble(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    // Safely parse user text input into an Int
    fun parseInt(input: String): Int {
        if (input.isBlank()) return 0
        val sanitized = input.replace(Regex("[^0-9]"), "")
        return sanitized.toIntOrNull() ?: 0
    }
}
