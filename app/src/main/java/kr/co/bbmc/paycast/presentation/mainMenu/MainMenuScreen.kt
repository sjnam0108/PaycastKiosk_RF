@file:Suppress("PreviewAnnotationInFunctionWithParameters")
@file:OptIn(FlowPreview::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)

package kr.co.bbmc.paycast.presentation.mainMenu

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.orhanobut.logger.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.data.model.MenuObject
import kr.co.bbmc.paycast.presentation.dialog.model.DlgInfo
import kr.co.bbmc.paycast.ui.component.*
import kr.co.bbmc.paycast.ui.component.theme.imageExt
import kr.co.bbmc.paycast.util.getDecimalFormat
import kr.co.bbmc.paycast.util.sumOfOptions
import kr.co.bbmc.paycast.util.withSeparatorString
import java.util.*

@FlowPreview
@Preview(widthDp = 1080, heightDp = 1920)
@Composable
fun MainMenuScreen(vm: MainMenuViewModel) {
    val showDialog = vm.showDlg.asFlow().collectAsState(false).value
    val dlgInfo = vm.dlgInfo.asFlow().collectAsState(DlgInfo()).value
    val settingDlg = vm.showSettingDlg.asFlow().collectAsState(false).value
    val enterLockScreen = vm.enterLockScreen.asFlow().collectAsState(false).value
    val showNewPasswordScreen = vm.showNewPasswordScreen.asFlow().collectAsState(false).value
    val initData by vm.init.collectAsState()
    val showOption by vm.showOption.collectAsState()
    val plusShow by vm.showPlusDlg.collectAsState()
    val plusOptShow by vm.showPlusOptDlg.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
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
        }
        if (!initData) DialogBoxLoading(textMsg = "화면을 준비중입니다...")
        StoreName(vm)
        TabPager(vm)
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            OrderScreen(vm)
            ButtonActions(vm)
        }
    }
    PlusMenuDialog(plusShow, vm)
    OptionDialog(showOption, vm)
    if(plusOptShow) PlusOptionDialog(vm)
    if (settingDlg) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CustomSettingScreen(vm)
        }
    }
    if (enterLockScreen) {
        vm.saveInputPassword(TextFieldValue())
        vm.rightPassword(false)
        CustomLockScreen(vm)
    }
    if (showNewPasswordScreen) {
        vm.saveNewPassword(TextFieldValue())
        vm.saveChkNewPassword(TextFieldValue())
        CustomNewLockScreen(vm)
    }
}

