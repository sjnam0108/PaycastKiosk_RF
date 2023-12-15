package kr.co.bbmc.paycast.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.bbmc.paycast.util.getDecimalFormat


@Preview
@ExperimentalFoundationApi
@Composable
fun MenuWithPriceText(title: String? = "메뉴", price: String? = "0") {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title ?: "이름 없는 메뉴",
            fontSize = 25.sp,
            color = Color.Cyan,
            modifier = Modifier
                .weight(0.7f)
                .height(45.dp)
                .basicMarquee()
                .padding(end = 16.dp)
        )
        val priceText = runCatching { price?.toInt() ?: 0 }.getOrDefault(0)
        Text(
            text = "${getDecimalFormat(priceText)} 원",
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier
                .weight(0.3f)
                .height(45.dp),
            textAlign = TextAlign.End
        )
    }
}

