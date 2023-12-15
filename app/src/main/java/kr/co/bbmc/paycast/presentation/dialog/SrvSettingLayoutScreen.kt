package kr.co.bbmc.paycast.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.databinding.SrvSettingLayoutBinding

@Preview
@Composable
fun SrvSettingLayoutScreen() {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(color = Color.White)
            .width(1000.dp)
    ) {
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
            text = stringResource(id = R.string.str_quit_app),
            fontSize = 30.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}