package com.app.bemyrider.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.app.bemyrider.R

/**
 * Utility class for UI operations.
 * Optimized by Gemini - 2024.
 */
object UIUtils {

    /**
     * Returns the standard Account Circle icon as a fallback.
     */
    fun getPlaceholderDrawable(context: Context): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.account_circle_24)
    }
}
