package kr.co.bbmc.paycast.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.presentation.mainMenu.MainMenuViewModel


@OptIn(FlowPreview::class)
@Preview
@Composable
fun CustomSettingScreen(
    vm: MainMenuViewModel
) {
    val cancelCode = vm.cancelPaymentCode.collectAsState()

    Box(
        modifier = Modifier
            .clickable { }
            .fillMaxSize()
            .background(Color.Black.copy(0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(800.dp)
                .wrapContentHeight()
                .padding(16.dp)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
            ) {
                Text(
                    text = "관리자 페이지",
                    fontSize = 35.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.str_closing_settlement_app),
                    fontSize = 30.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
                Divider(color = Color.LightGray)
                Text(
                    text = stringResource(id = R.string.str_order_cancel),
                    fontSize = 30.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(4.dp)
                )
                TextField(
                    value = cancelCode.value,
                    onValueChange = {  vm.saveCancelCode(it) },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    textStyle = TextStyle.Default.copy(fontSize = 30.sp),
                    placeholder = { Text(
                        text = "주문 취소 번호 4자리를 입력해 주세요.",
                        fontSize = 30.sp,
                        color = Color.Black
                    )},
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { vm.showSettingDlg(false) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = custom_red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.Hint_Cancel),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = { vm.cancelPayment() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = custom_yellow),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_order_cancel),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Divider(color = Color.LightGray)
                Text(
                    text = stringResource(id = R.string.str_change_lock_code),
                    fontSize = 30.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable { vm.showNewPasswordScreen(true) }
                )
                Divider(color = Color.LightGray)
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable { vm.showSettingDlg(false) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.str_quit_app),
                        fontSize = 30.sp,
                        color = Color.Black
                    )
                    Icon(
                        Icons.Default.Close, "", tint = Color.Black
                    )
                }
            }
        }
    }
}