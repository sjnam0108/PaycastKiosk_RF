package kr.co.bbmc.paycast.presentation.provisioninfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.bbmc.paycast.R

@Preview(widthDp = 1080, heightDp = 1920)
@Composable
fun ProvisionInfoScreen(vm: ProvisionViewModel) {
    val phoneNumber = vm.phoneNumber.collectAsState()
    val agreement = vm.agreePrivacyCollection.collectAsState()
    val showAgreementScreen = remember { mutableStateOf(false)}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.str_message_noti_service_title),
                fontSize = 75.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(240.dp))
            Text(
                text = stringResource(id = R.string.str_message_noti_service_body),
                fontSize = 45.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(440.dp))
            Text(
                text = stringResource(id = R.string.str_guide_input_telnumber),
                fontSize = 45.sp,
                color = Color.White
            )
            TextField(
                value = phoneNumber.value,
                onValueChange = { vm.savePhoneNumber(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                textStyle = TextStyle.Default.copy(
                    fontSize = 45.sp,
                    color = Color.White
                )
//              keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreement.value,
                    onCheckedChange = { vm.setAgreePrivacyCollection(it) },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = Color.Black,
                        checkedColor = Color.White,
                        uncheckedColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(scale = 1.6f)
                        .width(60.dp)
                )
                Text(
                    text = stringResource(id = R.string.str_provide_personal_info),
                    fontSize = 45.sp,
                    color = Color.White,
                    modifier = Modifier.width(780.dp)
                )
                OutlinedButton(
                    onClick = { showAgreementScreen.value = true},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent
                    ),
                    border = BorderStroke(4.dp, Color.White),

                    ) {
                    Text(
                        text = stringResource(id = R.string.str_view_details),
                        fontSize = 45.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { vm.skipAction() },
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 8.dp)
                        .weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent
                    ),
                    border = BorderStroke(4.dp, Color.White)
                ) {
                    Text(
                        text = stringResource(id = R.string.str_skip_button),
                        fontSize = 45.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { vm.startPayment() },
                    modifier = Modifier
                        .padding(start = 8.dp, top = 24.dp, bottom = 24.dp, end = 24.dp)
                        .weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent
                    ),
                    border = BorderStroke(4.dp, Color.White)
                ) {
                    Text(
                        text = stringResource(id = R.string.str_confirm),
                        fontSize = 45.sp,
                        color = colorResource(id = R.color.Yellow)
                    )
                }
            }
        }
    }

    if(showAgreementScreen.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .padding(80.dp)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(top = 40.dp, bottom = 80.dp),
                        text = stringResource(id = R.string.str_providing_agreement_title),
                        fontSize = 65.sp,
                        color = Color.Black
                    )
                    Column(
                        modifier = Modifier
                            .height(1200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_providing_agreement_body),
                            fontSize = 35.sp,
                            color = Color.Black
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(vertical = 40.dp)
                            .fillMaxSize(),
                        Arrangement.End
                    ) {
                        Button(
                            onClick = { showAgreementScreen.value = false },
                            modifier = Modifier
                                .width(180.dp)
                                .height(180.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                        ) {
                            Text(
                                text = stringResource(id = R.string.str_close_dialog),
                                fontSize = 45.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}