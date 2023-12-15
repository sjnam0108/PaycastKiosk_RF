@file:OptIn(FlowPreview::class)

package kr.co.bbmc.paycast.presentation.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.noticeSharedFlow
import kr.co.bbmc.paycast.presentation.dialog.model.DlgInfo
import kr.co.bbmc.paycast.ui.component.CustomDialog
import kr.co.bbmc.paycast.ui.component.theme.ButtonType

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val showDialog = viewModel.showDlg.asFlow().collectAsState(false).value
    val dlgInfo = viewModel.dlgInfo.asFlow().collectAsState(initial = DlgInfo()).value

    if(showDialog) {
        with(dlgInfo) {
            CustomDialog(
                dlgTitle = contentTitle ?: "Title",
                dlgContent = contents  ?: "기기인증에 실패하였습니다.",
                dlgOnPositive = positiveCallback ?: {viewModel.showDialog(false)},
                dlgOnNegative = negativeCallback ?: {viewModel.showDialog(false)},
                buttonType = type,
                modifier = Modifier.fillMaxWidth(),
                useIcon = iconResource
            )
        }
    }

    PayCastMain()
    PayCastLogo(viewModel)
}

@Composable
fun PayCastMain() {
    val notice = noticeSharedFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = notice.value,
            fontSize = 52.sp,
            color = Color.White,
            modifier = Modifier
                .padding(12.dp)
                .height(80.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PayCastLogo(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_error_w),
            contentDescription = "",
            modifier = Modifier
                .combinedClickable(
                    enabled = true,
                    onClickLabel = "Clickable Image",
                    onClick = {
                        //Nothing
                    },
                    onLongClick = {
                        viewModel.showDialog(true)
                        viewModel.setDlgInfo(
                            DlgInfo(
                                type = ButtonType.Single,
                                contentTitle = "테스트",
                                contents = "가나다라마바사아자차카타파하",
                                positiveCallback = {
                                    viewModel.showDialog(false)
                                },
                                negativeCallback = {
                                    viewModel.showDialog(false)
                                },
                                iconResource = R.drawable.icon_no_order
                            )
                        )
                    }
                )
                .size(100.dp)
                .padding(end = 8.dp)
        )
    }
}
