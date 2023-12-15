package kr.co.bbmc.paycast.ui.component

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orhanobut.logger.Logger
import kotlinx.coroutines.FlowPreview
import kr.co.bbmc.paycast.presentation.mainMenu.MainMenuViewModel
import kr.co.bbmc.paycast.R


@OptIn(FlowPreview::class)
@Composable
fun CustomNewLockScreen(vm: MainMenuViewModel) {
    val isSamePassword = remember { mutableStateOf(false) }
    val newPwVisibility = remember { mutableStateOf(false) }
    val chkNewPwVisibility = remember { mutableStateOf(false) }
    val isFourLength = remember { mutableStateOf(false) }
    val animateColor = remember { Animatable(Color.Transparent) }
    val newPw = vm.newPassword.collectAsState()
    val chkNewPw = vm.chkNewPassword.collectAsState()

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
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "신규 비밀번호",
                        fontSize = 30.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                    if(isFourLength.value) {
                        LaunchedEffect(Unit) {
                            repeat(3) {
                                animateColor.animateTo(Color.Red, tween(500))
                                animateColor.animateTo(Color.Black, tween(500))
                            }
                            animateColor.animateTo(Color.Transparent)
                            isFourLength.value = false
                        }
                        Text(
                            text = "4자리로 설정 해 주세요.",
                            fontSize = 30.sp,
                            color = animateColor.value,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .padding(4.dp)
                        )
                    }
                    Divider(color = Color.LightGray)
                }
                TextField(
                    value = newPw.value,
                    onValueChange = {
                        vm.saveNewPassword(it)
                        isSamePassword.value = vm.newPassword.value.text != vm.chkNewPassword.value.text
                    },
                    placeholder = {
                        Text(
                            text = "새 비밀번호 4자리를 입력해 주세요.",
                            fontSize = 30.sp,
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
                    if(newPwVisibility.value) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { newPwVisibility.value = !newPwVisibility.value}
                        ) {
                            if(newPwVisibility.value) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_lock_open_24),
                                    "",
                                    tint = Color.Black,
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Lock,
                                    "",
                                    tint = Color.Black,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = Color.LightGray)
                Text(
                    text = "신규 비밀번호 확인",
                    fontSize = 30.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
                Divider(color = Color.LightGray)
                TextField(
                    value = chkNewPw.value,
                    onValueChange = {
                        vm.saveChkNewPassword(it)
                        isSamePassword.value = vm.newPassword.value.text != vm.chkNewPassword.value.text
                    },
                    placeholder = {
                        Text(
                            text = "새 비밀번호 4자리를 다시 입력해 주세요",
                            fontSize = 30.sp,
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
                    if(chkNewPwVisibility.value) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { chkNewPwVisibility.value = !chkNewPwVisibility.value}
                        ) {
                            if(chkNewPwVisibility.value) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_lock_open_24),
                                    "",
                                    tint = Color.Black,
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Lock,
                                    "",
                                    tint = Color.Black,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                )
                if (isSamePassword.value) {
                    Text(
                        text = "비밀번호와 새 비밀번호가 일치하지 않습니다.",
                        fontSize = 30.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(modifier = Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { /* TODO : [OK] - 비밀 번호 두 개 같은 지 체크, 4자리 인지 체크, DataStore 에 해당 값 넣기 */
                            Logger.e("Pw Test : ${vm.newPassword.value.text} & ${vm.chkNewPassword.value.text}")
                            Logger.e("Pw Length Test : ${vm.newPassword.value.text.length} & ${vm.chkNewPassword.value.text.length}")
                            // 새 비밀 번호와 새 확인 비밀 번호가 길이가 4인지 확인, 새 비밀 번호와 새 확인 비밀 번호가 같은지 확인 해야 함
                            if(( vm.newPassword.value.text.length == 4 && vm.chkNewPassword.value.text.length == 4) && (
                                        vm.newPassword.value.text == vm.chkNewPassword.value.text
                                    ) ) {
                                vm.isSamePassword(vm.newPassword.value.text) // 조건에 만족 하면 데이터 스토어 에 해당 값을 넣어 줌
                                Logger.e("길이도 같고 비번도 같다.")
                                vm.showNewPasswordScreen(false) // 현재 창을 닫는다
                                vm.showSettingDlg(false) // 기존의 셋팅 다이얼 로그도 닫아야 새로운 비밀 번호로 로그인 할 수가 있음
                                vm.sendToast("비밀번호 설정이 완료되었습니다. 다시 로그인 해주세요.")
                            }
                            // 길이가 4가 아니 라면 텍스트 에 효과 주기
                            if (vm.newPassword.value.text.length != 4 || vm.chkNewPassword.value.text.length != 4) {
                                isFourLength.value = true
                            }
                            // 오류 처리에 대한 ui 작업 필요
                            // 두 비밀 번호가 같은 것 체크 는 TextField 의 onValueChange 에서 진행 하고 있음
                            else {
                                Logger.e("길이나 비번중 하나는 오류임")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = custom_yellow
                        ),
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
                        onClick = { vm.showNewPasswordScreen(false) },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = custom_yellow
                        ),
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