package kr.co.bbmc.paycast.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import com.orhanobut.logger.Logger
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.presentation.mainMenu.MainMenuViewModel

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(FlowPreview::class)
@Preview
@Composable
fun CustomLockScreen(
    vm: MainMenuViewModel
) {
    val password = vm.inputPassword.collectAsState()
    val wrongPw = remember { mutableStateOf(false) }
    val passwordVisibility = remember { mutableStateOf(false) }
    val checkPw = vm.rightPassword.asFlow().collectAsState("").value

    Box(
        modifier = Modifier
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
                .clickable { }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.str_lock_code),
                        fontSize = 30.sp,
                        color = Color.Black
                    )
                    if (wrongPw.value) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "비밀번호가 올바르지 않습니다.",
                            fontSize = 25.sp,
                            color = Color.Red
                        )
                    }
                }
                Divider()
                TextField(
                    value = password.value,
                    onValueChange = {
                        vm.saveInputPassword(it)
                        vm.passwordMatch(it.text)
                        Logger.e("Password : ${it.text}")
                    },
                    placeholder = {
                        Text(
                            text = "비밀번호 4자리를 입력해 주세요.",
                            fontSize = 25.sp,
                            color = Color.Black
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        textColor = Color.Black,
                        placeholderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    visualTransformation =
                    if (passwordVisibility.value) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisibility.value = !passwordVisibility.value }
                        ) {
                            if (passwordVisibility.value) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_lock_open_24), "",
                                    tint = Color.Black, modifier = Modifier.size(30.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Lock, "",
                                    tint = Color.Black, modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (checkPw == true) {
                                vm.enterLockScreen(false)
                                vm.showSettingDlg(true)
                            } else {
                                wrongPw.value = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = custom_yellow),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_confirm),
                            fontSize = 30.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = { vm.enterLockScreen(false) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = custom_yellow),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_cancel),
                            fontSize = 30.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}