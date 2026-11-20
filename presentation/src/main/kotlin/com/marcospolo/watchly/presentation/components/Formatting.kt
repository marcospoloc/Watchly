package com.marcospolo.watchly.presentation.components

import java.util.Locale

/** Formats a 0-10 rating with one decimal using the user's locale ("8.5" / "8,5"). */
fun formatRating(rating: Double, locale: Locale = Locale.getDefault()): String = String.format(locale, "%.1f", rating)
