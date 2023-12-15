package kr.co.bbmc.paycast.util

import android.content.Context
import com.orhanobut.logger.Logger
import kr.co.bbmc.paycast.App
import kr.co.bbmc.paycast.R
import kr.co.bbmc.selforderutil.PlayerOptionEnv
import kr.co.bbmc.selforderutil.StbOptionEnv
import org.w3c.dom.Element
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

fun parseAgentOptionXMLV2(fileName: String, stbOption: StbOptionEnv): StbOptionEnv {
    return runCatching {
        val fXmlFile = File(fileName)
        Logger.d("parseAgentOptionXML fileName=$fileName")
        val xmlInfo: InputStream = fXmlFile.inputStream()
        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        val parser: XmlPullParser = factory.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(xmlInfo, null)

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "FtpActiveMode" -> stbOption.ftpActiveMode =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "MonitorMins" -> stbOption.monitorMins =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "PlayerStart" -> stbOption.playerStart =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "Server.FtpHost" -> stbOption.ftpHost =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.FtpPassword" -> stbOption.ftpPassword =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.FtpPort" -> stbOption.ftpPort =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Server.FtpUser" -> stbOption.ftpUser =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.ServerHost" -> stbOption.serverHost =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.ServerPort" -> stbOption.serverPort =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Server.ServerUkid" -> stbOption.serverUkid =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.StbId" -> stbOption.stbId =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Server.StbName" -> stbOption.stbName =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.StbServiceType" -> stbOption.stbServiceType =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.StbUdpPort" -> stbOption.stbUdpPort =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Server.storeName" -> stbOption.storeName =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.storeAddr" -> stbOption.storeAddr =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.businessNum" -> stbOption.storeBusinessNum =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.storeTel" -> stbOption.storeTel =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.merchantNum" -> stbOption.storeMerchantNum =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.storeCatid" -> stbOption.storeCatId =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.represent" -> stbOption.storeRepresent =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.storeId" -> stbOption.storeId =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "operatingTime" -> stbOption.operatingTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "storeIntroduction" -> stbOption.introMsg =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.deviceId" -> stbOption.deviceId =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.MainPrintEnable" -> stbOption.mainPrtEnable =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Server.MainPrintIp" -> stbOption.mainPrtip =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "koEnabled" -> stbOption.koEnable =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "atEnabled" -> stbOption.atEnable =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "openType" -> stbOption.openType =
                            runCatching { parser.nextText() }.getOrDefault("")
                        else -> {}
                    }
                }
            }
            event = parser.next()
        }
        stbOption
    }
        .onSuccess { Logger.w("Success parse xml Data : ${it.stbId}") }
        .onFailure { Logger.e("Error : ${it.message}") }
        .getOrDefault(stbOption)
}

