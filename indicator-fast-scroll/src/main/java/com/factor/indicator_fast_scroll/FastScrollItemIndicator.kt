package com.factor.indicator_fast_scroll

import androidx.annotation.DrawableRes

sealed class FastScrollItemIndicator {
  data class Icon(@DrawableRes val iconRes: Int) : FastScrollItemIndicator()
  data class Text(val text: String) : FastScrollItemIndicator()
}
