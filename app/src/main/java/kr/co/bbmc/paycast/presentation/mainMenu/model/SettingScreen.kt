package kr.co.bbmc.paycast.presentation.mainMenu.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.presentation.mainMenu.MainMenuViewModel

@Preview(widthDp = 1080, heightDp = 1920)
@Composable
fun SettingScreen() {
    val cancelNum = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.str_closing_settlement_app),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        Divider(
            color = colorResource(id = R.color.grey),
            thickness = 1.dp
        )
        Text(
            text = stringResource(id = R.string.str_order_cancel),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
        )
        TextField(
            value = cancelNum.value,
            onValueChange = { cancelNum.value = it },
            placeholder = {
                Text(
                    text = "주문 취소번호 4자리를 입력해 주세요.",
                    fontSize = 30.sp
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = Color.Black,
                placeholderColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Row(
            Modifier
                .padding(8.dp)
        ) {
            Button(
                onClick = {},
                Modifier
                    .fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    colorResource(id = R.color.red)
                ),
                content = {
                    Text(
                        text = "CANCEL",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                shape = MaterialTheme.shapes.large
            )
            Button(
                onClick = {},
                Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    colorResource(id = R.color.orange500transparent)
                ),
                content = {
                    Text(
                        text = stringResource(id = R.string.str_order_cancel),
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                },
                shape = MaterialTheme.shapes.large
            )
        }
        Text(
            text = stringResource(id = R.string.str_server_host_setting),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
        )
        Divider(
            color = colorResource(id = R.color.grey),
            thickness = 1.dp
        )
        Text(
            text = stringResource(id = R.string.str_change_lock_code),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
        )
        Divider(
            color = colorResource(id = R.color.grey),
            thickness = 1.dp
        )
        Text(
            text = stringResource(id = R.string.str_main_print_setting),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
        )
        Divider(
            color = colorResource(id = R.color.grey),
            thickness = 1.dp
        )
        Text(
            text = stringResource(id = R.string.str_quit_app),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
        )
    }
}
