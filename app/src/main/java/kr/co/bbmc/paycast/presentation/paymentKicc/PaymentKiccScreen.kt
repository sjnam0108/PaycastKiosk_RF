package kr.co.bbmc.paycast.presentation.paymentKicc

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.approveMoney
import kr.co.bbmc.paycast.mNumberOfOrder
import kr.co.bbmc.paycast.presentation.dialog.model.DlgInfo
import kr.co.bbmc.paycast.ui.component.CustomDialog
import kr.co.bbmc.paycast.util.getDecimalFormat
import kr.co.bbmc.paycast.waitOrderCount

@FlowPreview
@Composable
fun PaymentKiccScreen(vm: PaymentViewModel) {
    val showDialog = vm.showDlg.asFlow().collectAsState(false).value
    val dlgInfo = vm.dlgInfo.asFlow().collectAsState(initial = DlgInfo()).value
    val state by vm.viewStatus.collectAsState()

    if (showDialog) {
        with(dlgInfo) {
            CustomDialog(
                dlgTitle = contentTitle ?: "Title",
                dlgContent = contents ?: "기기인증에 실패하였습니다.",
                dlgOnPositive = positiveCallback ?: { vm.showDialog(false) },
                dlgOnNegative = negativeCallback ?: { vm.showDialog(false) },
                buttonType = type,
                modifier = Modifier.fillMaxWidth(),
                useIcon = iconResource
            )
        }
    } else {
        Box(
            modifier = Modifier
                .background(Color.Black),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val txtTitleResId = when (state) {
                    2 -> R.string.msg_complete_payment
                    else -> R.string.msg_card_payment
                }
                Image(
                    painter = painterResource(id = R.drawable.icon_card),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                )
                Text(
                    text = stringResource(id = txtTitleResId),
                    fontSize = 45.sp,
                    color = White
                )
                Spacer(modifier = Modifier.height(60.dp))
                PaymentHeader(state)
                Spacer(modifier = Modifier.height(260.dp))
                KiccAnimation(state)
            }
        }
    }
}

@Composable
fun KiccAnimation(state: Int) {
    val imageList = listOf(
        R.drawable.card00, R.drawable.card01, R.drawable.card02, R.drawable.card03,
        R.drawable.card04, R.drawable.card05, R.drawable.card06, R.drawable.card07,
        R.drawable.card08, R.drawable.card09, R.drawable.card10, R.drawable.card11,
        R.drawable.card12, R.drawable.card13, R.drawable.card14, R.drawable.card15,
        R.drawable.card16, R.drawable.card17, R.drawable.card18, R.drawable.card19,
        R.drawable.card20, R.drawable.card21, R.drawable.card22, R.drawable.card23,
        R.drawable.card24
    )
    val cardOutList = listOf(
        R.drawable.cardout00, R.drawable.cardout01, R.drawable.cardout02, R.drawable.cardout03,
        R.drawable.cardout04, R.drawable.cardout05, R.drawable.cardout06, R.drawable.cardout07,
        R.drawable.cardout08, R.drawable.cardout09, R.drawable.cardout10, R.drawable.cardout11,
        R.drawable.cardout12, R.drawable.cardout13, R.drawable.cardout14, R.drawable.cardout15,
        R.drawable.cardout16, R.drawable.cardout17, R.drawable.cardout18, R.drawable.cardout19,
        R.drawable.cardout20, R.drawable.cardout21, R.drawable.cardout22, R.drawable.cardout23,
        R.drawable.cardout24
    )
    val imageIdx = remember { Animatable(initialValue = 0F) }
    val targetList = if (state != 2) imageList else cardOutList
    val currentImage = targetList[imageIdx.value.toInt()]

    LaunchedEffect(Unit) {
        imageIdx.animateTo(
            targetValue = targetList.lastIndex.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(currentImage),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(start = 80.dp, end = 80.dp, bottom = 10.dp, top = 30.dp)
                .align(Alignment.TopCenter)
        )
        if(state == 2) {
            Text(
                text = "카드를 빼주세요.",
                textAlign = TextAlign.Center,
                fontSize = 45.sp,
                color =  colorResource(id = R.color.Orange),
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun PaymentHeader(state: Int) {
    if (state != 2) {
        val styledText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = White, fontSize = 50.sp)) {
                append("결제금액은 ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 52.sp, color = colorResource(id = R.color.Orange))) {
                append(getDecimalFormat(approveMoney.toInt()))
            }
            withStyle(style = SpanStyle(color = White, fontSize = 50.sp)) {
                append("원 입니다.")
            }
        }
        Text(
            modifier = Modifier.border(width = 2.dp, color = White, shape = RoundedCornerShape(20))
                .height(100.dp)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = styledText,
            textAlign = TextAlign.Center,
            color = White,
        )
    } else {
        Text(
            modifier = Modifier.border(width = 2.dp, color = White, shape = RoundedCornerShape(20))
                .height(100.dp)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically),
            text = "주문번호",
            fontSize = 52.sp,
            textAlign = TextAlign.Center,
            color = White,
        )
    }
    if (state != 2) {
        Spacer(modifier = Modifier.height(140.dp))
        Text(
            text = stringResource(id = R.string.msg_card_insert),
            fontSize = 40.sp,
            color = colorResource(id = R.color.Orange)
        )
    } else {
        Text(
            text = mNumberOfOrder.toString(),
            fontSize = 140.sp,
            color = colorResource(id = R.color.Yellow)
        )
        val styledText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = White, fontSize = 40.sp)) {
                append("대기자수 : ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 42.sp, color = colorResource(id = R.color.Orange))) {
                append("$waitOrderCount")
            }
            withStyle(style = SpanStyle(color = White, fontSize = 40.sp)) {
                append(" 명")
            }
        }
        Text(
            text = styledText,
            fontSize = 40.sp,
            color = colorResource(id = R.color.white)
        )
    }
}