@FlowPreview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptionDialog(
    showOption: Boolean,
    viewModel: MainMenuViewModel,
) {
    if (showOption) { //isVisible 을 clickable 하면 true 로 설정해줌 ( option존재 유무에 따라서 조건문 추가 작성 필요)
        val imgPath = "/storage/emulated/0/BBMC/PAYCAST/Content/"
        val options by viewModel.optionData.collectAsState()

        val addedSelected = remember { mutableStateListOf("") } //checkBox를 눌렀을 때
        val radioSelectedList = remember { mutableStateOf(listOf<String>()) }
        val addedSelectedTest = remember { mutableStateListOf("") }

        Box(
            modifier = Modifier
                .padding(40.dp)
                .background(Color.Transparent)
                .blur(50.dp)
                .fillMaxSize()
                .clickable { }
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .background(Color.White, shape = RoundedCornerShape(10.dp))
                    .border(
                        width = (1.5).dp,
                        color = papa_darkgray,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .fillMaxSize()

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
//                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = options?.name.toString(),
                        fontSize = 65.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 40.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    val imgFile = imgPath + options.imagefile
                    Image(
                        rememberAsyncImagePainter(
                            model = imgFile,
                            placeholder = painterResource(id = R.drawable.default_menu_icon)
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 30.dp)
                            .padding(horizontal = 80.dp)
                            .height(400.dp)
                            .fillMaxWidth(),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = options.description,
                        fontSize = 45.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, top = 5.dp)
                            .fillMaxWidth()
                    )
                    val price = options.price
                    Text(
                        text = "${getDecimalFormat(price.toInt())} 원",
                        fontSize = 55.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    LazyColumn(
                        modifier = Modifier
                            .padding(30.dp)
                            .fillMaxSize()
                    ) {
                        val optionDataSize = options?.optMenusList?.count { it.menuGubun == "0" } ?: 0
                        Logger.e("optionDataSize : $optionDataSize // opt data : ${options.optMenusList}")
                        val radioValuesV2: MutableList<String> = MutableList(optionDataSize) { "" }
                        radioSelectedList.value = radioValuesV2.toList()
                        itemsIndexed(options.optMenusList) { i, it ->
                            Box(Modifier.wrapContentHeight()) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = it.menuOptName,
                                        fontSize = 50.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 40.dp, end = 20.dp,top = 20.dp)
                                    )
                                    if (it.menuGubun == "0") {
                                        LazyColumn(
                                            modifier = Modifier
                                                .height(500.dp)
                                                .fillMaxWidth()
                                        ) {
                                            val currentOptList = it.optList.flatMap { it.requiredOptList.map { menu-> menu?.optMenuName } }
                                            Logger.w("currentOptList : $currentOptList")
                                            Logger.w("radioValuesV2 : $radioValuesV2 // optSize - ${it.optList.size}")
                                            itemsIndexed(it.optList) { _, requiredOpt ->
                                                Row(
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .height(100.dp)
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            radioValuesV2[i] =
                                                                requiredOpt.requiredOptList[0]!!.optMenuName
                                                            radioSelectedList.value =
                                                                radioValuesV2.toList()
                                                            Logger.w("Selected opt list - ${radioSelectedList.value}")
                                                            viewModel.setRequiredOptions(
                                                                requiredOpt.requiredOptList[0]!!,
                                                                i
                                                            )
                                                            viewModel.countPopup()
                                                        },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = (radioSelectedList.value[i]) == requiredOpt.requiredOptList[0]!!.optMenuName,
                                                        onClick = {
                                                            radioValuesV2[i] = requiredOpt.requiredOptList[0]!!.optMenuName
                                                            radioSelectedList.value = radioValuesV2.toList()
                                                            Logger.w("Selected opt list - ${radioSelectedList.value}")
                                                            viewModel.setRequiredOptions(requiredOpt.requiredOptList[0]!!, i)
                                                            viewModel.countPopup()
                                                        },
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = custom_green,
                                                            disabledColor = Color.White,
                                                            unselectedColor = Color.Black
                                                        ),
                                                        modifier = Modifier
                                                            .scale(1.6f)
                                                            .width(60.dp)
                                                    )
                                                    Text(
                                                        text = requiredOpt.requiredOptList[0]!!.optMenuName,
                                                        fontSize = 45.sp,
                                                        color = Color.Black,
                                                        modifier = Modifier
                                                            .width(600.dp)
                                                            .basicMarquee()
                                                            .padding(start = 10.dp, end = 16.dp)
                                                    )
                                                    Text(
                                                        text = "${getDecimalFormat(requiredOpt.requiredOptList[0]!!.optMenuPrice.toInt())} 원",
                                                        fontSize = 45.sp,
                                                        color = Color.Black,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .wrapContentWidth(Alignment.End)
                                                    )
                                                }
                                            }
                                        }
                                    } else if (it.menuGubun == "1") {
                                        LazyColumn(
                                            modifier = Modifier
                                                .height(500.dp)
                                                .fillMaxWidth()
                                        ) {
                                            items(it.optList) { addedOpt ->
                                                val item = addedOpt.addOptList[0]?.optMenuName
                                                val isClicked = remember { mutableStateOf(item in addedSelectedTest) }
                                                Row(
                                                    modifier = Modifier
                                                        .padding(
                                                            start = 8.dp,
                                                            end = 8.dp,
                                                            top = 4.dp
                                                        )
                                                        .height(100.dp)
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            isClicked.value = !isClicked.value
                                                            if (isClicked.value) {
                                                                addedSelected.add(item!!)
                                                                viewModel.tempAddOptions(addedOpt.addOptList[0]!!)
                                                            } else {
                                                                addedSelected.remove(item)
                                                                viewModel.tempRemoveAddOptions(
                                                                    addedOpt.addOptList[0]!!
                                                                )
                                                            }
                                                            Logger.e("Check Box Click Test : ${addedSelected.map { it }}")
                                                            viewModel.countPopup()
                                                        },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = isClicked.value,
                                                        onCheckedChange = {
                                                            isClicked.value = it
                                                            if (it) {
                                                                addedSelected.add(item!!)
                                                                viewModel.tempAddOptions(addedOpt.addOptList[0]!!)
                                                            } else {
                                                                addedSelected.remove(item)
                                                                viewModel.tempRemoveAddOptions(addedOpt.addOptList[0]!!)
                                                            }
                                                            viewModel.countPopup()
                                                        },
                                                        colors = CheckboxDefaults.colors(
                                                            checkmarkColor = Color.White,
                                                            checkedColor = custom_green,
                                                            uncheckedColor = Color.Black
                                                        ),
                                                        modifier = Modifier
                                                            .scale(1.6f)
                                                            .width(60.dp)
                                                    )
                                                    Text(
                                                        text = addedOpt.addOptList[0]?.optMenuName ?: "",
                                                        fontSize = 45.sp,
                                                        color = Color.Black,
                                                        modifier = Modifier
                                                            .width(600.dp)
                                                            .basicMarquee()
                                                            .padding(start = 10.dp, end = 16.dp)
                                                    )
                                                    Text(
                                                        text = "${getDecimalFormat(addedOpt.addOptList[0]?.optMenuPrice?.toInt() ?: 0)} 원",
                                                        fontSize = 45.sp,
                                                        color = Color.Black,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .wrapContentWidth(Alignment.End)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Divider(color = Color.Black, modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 24.dp)
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                viewModel.showOption(false)
                                viewModel.clearOptions()
                                viewModel.countPopup()
                            }
                            .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                            .background(papa_red, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_cancel),
                            fontSize = 45.sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clickable {
                                if (viewModel.checkValidRequiredOption()) {
                                    viewModel.sendToast("필수옵션을 선택해 주세요.")
                                    return@clickable
                                }
                                options?.let {
                                    viewModel.setCustomOptions()
                                    viewModel.checkPlusMenuItem(it)
                                    //viewModel.addMenus(it)
                                    viewModel.showOption(false)
                                    //viewModel.countPopup()
                                }
                            }
                            .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                            .background(custom_green, shape = RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_confirm),
                            fontSize = 45.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonActions(viewModel: MainMenuViewModel) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(papa_darkgray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .fillMaxWidth()
                .background(color = papa_darkgray)
                .padding(start = 3.dp, 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                PackageButton(viewModel)
                Spacer(modifier = Modifier.height(4.dp))
                PaymentButton(viewModel)
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
        Divider(
            color = papa_darkgray, thickness = 2.dp
        )
        Spacer(modifier = Modifier.height(2.dp))
        CancelButton(viewModel)
    }
}

@Composable
fun CancelButton(viewModel: MainMenuViewModel) {
    val showOption by viewModel.showOption.collectAsState()
    val plusShow by viewModel.showPlusDlg.collectAsState()
    Box(
        modifier = Modifier
            .padding(start = 3.dp)
            .background(papa_darkgray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(papa_red)
                .clickable {
                    if (showOption || plusShow) return@clickable
                    viewModel.clearAllOrders()
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.str_order_cancellation),
                fontSize = 40.sp,
                color = Color.White,
            )
        }
    }
}

@Composable
fun PaymentButton(viewModel: MainMenuViewModel) {
    val showOption by viewModel.showOption.collectAsState()
    val plusShow by viewModel.showPlusDlg.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (showOption || plusShow) return@clickable
                viewModel.payment()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.new_card),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .padding(start = 37.dp, end = 30.dp)
        )
        Text(
            text = stringResource(id = R.string.str_payment_button),
            fontSize = 50.sp,
            color = papa_black,
        )
    }
}