@Suppress("DUPLICATE_LABEL_IN_WHEN")
fun parsePlayerOptionXMLV2(fileName: String) {
    try {
        val fXmlFile = File(fileName)
        Logger.d("parseAgentOptionXML fileName=$fileName")
        val xmlInfo: InputStream = fXmlFile.inputStream()
        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        val parser: XmlPullParser = factory.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(xmlInfo, null)

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "Communication.ManagerTcpPort" -> App.APP.playerOpt?.optionDefaultLang =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Communication.StbUdpPort" -> App.APP.playerOpt?.optionStbUdpPort =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Playtime.FriBeginTime" -> App.APP.playerOpt?.timeFriBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.FriEndTime" -> App.APP.playerOpt?.timeFriEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SatBeginTime" -> App.APP.playerOpt?.timeSatBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SatEndTime" -> App.APP.playerOpt?.timeSatEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SunBeginTime" -> App.APP.playerOpt?.timeSunBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SunEndTime" -> App.APP.playerOpt?.timeSunEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.MonBeginTime" -> App.APP.playerOpt?.timeMonBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.MonEndTime" -> App.APP.playerOpt?.timeMonEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.TueBeginTime" -> App.APP.playerOpt?.timeTueBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.TueEndTime" -> App.APP.playerOpt?.timeTueEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.WedBeginTime" -> App.APP.playerOpt?.timeWedBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.WedEndTime" -> App.APP.playerOpt?.timeWedEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.ThuBeginTime" -> App.APP.playerOpt?.timeThuBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.ThuEndTime" -> App.APP.playerOpt?.timeThuEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Region" -> App.APP.playerOpt?.optionDefaultRegion =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.GuideImageFile" -> App.APP.playerOpt?.optionIntroductionImgFile =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.PptViewerFile" -> App.APP.playerOpt?.optionPowerPointViewerFile =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.ScheduleFile" -> App.APP.playerOpt?.optionDefaultScheduleFile =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.StoreMenuFile" -> App.APP.playerOpt?.optionDefaultMenuFile =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "OrderNum.initValue" -> App.APP.playerOpt?.optionDefaultOrderNum =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.ConnectionCheck" -> App.APP.playerOpt?.optionConnectionCheckingRequired =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.ConnectionErrorDisplayed" -> App.APP.playerOpt?.optionDisplayConnectionError =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.CustomExec" -> App.APP.playerOpt?.optionCustomExeFile =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.CustomArgs" -> App.APP.playerOpt?.optionCustomExeArgs =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.SleepSecs" -> App.APP.playerOpt?.optionSleepSecs =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "SlideShow.KioskStartOnPlayerStartUp" -> App.APP.playerOpt?.optionKioskStartOnPlayerStartUp =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.KioskAutoStartSecs" -> App.APP.playerOpt?.optionKioskAutoStartSecs =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "SlideShow.AfterClosingAction" -> App.APP.playerOpt?.optionAfterClosingAction =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.ClosingDelaySecs" -> App.APP.playerOpt?.optionClosingDelaySecs =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "SlideShow.MonitorAction" -> App.APP.playerOpt?.optionMonitorAction =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.RestartHours" -> App.APP.playerOpt?.optionRestartHours =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.ScreenExtMode" -> App.APP.playerOpt?.optionScreenExtMode =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.TestNoticeDisplayed" -> App.APP.playerOpt?.optionTestNoticeDisplayed =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.TestNoticeText" -> App.APP.playerOpt?.optionTestNoticeText =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.TopmostDisplayed" -> App.APP.playerOpt?.optionTopmostDisplayed =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.TimeSyncRequired" -> App.APP.playerOpt?.optionTimeSyncRequired =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.AllSchedulePlayMode" -> App.APP.playerOpt?.optionAllSchedulePlayMode =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.WebBrowserFile" -> App.APP.playerOpt?.optionWebBrowserFile =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.SimpleClockDisplayed" -> App.APP.playerOpt?.optionSimpleClockDisplayed =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.SimpleClockDisplayedOnlyOntheHour" -> App.APP.playerOpt?.optionSimpleClockDisplayedOnlyOntheHour =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.SlideProgressBarDisplayed" -> App.APP.playerOpt?.optionSlideProgressBarDisplayed =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.SlideProgressBarColor" -> App.APP.playerOpt?.optionSlideProgressBarColor =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.OperationInfoDebugged" -> App.APP.playerOpt?.optionOperationInfoDebugged =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.TouchInfoLogged" -> App.APP.playerOpt?.optionTouchInfoLogged =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.DefaultFadeApplied" -> App.APP.playerOpt?.optionSlideShowFadeInOutApplied =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.DefaultFadeTimeType" -> App.APP.playerOpt?.optionSlideShowDefaultFadeTimeType =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.TransparentBGApplied" -> App.APP.playerOpt?.optionSlideShowTransparentBGApplied =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.DisplayAfterDuration" -> App.APP.playerOpt?.optionSlideShowDisplayAfterDuration =
                            runCatching { parser.nextText().toBoolean() }.getOrDefault(false)
                        "SlideShow.RoutineSchedPrefix" -> App.APP.playerOpt?.optionRoutineSchedPrefix =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "SlideShow.AutoDelSchedDays" -> App.APP.playerOpt?.optionAutoDelSchedDays =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Playtime.MonBeginTime" -> App.APP.playerOpt?.timeMonBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.MonEndTime" -> App.APP.playerOpt?.timeMonEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.TueBeginTime" -> App.APP.playerOpt?.timeTueBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.TueEndTime" -> App.APP.playerOpt?.timeTueEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.WedBeginTime" -> App.APP.playerOpt?.timeWedBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.WedEndTime" -> App.APP.playerOpt?.timeWedEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.ThuBeginTime" -> App.APP.playerOpt?.timeThuBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.ThuEndTime" -> App.APP.playerOpt?.timeThuEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.FriBeginTime" -> App.APP.playerOpt?.timeFriBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.FriEndTime" -> App.APP.playerOpt?.timeFriEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SatBeginTime" -> App.APP.playerOpt?.timeSatBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SatEndTime" -> App.APP.playerOpt?.timeSatEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SunBeginTime" -> App.APP.playerOpt?.timeSunBeginTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Playtime.SunEndTime" -> App.APP.playerOpt?.timeSunEndTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Communication.ManagerTcpPort" -> App.APP.playerOpt?.optionManagerTcpPort =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Communication.StbUdpPort" -> App.APP.playerOpt?.optionStbUdpPort =
                            runCatching { parser.nextText().toInt() }.getOrDefault(0)
                        "Temp.DebugDueTime" -> App.APP.playerOpt?.optionDebugDueTime =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Weather.City" -> App.APP.playerOpt?.optionWeatherCity =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Weather.Display" -> App.APP.playerOpt?.optionWeatherCityDisplay =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Weather.Unit" -> App.APP.playerOpt?.optionWeatherUnit =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Weather.ApiKey" -> App.APP.playerOpt?.optionWeatherApiKey =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Volume" -> App.APP.playerOpt?.optionVolume =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Communication.cardServer" -> App.APP.playerOpt?.optionCardServer =
                            runCatching { parser.nextText() }.getOrDefault("")
                        "Communication.cardport" -> App.APP.playerOpt?.optionCardport =
                            runCatching { parser.nextText() }.getOrDefault("")
                        else -> {}
                    }
                }
            }
            event = parser.next()
        }
    } catch (e: Exception) {
        Logger.e("Error: ${e.message}")
    }
}

