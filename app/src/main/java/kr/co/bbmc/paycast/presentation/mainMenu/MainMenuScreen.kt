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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
            .background(color = Color.Black)
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
                .background(Color.White)
                .fillMaxSize()
                .clickable { }
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .background(Color.Black)
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
                        color = Color.White,
                        modifier = Modifier
                            .padding(12.dp)
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
                            .padding(12.dp)
                            .padding(horizontal = 80.dp)
                            .height(400.dp)
                            .fillMaxWidth(),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = options.description,
                        fontSize = 45.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    )
                    val price = options.price
                    Text(
                        text = "${getDecimalFormat(price.toInt())} 원",
                        fontSize = 55.sp,
                        color = Color.White,
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
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 40.dp, end = 20.dp)
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
                                                            selectedColor = Color.White,
                                                            disabledColor = Color.Black,
                                                            unselectedColor = Color.White
                                                        ),
                                                        modifier = Modifier
                                                            .scale(1.6f)
                                                            .width(60.dp)
                                                    )
                                                    Text(
                                                        text = requiredOpt.requiredOptList[0]!!.optMenuName,
                                                        fontSize = 45.sp,
                                                        color = Color.White,
                                                        modifier = Modifier
                                                            .width(600.dp)
                                                            .basicMarquee()
                                                            .padding(end = 16.dp)
                                                    )
                                                    Text(
                                                        text = "${getDecimalFormat(requiredOpt.requiredOptList[0]!!.optMenuPrice.toInt())} 원",
                                                        fontSize = 45.sp,
                                                        color = Color.White,
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
                                                        .padding(8.dp)
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
                                                            checkmarkColor = Color.Black,
                                                            checkedColor = Color.White,
                                                            uncheckedColor = Color.White
                                                        ),
                                                        modifier = Modifier
                                                            .scale(1.6f)
                                                            .width(60.dp)
                                                    )
                                                    Text(
                                                        text = addedOpt.addOptList[0]?.optMenuName ?: "",
                                                        fontSize = 45.sp,
                                                        color = Color.White,
                                                        modifier = Modifier
                                                            .width(600.dp)
                                                            .basicMarquee()
                                                            .padding(end = 16.dp)
                                                    )
                                                    Text(
                                                        text = "${getDecimalFormat(addedOpt.addOptList[0]?.optMenuPrice?.toInt() ?: 0)} 원",
                                                        fontSize = 45.sp,
                                                        color = Color.White,
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
                            Divider(color = Color.White, modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp)
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
                            .background(custom_red)
                            .clip(RoundedCornerShape(10.dp))
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_cancel),
                            fontSize = 45.sp,
                            color = Color.Black
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
                            .background(custom_yellow)
                            .clip(RoundedCornerShape(10.dp))
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.str_confirm),
                            fontSize = 45.sp,
                            color = Color.Black
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
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .fillMaxWidth()
                .background(color = Color.Black)
                .padding(start = 4.dp)
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
            color = Color.White, thickness = 2.dp
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
            .padding(start = 4.dp)
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(custom_red)
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
                color = Color.Black,
            )
        }
    }
}