@Composable
fun PackageButton(viewModel: MainMenuViewModel) {
    val isPackage by viewModel.isPackage.observeAsState(false)
    val showOption by viewModel.showOption.collectAsState()
    val plusShow by viewModel.showPlusDlg.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable {
                    if (showOption || plusShow) return@clickable
                    Logger.w("click checkBox!!")
                    viewModel.setItemPackage(!isPackage)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isPackage,
                onCheckedChange = {
                    viewModel.setItemPackage(it)
                },
                colors = CheckboxDefaults.colors(
                    checkmarkColor = Color.White,
                    checkedColor = custom_green,
                    uncheckedColor = papa_black
                ),
                modifier = Modifier
                    .scale(scale = 1.6f)
                    .padding(start = 30.dp, end = 40.dp)
            )
            Text(
                text = stringResource(id = R.string.str_packing_button),
                fontSize = 40.sp,
                color = papa_black
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderScreen(viewModel: MainMenuViewModel) {
    val selectedOrdersGroup by viewModel.selectedOrderGroup.observeAsState(emptyList())
    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .fillMaxHeight()
            .background(papa_darkgray)
            .padding(top = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(id = R.color.white))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .padding(12.dp)
            ) {
                items(selectedOrdersGroup) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it[0].text,
                                fontSize = 24.sp,
                                color = papa_black,
                                modifier = Modifier
                                    .width(300.dp)
                                    .wrapContentWidth(Alignment.Start)
                                    .basicMarquee()
                            )
                            Spacer(modifier = Modifier.padding(end = 8.dp))
                            Image(
                                modifier = Modifier
                                    .clickable {
                                        if(it.size > 1) {
                                            viewModel.removeOrders(it.last())
                                        }

                                    }
                                    .size(28.dp),
                                painter = painterResource(id = R.drawable.re_bt_minus),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(40.dp))
                            Text(
                                text = "${it.size}",
                                fontSize = 24.sp,
                                color = papa_black,
                                modifier = Modifier
                                    .width(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Image(
                                modifier = Modifier
                                    .clickable {
                                        viewModel.addOrders(it.first())
                                    }
                                    .size(28.dp),
                                painter = painterResource(id = R.drawable.re_bt_plus),
                                contentDescription = null
                            )
                            Text(
                                text = "${getDecimalFormat((it.first().itemprice.toLong() + it.first().sumOfOptions()).toInt())}  원",
                                fontSize = 24.sp,
                                color = papa_black,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.End)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Image(
                                modifier = Modifier
                                    .clickable {
                                        viewModel.clearOrders(it)
                                    }
                                    .size(28.dp),
                                painter = painterResource(id = R.drawable.re_bt_delete),
                                contentDescription = null
                            )
                        }
                        val reqOptions = it.flatMap { reqOpt -> reqOpt.optionList.flatMap { optList -> optList.requiredOptList.map { opt -> opt?.optMenuName } } }
                        val addOptions = it.flatMap { addOpt -> addOpt.optionList.flatMap { optList -> optList.addOptList.map { opt -> opt?.optMenuName } } }
                        val separator = if (addOptions.isEmpty() || reqOptions.isEmpty()) "" else " | "
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {
                            Text(
                                text = reqOptions.distinct().withSeparatorString(),
                                fontSize = 18.sp,
                                color = papa_black
                            )
                            Text(
                                text = "$separator${addOptions.distinct().withSeparatorString()}",
                                fontSize = 18.sp,
                                color = papa_lightgray
                            )
                        }
                    }
                }
            }
            Divider(
                color = papa_darkgray, thickness = 3.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.str_total_orders),
                    fontSize = 35.sp,
                    color = papa_black
                )
                Text(
                    text = selectedOrdersGroup.size.toString(),
                    fontSize = 35.sp,
                    color = papa_black
                )
                Text(
                    text = stringResource(id = R.string.str_payment_money),
                    fontSize = 35.sp,
                    color = papa_black,
                    modifier = Modifier.padding(start = 44.dp)
                )
                Text(
                    text = "${getDecimalFormat(viewModel.totalPrice.value.toString().toInt())}  " + stringResource(id = R.string.str_won),
                    fontSize = 35.sp,
                    color = papa_black,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(widthDp = 1080, heightDp = 1920)
@Composable
fun StoreName(vm: MainMenuViewModel) {
    val storeName by vm.storeName.observeAsState()
    val options by vm.storeImage.observeAsState()
    val imgPath = "/storage/emulated/0/BBMC/PAYCAST/Content/"

    Box(
        modifier = Modifier
            .padding(top = 50.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val imgFile = imgPath + options
        Image(
            rememberAsyncImagePainter(
                model = imgFile,
            ),
            contentDescription = null,
            modifier = Modifier
                .height(75.dp),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop,
        )

        Text(
            text = storeName ?: "",
            fontSize = 90.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .basicMarquee()
        )
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            Logger.d("Long click!!!!")
                            vm.sendToast("관리자 페이지를 선택하였습니다.")
                            //viewModel.showSettingDlg(true)
                            vm.enterLockScreen(true)
                        }
                    )
                }
                .background(Color.Transparent)
                .size(100.dp)
                .align(Alignment.TopEnd)
        )
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun TabPager(vm: MainMenuViewModel) {
    val menuDatas by vm.menuData.observeAsState(emptyList())
    val imgPath = "/storage/emulated/0/BBMC/PAYCAST/Content/"
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val showOption by vm.showOption.collectAsState()
    val plusShow by vm.showPlusDlg.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Logger.w("menuDatas : $menuDatas")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(papa_darkdarkgray),
                contentAlignment = Alignment.CenterStart
            ) {

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage + 0,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .pagerTabIndicatorOffset(pagerState, tabPositions)
                                .height(40.dp)
                                .background(Color.Transparent)
                                .padding(bottom = 20.dp)
                        )
                    },
                    backgroundColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .height(92.dp)
                    ,
                ) {
                    menuDatas.forEachIndexed { index, title ->
                        //tab indicator 사용하지 않고 선택된 text 의 배경색을 바꾸는 방법을 사용,
                        // selected 때문에 오류가 뜬다면 기존 방식으로 전환할 것
                        val selected = index == pagerState.currentPage
                        val color = if (selected) custom_green else Color.White
                        val txtColor = if (selected) Color.White else Color.Black
                        Tab(
                            text = {
                                Text(
                                    text = title.name ?: "TEST",
                                    fontSize = 40.sp,
                                    color = txtColor,
                                )
                            },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                Logger.w("Page move : $index")
                                if(showOption || plusShow) return@Tab
                                coroutineScope.launch {
                                    pagerState.scrollToPage(index)
                                    vm.countPopup()
                                }
                            },
                            modifier = Modifier
                                //border(width = 2.dp, color = color, shape = RoundedCornerShape( 20))
                                .background(color, shape = RoundedCornerShape(20))
                                .padding(horizontal = 22.dp, vertical = 4.dp)
                        )

                    }
                }
            }

        HorizontalPager(
            count = menuDatas.size,
            state = pagerState,
            modifier = Modifier.padding(26.dp),
        ) { page ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                items(menuDatas[page].menuObjectList) { item ->
                    Column(
                        modifier = Modifier
                            .border(
                                width = (1.5).dp,
                                color = papa_gray,
                                shape = MaterialTheme.shapes.small,
                            )
                            .height(350.dp)
                            .clickable {
                                if (showOption || plusShow) return@clickable
                                if (item.soldout == "true") return@clickable
                                //isVisible.value 가 false 일때만 처리하기
                                //soldout 된 메뉴들의 clickable 처리를 막음
                                Logger.w("Selected menu by tab!!")
                                //if (!vm.checkOptionItem(item)) vm.checkPlusMenuItem(item)
                                if (item.optMenusList.isNotEmpty()) {
                                    val optSize = item.optMenusList.count { it.menuGubun == "0" }
                                    with(vm) {
                                        setInitOptSize(optSize)
                                        setOption(item)
                                        showOption(true)
                                        countPopup()
                                    }
                                } else {
                                    vm.checkPlusMenuItem(item)
                                }
//                                else {
//                                    //뷰모델로 분기해야함... vm.checkPlusItem(item)
//                                    if (item.plusGroup == "행사상품") {
//                                        val listOfGroupItem = vm.getPlusMenuItem(item)
//                                        listOfGroupItem?.let {
//                                            if(it.isNotEmpty()) vm.setPlusItem(item to it)
//                                            else vm.sendToast("행사상품을 가져올수 없습니다.")
//                                        } ?: vm.sendToast("행사상품을 가져올수 없습니다.")
//                                        vm.showPlusDlg(true)
//                                    }
//                                    vm.addMenus(item)
//                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val imageSrc = imgPath + item.imagefile
                        val imageSrcExt = item.imagefile.split(".").last().lowercase(Locale.getDefault())
                        Box {
                            if (!imageExt.contains(imageSrcExt)) { //상황에 따라서 png 도 추가할 것
                                Image(
                                    painter = painterResource(id = R.drawable.default_menu_icon),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .height(230.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            } else {
                                Image(
                                    rememberAsyncImagePainter(
                                        model = imageSrc,
                                        placeholder = painterResource(id = R.drawable.default_menu_icon)
                                    ),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(bottom = 4.dp, top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.name + "(${item.code})",
                                fontSize = 28.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .basicMarquee()
                                    .padding(top = 8.dp, bottom = 4.dp),
                            )

                            Text(
                                text = item.groupName ?: "일반상품",
                                fontSize = 18.sp,
                                color = colorResource(id = R.color.black),
                                modifier = Modifier
                                    .basicMarquee()
                                    .padding(top = 0.dp, bottom = 0.dp),
                            )

                            val styledText = buildAnnotatedString {
                                var itemPrice = item.price.toFloat()
                                if(item.discount < 1F) {
                                    withStyle(style = SpanStyle(color = Color.Black, fontSize = 18.sp, textDecoration = LineThrough)) {
                                        append(getDecimalFormat(item.price.toInt()))
                                    }
                                    itemPrice *= (1 - item.discount)
                                    append("  ")
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colorResource(id = R.color.black))) {
                                    append(getDecimalFormat(itemPrice.toInt()) + " 원")
                                }
                            }

                            Text(
                                modifier = Modifier
                                    .padding(6.dp, bottom = 6.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically),
                                text = styledText,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                            )

//                            Text(
//                                //text = "${getDecimalFormat(item.price.toInt())} 원",
//                                text = "$styledText 원",
//                                fontSize = 28.sp,
//                                color = Color.White,
//                                modifier = Modifier
//                                    .padding(12.dp)
//                            )
                        }
                    }
                    Box(modifier = Modifier
                        .wrapContentWidth(Alignment.End)
                        .padding(top = 5.dp, end = 5.dp)
                    ) {
                        BadgeImage(item)
                    }
                    if (item.soldout == "true") {
                        Box(
                            modifier = Modifier.height(350.dp)
                        ) {
                            SoldOutItem()
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun BadgeImage(item: MenuObject) {
//    Logger.i("Discount : ${item.name to item.discount}")
    val badgeSrc = when {
        item.newmenu == "true" -> painterResource(id = R.drawable.neww)
        item.popular == "true" -> painterResource(id = R.drawable.new_best_icon)
        item.refill == "infinity" -> painterResource(id = R.drawable.new_sale_icon)
        item.discount < 1f -> painterResource(id = R.drawable.new_sale_icon)
        else -> null
    }
    badgeSrc?.let {
        Image(
            painter = it,
            contentDescription = "",
            modifier = Modifier
                .height(55.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview
@Composable
fun SoldOutItem() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.icon_no_order),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .size(90.dp)
            )
            Spacer(modifier = Modifier.height(35.dp))
            Text(
                text = "지금은\n주문할 수 없어요.",
                fontSize = 23.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
fun PlusMenuDialog(
    plusShow: Boolean,
    vm: MainMenuViewModel,
) {
    val itemGroup by vm.plusItem.collectAsState()
    val item = itemGroup.first
    val selectedItem by vm.plusSelectedItem.collectAsState()
    val optionList by vm.optionPlusData.collectAsState()
    if(plusShow) {
        val imgPath = "/storage/emulated/0/BBMC/PAYCAST/Content/"
        val imageSrc = imgPath + item.imagefile
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)) {
            Box(
                modifier = Modifier
                    .padding(40.dp)
                    .background(Color.Transparent)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(2.dp, papa_darkgray, RoundedCornerShape(10.dp))
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .padding(70.dp) //12였음
//                        .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = "해당 상품은 ${item.groupName} 상품 입니다.",
                            fontSize = 38.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = item.name,
                            fontSize = 45.sp,
                            color = papa_red,
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Image(
                            rememberAsyncImagePainter(
                                model = imageSrc,
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .padding(horizontal = 80.dp)
                                .height(400.dp)
                                .fillMaxWidth(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "아래 메뉴중에서 상품을 골라주세요.",
                            fontSize = 40.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = selectedItem.name.ifEmpty { "선택된 상품이 없습니다." },
                            fontSize = 45.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        val count = optionList.optMenusList.count()
                        if(count > 0) {
                            Text(
                                text = "옵션 : $count 개",
                                fontSize = 25.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        LazyVerticalGridDemo(itemGroup.second, vm)
                    }
                    Row(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 24.dp)
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    Logger.w("Click Cancel")
                                    vm.showPlusDlg(false)
                                    vm.clearMenuData()
                                }
                                .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                                .background(papa_red, RoundedCornerShape(10.dp))
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.str_cancel),
                                fontSize = 45.sp,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable {
                                    Logger.w("Click OK")
                                    if (vm.checkValidPlusSelectedItem()) {
                                        vm.sendToast("행사상품을 골라주세요")
                                    } else {
                                        vm.showPlusDlg(false)
                                        vm.addMenus(item)
                                    }
                                }
                                .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                                .background(custom_green, RoundedCornerShape(10.dp))
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.str_confirm),
                                fontSize = 45.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LazyVerticalGridDemo(list: List<MenuObject>, vm: MainMenuViewModel) {
    val imgPath = "/storage/emulated/0/BBMC/PAYCAST/Content/"

    LazyVerticalGrid(
        //columns = GridCells.Adaptive(128.dp), : 128dp로 고정되어 나눈다.
        columns = GridCells.Fixed(3),

        // content padding
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 16.dp,
            end = 12.dp,
            bottom = 16.dp
        ),
        content = {
            items(list.size) { index ->
                Card(
                    backgroundColor = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                        .height(300.dp)
                        .border(
                            color = papa_darkgray2,
                            width = 2.dp,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .fillMaxWidth(),
                    elevation = 0.dp,
                    onClick = {
                        val item = list[index]
                        if (item.optMenusList.isNotEmpty()) {
                            val optSize = item.optMenusList.count { it.menuGubun == "0" }
                            with(vm) {
                                setInitPlusOptSize(optSize)
                                setPlusOption(item)
                                clearPlusOptions()
                                showPlusOptDlg(true)
                                countPopup()
                            }
                        } else {
                            vm.setPlusItem(item)
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                    ) {
                        Text(
                            text = list[index].name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .basicMarquee()
                        )
                        Text(
                            text = getDecimalFormat(list[index].price.toInt()) + " 원",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .padding(0.dp, 0.dp, 10.dp, 0.dp)
                                .align(Alignment.End)
                        )
                        val imgFile = imgPath + list[index].imagefile
                        Image(
                            rememberAsyncImagePainter(
                                model = imgFile,
                                placeholder = painterResource(id = R.drawable.default_menu_icon),
                                error = painterResource(id = R.drawable.default_menu_icon),
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .height(250.dp)
                                .fillMaxWidth(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun PlusOptionDialog(
    viewModel: MainMenuViewModel,
) {
    val imgPath = "/storage/emulated/0/BBMC/PAYCAST/Content/"
    val item by viewModel.optionPlusData.collectAsState()

    val addedSelected = remember { mutableStateListOf("") } //checkBox를 눌렀을 때
    val radioSelectedList = remember { mutableStateOf(listOf<String>()) }
    val addedSelectedTest = remember { mutableStateListOf("") }

    Logger.e("Plus opt item : $item")

    Box(
        modifier = Modifier
            .padding(40.dp)
            .background(Color.Transparent)
            .fillMaxSize()
            .clickable { }
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .border(2.dp, papa_darkgray, RoundedCornerShape(10.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Text(
                    text = item?.name.toString(),
                    fontSize = 65.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 40.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                val imgFile = imgPath + item?.imagefile.toString()
                Image(
                    rememberAsyncImagePainter(
                        model = imgFile,
                        placeholder = painterResource(id = R.drawable.default_menu_icon)
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 30.dp)
                        .padding(horizontal = 80.dp)
                        .height(400.dp)
                        .fillMaxWidth(),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop,
                )
                //         if(item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    fontSize = 45.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 5.dp)
                        .fillMaxWidth()
                )
                //          }
                val price = item.price
                Text(
                    text = "${getDecimalFormat(price.toInt())} 원",
                    fontSize = 55.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                LazyColumn(
                    modifier = Modifier
                        .padding(30.dp)
                        .fillMaxSize()
                ) {
                    val optionDataSize = item.optMenusList.count { it.menuGubun == "0" }
                    val radioValuesV2: MutableList<String> = MutableList(optionDataSize) { "" }
                    radioSelectedList.value = radioValuesV2.toList()
                    itemsIndexed(item.optMenusList) { i, it ->
                        Box(Modifier.wrapContentHeight()) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = it.menuOptName,
                                    fontSize = 50.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 40.dp, end = 20.dp)
                                )
                                if (it.menuGubun == "0") {
                                    LazyColumn(
                                        modifier = Modifier
                                            .height(500.dp)
                                            .fillMaxWidth()
                                    ) {
                                        itemsIndexed(it.optList) { _, requiredOpt ->
                                            Row(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .height(100.dp)
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        radioValuesV2[i] =
                                                            requiredOpt.requiredOptList[0]!!.optMenuName
                                                        radioSelectedList.value =
                                                            radioValuesV2.toList()
                                                        viewModel.setPlusRequiredOptions(
                                                            requiredOpt.requiredOptList[0]!!,
                                                            i
                                                        )
                                                        viewModel.countPopup()
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = (radioSelectedList.value[i]) == requiredOpt.requiredOptList[0]!!.optMenuName,
                                                    onClick = {
                                                        radioValuesV2[i] = requiredOpt.requiredOptList[0]!!.optMenuName
                                                        radioSelectedList.value = radioValuesV2.toList()
                                                        viewModel.setPlusRequiredOptions(requiredOpt.requiredOptList[0]!!, i)
                                                        viewModel.countPopup()
                                                    },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = custom_green,
                                                        disabledColor = Color.Black,
                                                        unselectedColor = Color.Black
                                                    ),
                                                    modifier = Modifier
                                                        .scale(1.6f)
                                                        .width(60.dp)
                                                )
                                                Text(
                                                    text = requiredOpt.requiredOptList[0]!!.optMenuName,
                                                    fontSize = 45.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier
                                                        .wrapContentWidth()
                                                        .basicMarquee()
                                                        .padding(start = 10.dp, end = 16.dp)
                                                )
                                                Text(
                                                    text = "${getDecimalFormat(requiredOpt.requiredOptList[0]!!.optMenuPrice.toInt())} 원",
                                                    fontSize = 45.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .wrapContentWidth(Alignment.End)
                                                )
                                            }
                                        }
                                    }
                                } else if (it.menuGubun == "1") {
                                    LazyColumn(
                                        modifier = Modifier
                                            .height(500.dp)
                                            .fillMaxWidth()
                                    ) {
                                        items(it.optList) { addedOpt ->
                                            val optItem = addedOpt.addOptList[0]?.optMenuName
                                            val isClicked =
                                                remember { mutableStateOf(optItem in addedSelectedTest) }
                                            Row(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .height(100.dp)
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        isClicked.value = !isClicked.value
                                                        if (isClicked.value) {
                                                            addedSelected.add(optItem!!)
                                                            viewModel.tempPlusAddOptions(addedOpt.addOptList[0]!!)
                                                        } else {
                                                            addedSelected.remove(optItem)
                                                            viewModel.tempRemovePlusAddOptions(
                                                                addedOpt.addOptList[0]!!
                                                            )
                                                        }
                                                        viewModel.countPopup()
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isClicked.value,
                                                    onCheckedChange = {
                                                        isClicked.value = it
                                                        if (it) {
                                                            addedSelected.add(optItem!!)
                                                            viewModel.tempPlusAddOptions(addedOpt.addOptList[0]!!)
                                                        } else {
                                                            addedSelected.remove(optItem)
                                                            viewModel.tempRemovePlusAddOptions(addedOpt.addOptList[0]!!)
                                                        }
                                                        viewModel.countPopup()
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkmarkColor = Color.White,
                                                        checkedColor = custom_green,
                                                        uncheckedColor = Color.Black
                                                    ),
                                                    modifier = Modifier
                                                        .scale(1.6f)
                                                        .width(60.dp)
                                                )
                                                Text(
                                                    text = addedOpt.addOptList[0]?.optMenuName
                                                        ?: "",
                                                    fontSize = 45.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier
                                                        .wrapContentWidth()
                                                        .basicMarquee()
                                                        .padding(start = 10.dp, end = 16.dp)
                                                )
                                                Text(
                                                    text = "${getDecimalFormat(addedOpt.addOptList[0]?.optMenuPrice?.toInt() ?: 0)} 원",
                                                    fontSize = 45.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .wrapContentWidth(Alignment.End)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Divider(
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            thickness = 2.dp
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 24.dp)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .clickable {
                            viewModel.showPlusOptDlg(false)
                            viewModel.clearPlusOptions()
                            viewModel.clearOptions()
                            viewModel.countPopup()
                        }
                        .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                        .background(papa_red, RoundedCornerShape(10.dp))
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.str_cancel),
                        fontSize = 45.sp,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .clickable {
                            if (viewModel.checkValidPlusRequiredOption()) {
                                viewModel.sendToast("필수옵션을 선택해 주세요.")
                                return@clickable
                            }
                            item?.let {
                                viewModel.setPlusCustomOptions()
                                //viewModel.addMenus(it)
                                viewModel.setPlusItem(it)
                                viewModel.showPlusOptDlg(false)
                                viewModel.countPopup()
                            }
                        }
                        .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                        .background(custom_green, RoundedCornerShape(10.dp))
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.str_confirm),
                        fontSize = 45.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}