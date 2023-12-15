package kr.co.bbmc.paycast.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kr.co.bbmc.paycast.R

@Preview
@Composable
fun CustomCancelOrderDialogScreen() {
    Dialog(
        onDismissRequest = {}
    ) {
        DialogContent()
    }
}

@Composable
fun DialogContent() {
    //val textValue = remember { mutableStateOf(TextFiledValue()) }

    Box(
        modifier = Modifier
            .background(color = Color.White)
            .height(160.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.str_order_number),
                fontSize = 12.sp,
                color = Color.Black
            )
            Divider(color = Color.LightGray)
            Text(
                text = "Payment",
                fontSize = 12.sp,
                color = Color.Black
            )
            Divider(color = Color.LightGray)
            Text(
                text = stringResource(id = R.string.str_approval_number),
                fontSize = 12.sp,
                color = Color.Black
            )
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.str_cancel_order_confirm),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Divider(
                    color = Color.Black,
                    thickness = 0.5.dp
                )
            }
            Row() {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth(0.5f),
                    colors = ButtonDefaults.buttonColors(
                        colorResource(id = R.color.red)
                    ),
                    content = {
                        Text(
                            text = stringResource(id = R.string.str_order_cancellation),
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    },
                    shape = MaterialTheme.shapes.large
                )
                Button(
                    onClick = {},
                    Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        colorResource(id = R.color.orange500transparent)
                    ),
                    content = {
                        Text(
                            text = stringResource(id = R.string.str_confirm),
                            fontSize = 8.sp,
                            color = Color.Black
                        )
                    },
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    }
}