// 기존 selfUtil에서 분리
fun parsePlayerOptionXML(fileName: String?, option: PlayerOptionEnv, c: Context): PlayerOptionEnv? {
    return try {
        val fXmlFile = File(fileName)
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val dBuilder = factory.newDocumentBuilder()
        val doc = dBuilder.parse(fXmlFile)
        doc.documentElement.normalize()
        val nList = doc.getElementsByTagName("Option")
        for (temp in 0 until nList.length) {
            val nNode = nList.item(temp)
            if (nNode.nodeType.toInt() == 1) {
                val eElement = nNode as Element
                option.optionDefaultLang =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerLang))
                        .item(0).textContent
                option.optionDefaultRegion =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerRegion))
                        .item(0).textContent
                option.optionIntroductionImgFile =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerGuidImgFile))
                        .item(0).textContent
                option.optionPowerPointViewerFile =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerPptViewFile))
                        .item(0).textContent
                option.optionDefaultScheduleFile =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerScheduleFile))
                        .item(0).textContent
                option.optionDefaultMenuFile =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerStoreMenuFile))
                        .item(0).textContent
                option.optionDefaultOrderNum =
                    eElement.getElementsByTagName(c.resources.getString(R.string.player_order_num_init))
                        .item(0).textContent
                option.optionConnectionCheckingRequired = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerConnectionCheck)
                    ).item(0).textContent
                )
                option.optionDisplayConnectionError = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playervConnectionErrorDisplayed)
                    ).item(0).textContent
                )
                option.optionCustomExeFile =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerCustomExec))
                        .item(0).textContent
                option.optionCustomExeArgs =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerCustomArgs))
                        .item(0).textContent
                option.optionSleepSecs = Integer.valueOf(
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerSleepSecs))
                        .item(0).textContent
                )
                option.optionKioskStartOnPlayerStartUp = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerKioskStartOnPlayerStartUp)
                    ).item(0).textContent
                )
                option.optionKioskAutoStartSecs = Integer.valueOf(
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerKioskAutoStartSecs))
                        .item(0).textContent
                )
                option.optionAfterClosingAction =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerAfterClosingAction))
                        .item(0).textContent
                option.optionClosingDelaySecs = Integer.valueOf(
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerClosingDelaySecs))
                        .item(0).textContent
                )
                option.optionMonitorAction =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerMonitorAction))
                        .item(0).textContent
                option.optionRestartHours =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerRestartHours))
                        .item(0).textContent
                option.optionScreenExtMode =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerScreenExtMode))
                        .item(0).textContent
                option.optionTestNoticeDisplayed = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerTestNoticeDisplayed)
                    ).item(0).textContent
                )
                option.optionTestNoticeText =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerTestNoticeText))
                        .item(0).textContent
                option.optionTopmostDisplayed = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerTopmostDisplayed)
                    ).item(0).textContent
                )
                option.optionTimeSyncRequired = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerTimeSyncRequired)
                    ).item(0).textContent
                )
                option.optionAllSchedulePlayMode = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerAllSchedulePlayMode)
                    ).item(0).textContent
                )
                option.optionWebBrowserFile =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerWebBrowserFile))
                        .item(0).textContent
                option.optionSimpleClockDisplayed = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerSimpleClockDisplayed)
                    ).item(0).textContent
                )
                option.optionSimpleClockDisplayedOnlyOntheHour = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerSimpleClockDisplayedOnlyOntheHour)
                    ).item(0).textContent
                )
                option.optionSlideProgressBarDisplayed = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerSlideProgressBarDisplayed)
                    ).item(0).textContent
                )
                option.optionSlideProgressBarColor =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerSlideProgressBarColor))
                        .item(0).textContent
                option.optionOperationInfoDebugged = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerOperationInfoDebugged)
                    ).item(0).textContent
                )
                option.optionTouchInfoLogged = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerTouchInfoLogged)
                    ).item(0).textContent
                )
                option.optionSlideShowFadeInOutApplied = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerDefaultFadeApplied)
                    ).item(0).textContent
                )
                option.optionSlideShowDefaultFadeTimeType =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerDefaultFadeTimeType))
                        .item(0).textContent
                option.optionSlideShowTransparentBGApplied = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerTransparentBGApplied)
                    ).item(0).textContent
                )
                option.optionSlideShowDisplayAfterDuration = java.lang.Boolean.valueOf(
                    eElement.getElementsByTagName(
                        c.resources.getString(R.string.playerDisplayAfterDuration)
                    ).item(0).textContent
                )
                option.optionRoutineSchedPrefix =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerRoutineSchedPrefix))
                        .item(0).textContent
                option.optionAutoDelSchedDays = Integer.valueOf(
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerAutoDelSchedDays))
                        .item(0).textContent
                )
                option.timeMonBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerMonBegin))
                        .item(0).textContent
                option.timeMonEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerMonEnd))
                        .item(0).textContent
                option.timeTueBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerTueBegin))
                        .item(0).textContent
                option.timeTueEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerTueEnd))
                        .item(0).textContent
                option.timeWedBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerWedBegin))
                        .item(0).textContent
                option.timeWedEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerWedEnd))
                        .item(0).textContent
                option.timeThuBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerThuBegin))
                        .item(0).textContent
                option.timeThuEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerThuEnd))
                        .item(0).textContent
                option.timeFriBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerFriBegin))
                        .item(0).textContent
                option.timeFriEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerFriEnd))
                        .item(0).textContent
                option.timeSatBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerSatBegin))
                        .item(0).textContent
                option.timeSatEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerSatEnd))
                        .item(0).textContent
                option.timeSunBeginTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerSunBegin))
                        .item(0).textContent
                option.timeSunEndTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerSunEnd))
                        .item(0).textContent
                option.optionManagerTcpPort = Integer.valueOf(
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerTcpPort))
                        .item(0).textContent
                )
                option.optionStbUdpPort = Integer.valueOf(
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerUdpport))
                        .item(0).textContent
                )
                option.optionDebugDueTime =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerDebugDueTime))
                        .item(0).textContent
                if (option.optionDebugDueTime == null) {
                    option.optionDebugDueTime = ""
                }
                option.optionWeatherCity =
                    eElement.getElementsByTagName(c.resources.getString(R.string.weatherCity))
                        .item(0).textContent
                option.optionWeatherCityDisplay =
                    eElement.getElementsByTagName(c.resources.getString(R.string.weatherDisp))
                        .item(0).textContent
                option.optionWeatherUnit =
                    eElement.getElementsByTagName(c.resources.getString(R.string.weatherUnit))
                        .item(0).textContent
                option.optionWeatherApiKey =
                    eElement.getElementsByTagName(c.resources.getString(R.string.weatherApiKey))
                        .item(0).textContent
                option.optionVolume =
                    eElement.getElementsByTagName(c.resources.getString(R.string.playerVolume))
                        .item(0).textContent
                option.optionCardServer =
                    eElement.getElementsByTagName(c.resources.getString(R.string.option_card_server))
                        .item(0).textContent
                option.optionCardport =
                    eElement.getElementsByTagName(c.resources.getString(R.string.option_card_port))
                        .item(0).textContent
                option.optionCardport =
                    eElement.getElementsByTagName("Communication.cardport").item(0).textContent
            }
        }
        option
    } catch (var12: Exception) {
        Logger.e("XmlOptionParser : 5. Error in ParseXML()", var12)
        option
    }
}