@Composable
fun PaymentButton(viewModel: MainMenuViewModel) {
    val showOption by viewModel.showOption.collectAsState()
    val plusShow by viewModel.showPlusDlg.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(custom_yellow)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (showOption || plusShow) return@clickable
                viewModel.payment()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.str_payment_button),
            fontSize = 70.sp,
            color = Color.Black,
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
            .clip(RoundedCornerShape(16.dp))
            .background(custom_yellow)
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
                    checkmarkColor = Color.Black,
                    checkedColor = Color.White,
                    uncheckedColor = Color.Black
                ),
                modifier = Modifier
                    .scale(scale = 1.6f)
                    .padding(end = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.str_packing_button),
                fontSize = 50.sp,
                color = Color.Black
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(id = R.color.grey))
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
                                .padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it[0].text,
                                fontSize = 24.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .width(300.dp)
                                    .wrapContentWidth(Alignment.Start)
                                    .basicMarquee()
                            )
                            Spacer(modifier = Modifier.padding(end = 8.dp))
                            Image(
                                modifier = Modifier
                                    .clickable {
                                        viewModel.removeOrders(it.last())
                                    }
                                    .size(28.dp),
                                painter = painterResource(id = R.drawable.bt_minus),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(40.dp))
                            Text(
                                text = "${it.size}",
                                fontSize = 24.sp,
                                color = Color.White,
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
                                painter = painterResource(id = R.drawable.bt_plus),
                                contentDescription = null
                            )
                            Text(
                                text = "${getDecimalFormat((it.first().itemprice.toLong() + it.first().sumOfOptions()).toInt())}  원",
                                fontSize = 24.sp,
                                color = Color.White,
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
                                painter = painterResource(id = R.drawable.bt_delete),
                                contentDescription = null
                            )
                        }
                        val reqOptions = it.flatMap { reqOpt -> reqOpt.optionList.flatMap { optList -> optList.requiredOptList.map { opt -> opt?.optMenuName } } }
                        val addOptions = it.flatMap { addOpt -> addOpt.optionList.flatMap { optList -> optList.addOptList.map { opt -> opt?.optMenuName } } }
                        val separator = if (addOptions.isEmpty() || reqOptions.isEmpty()) "" else " | "
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = reqOptions.distinct().withSeparatorString(),
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                text = "$separator${addOptions.distinct().withSeparatorString()}",
                                fontSize = 20.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
            Divider(
                color = Color.White, thickness = 2.dp
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
                    color = Color.White
                )
                Text(
                    text = selectedOrdersGroup.size.toString(),
                    fontSize = 35.sp,
                    color = Color.White
                )
                Text(
                    text = stringResource(id = R.string.str_payment_money),
                    fontSize = 35.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 44.dp)
                )
                Text(
                    text = "${getDecimalFormat(viewModel.totalPrice.value.toString().toInt())}  " + stringResource(id = R.string.str_won),
                    fontSize = 35.sp,
                    color = Color.White,
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

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = storeName ?: "",
            fontSize = 90.sp,
            color = Color.White,
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
                .size(80.dp)
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
    val shape = RoundedCornerShape(20)
    val showOption by vm.showOption.collectAsState()
    val plusShow by vm.showPlusDlg.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage + 0,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .pagerTabIndicatorOffset(pagerState, tabPositions)
                        .height(40.dp)
                        .background(Color.Transparent)
                )
            },
            backgroundColor = Color.Transparent,
            modifier = Modifier
                .clip(shape = shape)
                .padding(bottom = 16.dp)
        ) {
            menuDatas.forEachIndexed { index, title ->
                //tab indicator 사용하지 않고 선택된 text 의 배경색을 바꾸는 방법을 사용,
                // selected 때문에 오류가 뜬다면 기존 방식으로 전환할 것
                val selected = index == pagerState.currentPage
                val color = if (selected) custom_yellow else Color.Transparent
                val txtColor = if (selected) Color.Black else Color.White
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
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
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
                                width = 4.dp,
                                color = Color.White,
                                shape = MaterialTheme.shapes.small
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
                                        .height(200.dp),
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
                                .padding(bottom = 4.dp, top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.name + "(${item.code})",
                                fontSize = 28.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .basicMarquee()
                                    .padding(top = 8.dp, bottom = 4.dp),
                            )

                            Text(
                                text = item.groupName ?: "일반상품",
                                fontSize = 18.sp,
                                color = colorResource(id = R.color.Orange),
                                modifier = Modifier
                                    .basicMarquee()
                                    .padding(top = 0.dp, bottom = 0.dp),
                            )

                            val styledText = buildAnnotatedString {
                                var itemPrice = item.price.toFloat()
                                if(item.discount < 1F) {
                                    withStyle(style = SpanStyle(color = Color.White, fontSize = 18.sp, textDecoration = LineThrough)) {
                                        append(getDecimalFormat(item.price.toInt()))
                                    }
                                    itemPrice *= (1 - item.discount)
                                    append("  ")
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colorResource(id = R.color.Orange))) {
                                    append(getDecimalFormat(itemPrice.toInt()) + " 원")
                                }
                            }

                            Text(
                                modifier = Modifier
                                    .padding(6.dp)
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
                        .padding(start = 6.dp, top = 6.dp)) {
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
        item.newmenu == "true" -> painterResource(id = R.drawable.badge_new)
        item.popular == "true" -> painterResource(id = R.drawable.badge_hot)
        item.refill == "infinity" -> painterResource(id = R.drawable.ic_refill_ball)
        item.discount < 1f -> painterResource(id = R.drawable.badge_sale)
        else -> null
    }
    badgeSrc?.let {
        Image(
            painter = it,
            contentDescription = "",
            modifier = Modifier
                .height(50.dp),
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
            .background(Color.Black.copy(alpha = 0.7f))) {
            Box(
                modifier = Modifier
                    .padding(140.dp)
                    .background(Color.White)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .background(Color.Black)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
//                        .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = "해당 상품은 ${item.groupName} 상품 입니다.",
                            fontSize = 43.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = item.name,
                            fontSize = 45.sp,
                            color = colorResource(id = R.color.Orange),
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
                                .height(300.dp)
                                .fillMaxWidth(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "아래 메뉴중에서 상품을 골라주세요.",
                            fontSize = 40.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = selectedItem.name.ifEmpty { "선택된 상품이 없습니다." },
                            fontSize = 45.sp,
                            color = Color.White,
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
                                color = Color.White,
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
                                .background(custom_red)
                                .clip(RoundedCornerShape(10.dp))
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.str_cancel),
                                fontSize = 45.sp,
                                color = Color.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable {
                                    Logger.w("Click OK")
                                    if(vm.checkValidPlusSelectedItem()) {
                                        vm.sendToast("행사상품을 골라주세요")
                                    } else {
                                        vm.showPlusDlg(false)
                                        vm.addMenus(item)
                                    }
                                }
                                .padding(start = 8.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
                                .background(custom_yellow)
                                .clip(RoundedCornerShape(10.dp))
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.str_confirm),
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
                    backgroundColor = Color.Red,
                    modifier = Modifier
                        .padding(4.dp)
                        .height(300.dp)
                        .fillMaxWidth(),
                    elevation = 8.dp,
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
                            color = Color(0xFFFFFFFF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .basicMarquee()
                        )
                        Text(
                            text = getDecimalFormat(list[index].price.toInt()) + " 원",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.Orange),
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
            .background(Color.White)
            .fillMaxSize(0.7f)
            .clickable { }
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .background(Color.Black)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Text(
                    text = item?.name.toString(),
                    fontSize = 50.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(12.dp)
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
                        .padding(12.dp)
                        .padding(horizontal = 80.dp)
                        .height(350.dp)
                        .fillMaxWidth(),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop,
                )
                if(item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        fontSize = 35.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    )
                }
                val price = item.price
                Text(
                    text = "${getDecimalFormat(price.toInt())} 원",
                    fontSize = 35.sp,
                    color = Color.White,
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
                                    fontSize = 40.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 40.dp, end = 20.dp)
                                )
                                if (it.menuGubun == "0") {
                                    LazyColumn(
                                        modifier = Modifier
                                            .height(350.dp)
                                            .fillMaxWidth()
                                    ) {
                                        itemsIndexed(it.optList) { _, requiredOpt ->
                                            Row(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .height(60.dp)
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        radioValuesV2[i] = requiredOpt.requiredOptList[0]!!.optMenuName
                                                        radioSelectedList.value = radioValuesV2.toList()
                                                        viewModel.setPlusRequiredOptions(requiredOpt.requiredOptList[0]!!, i)
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
                                                        selectedColor = Color.White,
                                                        disabledColor = Color.Black,
                                                        unselectedColor = Color.White
                                                    ),
                                                    modifier = Modifier
                                                        .scale(1.6f)
                                                        .width(40.dp)
                                                )
                                                Text(
                                                    text = requiredOpt.requiredOptList[0]!!.optMenuName,
                                                    fontSize = 30.sp,
                                                    color = Color.White,
                                                    modifier = Modifier
                                                        .wrapContentWidth()
                                                        .basicMarquee()
                                                        .padding(end = 16.dp)
                                                )
                                                Text(
                                                    text = "${getDecimalFormat(requiredOpt.requiredOptList[0]!!.optMenuPrice.toInt())} 원",
                                                    fontSize = 30.sp,
                                                    color = Color.White,
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
                                            .height(350.dp)
                                            .fillMaxWidth()
                                    ) {
                                        items(it.optList) { addedOpt ->
                                            val optItem = addedOpt.addOptList[0]?.optMenuName
                                            val isClicked =
                                                remember { mutableStateOf(optItem in addedSelectedTest) }
                                            Row(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .height(60.dp)
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        isClicked.value = !isClicked.value
                                                        if (isClicked.value) {
                                                            addedSelected.add(optItem!!)
                                                            viewModel.tempPlusAddOptions(addedOpt.addOptList[0]!!)
                                                        } else {
                                                            addedSelected.remove(optItem)
                                                            viewModel.tempRemovePlusAddOptions(addedOpt.addOptList[0]!!)
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
                                                        checkmarkColor = Color.Black,
                                                        checkedColor = Color.White,
                                                        uncheckedColor = Color.White
                                                    ),
                                                    modifier = Modifier
                                                        .scale(1.6f)
                                                        .width(40.dp)
                                                )
                                                Text(
                                                    text = addedOpt.addOptList[0]?.optMenuName
                                                        ?: "",
                                                    fontSize = 30.sp,
                                                    color = Color.White,
                                                    modifier = Modifier
                                                        .wrapContentWidth()
                                                        .basicMarquee()
                                                        .padding(end = 16.dp)
                                                )
                                                Text(
                                                    text = "${getDecimalFormat(addedOpt.addOptList[0]?.optMenuPrice?.toInt() ?: 0)} 원",
                                                    fontSize = 30.sp,
                                                    color = Color.White,
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
                        .background(custom_red)
                        .clip(RoundedCornerShape(10.dp))
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.str_cancel),
                        fontSize = 45.sp,
                        color = Color.Black
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
                        .background(custom_yellow)
                        .clip(RoundedCornerShape(10.dp))
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.str_confirm),
                        fontSize = 45.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}