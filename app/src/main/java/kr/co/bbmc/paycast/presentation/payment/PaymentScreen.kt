package kr.co.bbmc.paycast.presentation.payment

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.ui.component.papa_red

@Preview(widthDp = 1080, heightDp = 1920)
@Composable
fun PaymentActivity() {
    // 최종 결제 금액 viewModel 에서 가져올 것
    val paymentAmount = 10000
    val paymentText = stringResource(id = R.string.msg_payment_amount, paymentAmount)
    val formattedText = paymentText.format(paymentAmount)

    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(50.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_card),
            contentDescription = "",
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth()
                .padding(top = 40.dp)
        )
        Text(
            text = stringResource(id = R.string.msg_card_payment),
            fontSize = 40.sp,
            color = Color.Black
        )
        Text(
            text = stringResource(id = R.string.msg_card_insert),
            fontSize = 40.sp,
            color = papa_red,
            modifier = Modifier
                .padding(bottom = 40.dp)
        )
        SetAnimation()
        Box(
            modifier = Modifier
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formattedText,
                fontSize = 40.sp,
                color = Color.Black
            )
        }
        Column(
            modifier = Modifier
                .padding(top = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "남은 시간",
                fontSize = 50.sp,
                color = Color.White
            )
            Text(
                text = "30",
                fontSize = 70.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.bt_back_page),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.msg_back_page),
                fontSize = 30.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun SetAnimation() {
    val imageList = listOf(
        R.drawable.card00, R.drawable.card01, R.drawable.card02, R.drawable.card03,
        R.drawable.card04, R.drawable.card05, R.drawable.card06, R.drawable.card07,
        R.drawable.card08, R.drawable.card09, R.drawable.card10, R.drawable.card11,
        R.drawable.card12, R.drawable.card13, R.drawable.card14, R.drawable.card15,
        R.drawable.card16, R.drawable.card17, R.drawable.card18, R.drawable.card19,
        R.drawable.card20, R.drawable.card21, R.drawable.card22, R.drawable.card23,
        R.drawable.card24
    )
    val imageIdx = remember { Animatable(initialValue = 0F) }
    val currentImage = imageList[imageIdx.value.toInt()]

    LaunchedEffect(Unit) {
        imageIdx.animateTo(
            targetValue = imageList.lastIndex.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Image(
        painter = painterResource(currentImage),
        contentDescription = "",
        modifier = Modifier
            .size(400.dp)
            .clickable {}
    )
}