package com.android.developers.androidify.creation

import androidx.compose.ui.graphics.Color

data class BotColor(
    val name: String,
    val value: String,
    val imageRes: Int? = null,
    val color: Color? = null,
) {
    fun getVerboseDescription(): String {
        return "$name ($value)"
    }
}

val DEFAULT_BOT_COLORS = listOf(
    BotColor("Android Green", "#50C168", color = Color(0xFF50C168)),
    BotColor("Light Almond", "#F1DFD4", color = Color(0xFFF1DFD4)),
    BotColor("Light Champagne", "#F3E0CF", color = Color(0xFFF3E0CF)),
    BotColor("Wheat", "#F2DBBB", color = Color(0xFFF2DBBB)),
    BotColor("Birch Beige", "#DABE9B", color = Color(0xFFDABE9B)),
    BotColor("Tan", "#BD9A71", color = Color(0xFFBD9A71)),
    BotColor("Coyote Brown", "#8A633F", color = Color(0xFF8A633F)),
    BotColor("Chocolate", "#784C38", color = Color(0xFF784C38)),
    BotColor("Syrup Brown", "#633A2E", color = Color(0xFF633A2E)),
    BotColor("Espresso", "#45332D", color = Color(0xFF45332D)),
    BotColor("Black Brown", "#2C2523", color = Color(0xFF2C2523)),
    BotColor("Hot Pink", "#DB79D7", color = Color(0xFFDB79D7)),
    BotColor("Ultra Purple", "#9C6CD5", color = Color(0xFF9C6CD5)),
    BotColor("Honey Yellow", "#E2C96C", color = Color(0xFFE2C96C)),
    BotColor("Light Pink", "#E0BFC3", color = Color(0xFFE0BFC3)),
    BotColor("Flame Orange", "#DB774A", color = Color(0xFFDB774A)),
    BotColor("Tangerine", "#DC944F", color = Color(0xFFDC944F)),
    BotColor("Ocean Blue", "#5090D5", color = Color(0xFF5090D5)),
    BotColor("Cloud Gray", "#CBCBCB", color = Color(0xFFCBCBCB)),
)