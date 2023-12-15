package kr.co.bbmc.paycast.util

import android.content.Context
import android.util.Log
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.data.model.*
import kr.co.bbmc.paycast.data.model.CatagoryObject
import kr.co.bbmc.paycast.data.model.CustomAddedOptionData
import kr.co.bbmc.paycast.data.model.CustomRequiredOptionData
import kr.co.bbmc.paycast.data.model.MenuCatagoryObject
import kr.co.bbmc.paycast.data.model.MenuObject
import kr.co.bbmc.paycast.data.model.MenuOptionData
import kr.co.bbmc.selforderutil.*
import org.json.JSONArray
import org.json.JSONException
import org.w3c.dom.Element
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class XmlOptionParser {
    fun onGetPlayerConfigData(c: Context?): SCEnvironment {
        val data = SCEnvironment()
        OptUtil.ReadOptions(1, c)
        data.defaultLang = OptUtil.GetValue("Lang", data.defaultLang)
        data.defaultRegion = OptUtil.GetValue("Region", data.defaultRegion)
        data.introductionImgFile = OptUtil.GetValue("SlideShow.GuideImageFile")
        data.powerPointViewerFile = OptUtil.GetValue("SlideShow.PptViewerFile")
        data.defaultScheduleFile = OptUtil.GetValue("SlideShow.ScheduleFile")
        data.defaultMenuFile = OptUtil.GetValue("SlideShow.StoreMenuFile")
        data.connectionCheckingRequired = OptUtil.GetValue("SlideShow.ConnectionCheck", false)
        data.displayConnectionError = OptUtil.GetValue("SlideShow.ConnectionErrorDisplayed", false)
        data.customExeFile = OptUtil.GetValue("SlideShow.CustomExec")
        data.customExeArgs = OptUtil.GetValue("SlideShow.CustomArgs")
        data.sleepSecs = OptUtil.GetValue("SlideShow.SleepSecs", 0)
        data.kioskStartOnPlayerStartUp =
            OptUtil.GetValue("SlideShow.KioskStartOnPlayerStartUp", false)
        data.kioskAutoStartSecs = OptUtil.GetValue("SlideShow.KioskAutoStartSecs", 0)
        data.afterClosingAction = OptUtil.GetValue("SlideShow.AfterClosingAction", "PF")
        data.closingDelaySecs = OptUtil.GetValue("SlideShow.ClosingDelaySecs", 60)
        data.monitorAction = OptUtil.GetValue("SlideShow.MonitorAction", "Monitor")
        data.restartHours = OptUtil.GetValue("SlideShow.RestartHours", "3")
        data.screenExtMode = OptUtil.GetValue("SlideShow.ScreenExtMode", "0")
        data.testNoticeDisplayed = OptUtil.GetValue("SlideShow.TestNoticeDisplayed", false)
        data.testNoticeText = OptUtil.GetValue("SlideShow.TestNoticeText")
        data.topmostDisplayed = OptUtil.GetValue("SlideShow.TopmostDisplayed", false)
        data.timeSyncRequired = OptUtil.GetValue("SlideShow.TimeSyncRequired", false)
        data.allSchedulePlayMode = OptUtil.GetValue("SlideShow.AllSchedulePlayMode", false)
        data.webBrowserFile = OptUtil.GetValue("SlideShow.WebBrowserFile")
        data.simpleClockDisplayed = OptUtil.GetValue("SlideShow.SimpleClockDisplayed", false)
        data.simpleClockDisplayedOnlyOntheHour =
            OptUtil.GetValue("SlideShow.SimpleClockDisplayedOnlyOntheHour", true)
        data.SlideProgressBarDisplayed =
            OptUtil.GetValue("SlideShow.SlideProgressBarDisplayed", false)
        data.SlideProgressBarColor = OptUtil.GetValue("SlideShow.SlideProgressBarColor", "Red")
        data.OperationInfoDebugged = OptUtil.GetValue("SlideShow.OperationInfoDebugged", false)
        data.TouchInfoLogged = OptUtil.GetValue("SlideShow.TouchInfoLogged", false)
        data.slideShowFadeInOutApplied = OptUtil.GetValue("SlideShow.DefaultFadeApplied", true)
        data.slideShowDefaultFadeTimeType = OptUtil.GetValue("SlideShow.DefaultFadeTimeType", "M")
        data.slideShowTransparentBGApplied =
            OptUtil.GetValue("SlideShow.TransparentBGApplied", false)
        data.slideShowDisplayAfterDuration =
            OptUtil.GetValue("SlideShow.DisplayAfterDuration", true)
        data.RoutineSchedPrefix = OptUtil.GetValue("SlideShow.RoutineSchedPrefix")
        data.autoDelSchedDays = OptUtil.GetValue("SlideShow.AutoDelSchedDays", 0)
        data.monBeginTime = OptUtil.GetValue("Playtime.MonBeginTime")
        data.monEndTime = OptUtil.GetValue("Playtime.MonEndTime")
        data.tueBeginTime = OptUtil.GetValue("Playtime.TueBeginTime")
        data.tueEndTime = OptUtil.GetValue("Playtime.TueEndTime")
        data.wedBeginTime = OptUtil.GetValue("Playtime.WedBeginTime")
        data.wedEndTime = OptUtil.GetValue("Playtime.WedEndTime")
        data.thuBeginTime = OptUtil.GetValue("Playtime.ThuBeginTime")
        data.thuEndTime = OptUtil.GetValue("Playtime.ThuEndTime")
        data.friBeginTime = OptUtil.GetValue("Playtime.FriBeginTime")
        data.friEndTime = OptUtil.GetValue("Playtime.FriEndTime")
        data.satBeginTime = OptUtil.GetValue("Playtime.SatBeginTime")
        data.satEndTime = OptUtil.GetValue("Playtime.SatEndTime")
        data.sunBeginTime = OptUtil.GetValue("Playtime.SunBeginTime")
        data.sunEndTime = OptUtil.GetValue("Playtime.SunEndTime")
        data.managerTcpPort = OptUtil.GetValue("Communication.ManagerTcpPort", 11002)
        data.stbUdpPort = OptUtil.GetValue("Communication.StbUdpPort", 11001)
        data.weatherCity = OptUtil.GetValue("Weather.City", data.weatherCity)
        data.weatherCityDisplay = OptUtil.GetValue("Weather.Display", data.weatherCityDisplay)
        data.weatherUnit = OptUtil.GetValue("Weather.Unit", data.weatherUnit)
        data.weatherApiKey = OptUtil.GetValue("Weather.ApiKey", data.weatherApiKey)
        data.holidays = OptUtil.ReadOtherDateTimeOptions(5)
        data.TempLogDt = OptUtil.OptDateTimeValue(OptUtil.GetValue("Temp.DebugDueTime"))
        data.Volumes = OptUtil.GetValue("Volume")
        data.optionCardServer = OptUtil.GetValue("Communication.cardServer")
        data.optionCardport = OptUtil.GetValue("Communication.cardport", "1234")
        return data
    }

    fun getStbInfoFromServer(
        pullParser: XmlPullParser,
        env: StbOptionEnv
    ): StbOptionEnv {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "stbid" -> env.stbId = Integer.valueOf(pullParser.getAttributeValue(i))
                "stbname" -> env.stbName = pullParser.getAttributeValue(i)
                "stbudpport" -> env.stbUdpPort =
                    Integer.valueOf(pullParser.getAttributeValue(i))
                "stbservicetype" -> env.stbServiceType = pullParser.getAttributeValue(i)
                "ftphost" -> env.ftpHost = pullParser.getAttributeValue(i)
                "ftpport" -> env.ftpPort =
                    Integer.valueOf(pullParser.getAttributeValue(i))
                "ftpuser" -> env.ftpUser = pullParser.getAttributeValue(i)
                "ftppassword" -> env.ftpPassword = pullParser.getAttributeValue(i)
                "serverhost" -> env.serverHost = pullParser.getAttributeValue(i)
                "serverport" -> env.serverPort =
                    Integer.valueOf(pullParser.getAttributeValue(i))
                "serverukid" -> env.serverUkid = pullParser.getAttributeValue(i)
            }
        }
        return env
    }

    fun applyPropAndOptFromServer(pullParser: XmlPullParser?): Boolean {
        return true
    }

    fun onGetPlayerConfigData(env: SCEnvironment, tag: String?, text: String?): SCEnvironment {
        when (tag) {
            "Lang" -> env.defaultLang = text
            "Region" -> env.defaultRegion = text
            "SlideShow.GuideImageFile" -> env.introductionImgFile = text
            "Communication.ManagerTcpPort" -> env.managerTcpPort = Integer.valueOf(text)
            "Communication.StbUdpPort" -> env.stbUdpPort = Integer.valueOf(text)
            "Playtime.FriBeginTime" -> env.friBeginTime = text
            "Playtime.FriEndTime" -> env.friEndTime = text
            "Playtime.MonBeginTime" -> env.monBeginTime = text
            "Playtime.MonEndTime" -> env.monEndTime = text
            "Playtime.SatBeginTime" -> env.satBeginTime = text
            "Playtime.SatEndTime" -> env.satEndTime = text
            "Playtime.SunBeginTime" -> env.sunBeginTime = text
            "Playtime.SunEndTime" -> env.sunEndTime = text
            "Playtime.ThuBeginTime" -> env.thuBeginTime = text
            "Playtime.ThuEndTime" -> env.thuEndTime = text
            "Playtime.TueBeginTime" -> env.tueBeginTime = text
            "Playtime.TueEndTime" -> env.tueEndTime = text
            "Playtime.WedBeginTime" -> env.wedBeginTime = text
            "Playtime.WedEndTime" -> env.wedEndTime = text
            "SlideShow.AfterClosingAction" -> env.afterClosingAction = text
            "SlideShow.AllSchedulePlayMode" -> env.allSchedulePlayMode =
                java.lang.Boolean.valueOf(text)
            "SlideShow.AutoDelSchedDays" -> env.autoDelSchedDays = Integer.valueOf(text)
            "SlideShow.ClosingDelaySecs" -> env.closingDelaySecs = Integer.valueOf(text)
            "SlideShow.ConnectionCheck" -> env.connectionCheckingRequired =
                java.lang.Boolean.valueOf(text)
            "SlideShow.ConnectionErrorDisplayed" -> env.displayConnectionError =
                java.lang.Boolean.valueOf(text)
            "SlideShow.CustomArgs" -> env.customExeArgs = text
            "SlideShow.CustomExec" -> env.customExeFile = text
            "SlideShow.DefaultFadeApplied" -> env.slideShowFadeInOutApplied =
                java.lang.Boolean.valueOf(text)
            "SlideShow.DefaultFadeTimeType" -> env.slideShowDefaultFadeTimeType = text
            "SlideShow.DisplayAfterDuration" -> env.slideShowDisplayAfterDuration =
                java.lang.Boolean.valueOf(text)
            "SlideShow.KioskAutoStartSecs" -> env.kioskAutoStartSecs = Integer.valueOf(text)
            "SlideShow.KioskStartOnPlayerStartUp" -> env.kioskStartOnPlayerStartUp =
                java.lang.Boolean.valueOf(text)
            "SlideShow.MonitorAction" -> env.monitorAction = text
            "SlideShow.OperationInfoDebugged" -> env.OperationInfoDebugged =
                java.lang.Boolean.valueOf(text)
            "SlideShow.PptViewerFile" -> env.powerPointViewerFile = text
            "SlideShow.RestartHours" -> env.restartHours = text
            "SlideShow.RoutineSchedPrefix" -> env.RoutineSchedPrefix = text
            "SlideShow.ScheduleFile" -> env.defaultScheduleFile = text
            "SlideShow.ScreenExtMode" -> env.screenExtMode = text
            "SlideShow.SimpleClockDisplayed" -> env.simpleClockDisplayed =
                java.lang.Boolean.valueOf(text)
            "SlideShow.SimpleClockDisplayedOnlyOntheHour" -> env.simpleClockDisplayedOnlyOntheHour =
                java.lang.Boolean.valueOf(text)
            "SlideShow.SleepSecs" -> env.sleepSecs = Integer.valueOf(text)
            "SlideShow.SlideProgressBarColor" -> env.SlideProgressBarColor = text
            "SlideShow.SlideProgressBarDisplayed" -> env.SlideProgressBarDisplayed =
                java.lang.Boolean.valueOf(text)
            "SlideShow.TestNoticeDisplayed" -> env.testNoticeDisplayed =
                java.lang.Boolean.valueOf(text)
            "SlideShow.TestNoticeText" -> env.testNoticeText = text
            "SlideShow.TimeSyncRequired" -> env.timeSyncRequired = java.lang.Boolean.valueOf(text)
            "SlideShow.TopmostDisplayed" -> env.topmostDisplayed = java.lang.Boolean.valueOf(text)
            "SlideShow.TouchInfoLogged" -> env.TouchInfoLogged = java.lang.Boolean.valueOf(text)
            "SlideShow.TransparentBGApplied" -> env.slideShowTransparentBGApplied =
                java.lang.Boolean.valueOf(text)
            "SlideShow.WebBrowserFile" -> env.webBrowserFile = text
            "Temp.DebugDueTime" -> {}
            "Volume" -> env.defaultContentVolume = Integer.valueOf(text)
            "Weather.ApiKey" -> env.weatherApiKey = text
            "Weather.City" -> env.weatherCity = text
            "Weather.Display" -> env.weatherCityDisplay = text
            "Weather.Unit" -> env.weatherUnit = text
            "SlideShow.StoreMenuFile" -> env.defaultMenuFile = text
            "Communication.cardServer" -> env.optionCardServer = text
            "Communication.cardport" -> env.optionCardport = text
            else -> {}
        }
        return env
    }

    fun parsePlayerOptionXML(
        fileName: String?,
        option: PlayerOptionEnv,
        c: Context
    ): PlayerOptionEnv {
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
            Log.d("XmlOptionParser", "5. Error in ParseXML()", var12)
            option
        }
    }

    fun parseAgentOptionXML(fileName: String, stbOption: StbOptionEnv, c: Context): StbOptionEnv {
        return try {
            val fXmlFile = File(fileName)
            Log.d("XmlOptionParser", "parseAgentOptionXML fileName=$fileName")
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val dBuilder = factory.newDocumentBuilder()
            val doc = dBuilder.parse(fXmlFile)
            doc.documentElement.normalize()
            val nList = doc.getElementsByTagName("Option")
            for (temp in 0 until nList.length) {
                val nNode = nList.item(temp)
                Log.d("XmlOptionParser", "Current Element :" + nNode.nodeName)
                if (nNode.nodeType.toInt() == 1) {
                    val eElement = nNode as Element
                    stbOption.ftpActiveMode = java.lang.Boolean.valueOf(
                        eElement.getElementsByTagName(
                            c.resources.getString(R.string.ftpActiveMode)
                        ).item(0).textContent
                    )
                    stbOption.monitorMins = Integer.valueOf(
                        eElement.getElementsByTagName(c.resources.getString(R.string.monitorMins))
                            .item(0).textContent
                    )
                    stbOption.playerStart = java.lang.Boolean.valueOf(
                        eElement.getElementsByTagName(
                            c.resources.getString(R.string.playerStart)
                        ).item(0).textContent
                    )
                    stbOption.ftpHost =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverftpHost))
                            .item(0).textContent
                    stbOption.ftpPassword =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverftpPassword))
                            .item(0).textContent
                    stbOption.ftpPort = Integer.valueOf(
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverftpPort))
                            .item(0).textContent
                    )
                    stbOption.ftpUser =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverftpUser))
                            .item(0).textContent
                    stbOption.serverHost =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverserverHost))
                            .item(0).textContent
                    stbOption.serverPort = Integer.valueOf(
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverserverPort))
                            .item(0).textContent
                    )
                    stbOption.serverUkid =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverserverUkid))
                            .item(0).textContent
                    stbOption.stbId = Integer.valueOf(
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverstbId))
                            .item(0).textContent
                    )
                    stbOption.stbName =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverstbName))
                            .item(0).textContent
                    stbOption.stbServiceType =
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverstbServiceType))
                            .item(0).textContent
                    stbOption.stbUdpPort = Integer.valueOf(
                        eElement.getElementsByTagName(c.resources.getString(R.string.serverstbUdpPort))
                            .item(0).textContent
                    )
                    stbOption.storeName =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_store_name))
                            .item(0).textContent
                    stbOption.storeAddr =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_store_addr))
                            .item(0).textContent
                    stbOption.storeBusinessNum =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_business_num))
                            .item(0).textContent
                    stbOption.storeTel =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_store_tel))
                            .item(0).textContent
                    stbOption.storeMerchantNum =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_merchant_num))
                            .item(0).textContent
                    stbOption.storeCatId =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_store_catid))
                            .item(0).textContent
                    stbOption.storeRepresent =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_represent))
                            .item(0).textContent
                    stbOption.storeId =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_store_id))
                            .item(0).textContent
                    stbOption.deviceId =
                        eElement.getElementsByTagName(c.resources.getString(R.string.server_device_id))
                            .item(0).textContent
                    stbOption.mainPrtEnable =
                        eElement.getElementsByTagName(c.resources.getString(R.string.main_print_enable))
                            .item(0).textContent
                    stbOption.mainPrtip =
                        eElement.getElementsByTagName(c.resources.getString(R.string.main_print_ip))
                            .item(0).textContent
                }
            }
            stbOption
        } catch (var12: Exception) {
            Log.d("XmlOptionParser", "3. Error in ParseXML()", var12)
            stbOption
        }
    }

    fun ParseXML(
        fls: String,
        c: Context,
        prop: PropUtil,
        stbOpt: StbOptionEnv,
        commandList: List<CommandObject?>,
        downloadList: MutableList<DownFileInfo?>,
        newcommandList: MutableList<CommandObject?>?
    ): Boolean {
        var newcommandList = newcommandList
        var cmd: CommandObject? = null
        var addCmd = true
        val optKey: MutableList<String?> = arrayListOf()
        val optVal: MutableList<String?> = arrayListOf()
        var onMakeConfigData = false
        var onMakeConfigDataEdition = false
        val optEditionKey: MutableList<String?> = arrayListOf()
        val optEditionVal: MutableList<String?> = arrayListOf()
        var result = false
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            val `is`: InputStream = ByteArrayInputStream(fls.toByteArray())
            if (`is` == null) {
                Log.e("XmlOptionParser", "InputStream... null!!!!")
                return result
            }
            parser.setInput(`is`, null as String?)
            var eventType = parser.eventType
            val isItemTag = false
            var downloadInfo: DownFileInfo? = null
            while (eventType != 1) {
                var name: String?
                when (eventType) {
                    0 -> name = parser.name
                    1 -> Log.d("XmlOptionParser", "EVENT TYPE = $eventType")
                    2 -> {
                        name = parser.name
                        if (name == c.resources.getString(R.string.server)) {
                            name = parser.getAttributeValue(null as String?, "ref")
                            var i = 0
                            while (i < parser.attributeCount) {
                                val attrName = parser.getAttributeName(i)
                                if (attrName == c.resources.getString(R.string.ftpActiveMode)) {
                                    stbOpt.ftpActiveMode =
                                        java.lang.Boolean.valueOf(parser.getAttributeValue(i))
                                } else if (attrName == c.resources.getString(R.string.monitorMins)) {
                                    stbOpt.monitorMins =
                                        Integer.valueOf(parser.getAttributeValue(i))
                                } else if (attrName == c.resources.getString(R.string.playerStart)) {
                                    stbOpt.playerStart =
                                        java.lang.Boolean.parseBoolean(parser.getAttributeValue(i))
                                } else if (attrName == c.resources.getString(R.string.ftpHost)) {
                                    stbOpt.ftpHost = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.ftpPassword)) {
                                    stbOpt.ftpPassword = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.ftpPort)) {
                                    stbOpt.ftpPort = Integer.valueOf(parser.getAttributeValue(i))
                                } else if (attrName == c.resources.getString(R.string.ftpUser)) {
                                    stbOpt.ftpUser = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.serverHost)) {
                                    stbOpt.serverHost = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.serverPort)) {
                                    stbOpt.serverPort = Integer.valueOf(parser.getAttributeValue(i))
                                } else if (attrName == c.resources.getString(R.string.serverUkid)) {
                                    stbOpt.serverUkid = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.stbId)) {
                                    stbOpt.stbId = Integer.valueOf(parser.getAttributeValue(i))
                                    if (stbOpt.stbId > 0 && stbOpt.stbStatus == 0) {
                                        stbOpt.stbStatus = 5
                                    }
                                } else if (attrName == c.resources.getString(R.string.stbName)) {
                                    stbOpt.stbName = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.stbServiceType)) {
                                    stbOpt.stbServiceType = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.stbUdpPort)) {
                                    stbOpt.stbUdpPort = Integer.valueOf(parser.getAttributeValue(i))
                                } else if (attrName == c.resources.getString(R.string.server_store_name)) {
                                    stbOpt.storeName = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_store_addr)) {
                                    stbOpt.storeAddr = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_business_num)) {
                                    stbOpt.storeBusinessNum = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_store_tel)) {
                                    stbOpt.storeTel = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_merchant_num)) {
                                    stbOpt.storeMerchantNum = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_store_catid)) {
                                    stbOpt.storeCatId = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_represent)) {
                                    stbOpt.storeRepresent = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_store_id)) {
                                    stbOpt.storeId = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.server_device_id)) {
                                    stbOpt.deviceId = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.main_print_enable)) {
                                    stbOpt.mainPrtEnable = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.main_print_ip)) {
                                    stbOpt.mainPrtip = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.ko_enabled)) {
                                    stbOpt.koEnable = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.at_enabled)) {
                                    stbOpt.atEnable = parser.getAttributeValue(i)
                                } else if (attrName == c.resources.getString(R.string.openType)) {
                                    stbOpt.openType = parser.getAttributeValue(i)
                                }
                                ++i
                            }
                            FileUtils.updateFile(
                                FileUtils.BBMC_PAYCAST_DATA_DIRECTORY + FileUtils.AGENT_OPT_FILE,
                                c,
                                stbOpt
                            )
                        } else if (name == c.resources.getString(R.string.properties_tag)) {
                            onMakeConfigData = true
                        } else if (name == c.resources.getString(R.string.editionProperties)) {
                            onMakeConfigDataEdition = true
                        } else {
                            var i: Int
                            var attrName: String
                            if (name == "command") {
                                cmd = if (parser.attributeCount == 0) {
                                    if (stbOpt.stbStatus == 2) {
                                    }
                                    null
                                } else {
                                    CommandObject()
                                }
                                i = 0
                                while (i < parser.attributeCount) {
                                    attrName = parser.getAttributeName(i)
                                    if (attrName == "rcCommandId") {
                                        cmd!!.rcCommandid = parser.getAttributeValue(i)
                                    } else if (attrName == "command") {
                                        cmd!!.command = parser.getAttributeValue(i)
                                    } else if (attrName == "execTime") {
                                        cmd!!.execTime = parser.getAttributeValue(i)
                                    } else if (attrName == "CDATA") {
                                        cmd!!.execTime = parser.getAttributeValue(i)
                                    }
                                    ++i
                                }
                                val var38: Iterator<*> = commandList.iterator()
                                while (var38.hasNext()) {
                                    val cobj = var38.next() as CommandObject
                                    if (cmd!!.rcCommandid != null && !cmd.command.isEmpty()) {
                                        if (cobj.rcCommandid !== cmd.rcCommandid) {
                                            continue
                                        }
                                        addCmd = false
                                        break
                                    }
                                    addCmd = false
                                    break
                                }
                                if (cmd == null) {
                                    addCmd = false
                                }
                            } else if (name != "![CDATA") {
                                if (name != c.resources.getString(R.string.item)) {
                                    if (name == c.getString(R.string.updateFTPServerURL)) {
                                        if (onMakeConfigData) {
                                            optKey.add(name)
                                        }
                                    } else if (name == c.getString(R.string.updateInfoURL)) {
                                        if (onMakeConfigData) {
                                            optKey.add(name)
                                        }
                                    } else if (onMakeConfigData) {
                                        optKey.add(name)
                                    } else if (onMakeConfigDataEdition) {
                                        optEditionKey.add(name)
                                    }
                                } else {
                                    downloadInfo = DownFileInfo()
                                    i = 0
                                    while (i < parser.attributeCount) {
                                        attrName = parser.getAttributeName(i)
                                        if (attrName == c.resources.getString(R.string.foldername)) {
                                            downloadInfo.folderName =
                                                stbOpt.storeCatId + parser.getAttributeValue(i)
                                        } else if (attrName == c.resources.getString(R.string.filename)) {
                                            downloadInfo.fileName = parser.getAttributeValue(i)
                                        } else if (attrName == c.resources.getString(R.string.filelength)) {
                                            downloadInfo.fileLength =
                                                parser.getAttributeValue(i).toLong()
                                        } else if (attrName == c.resources.getString(R.string.stbfileid)) {
                                            downloadInfo.stbfileid =
                                                Integer.valueOf(parser.getAttributeValue(i))
                                            if (downloadInfo.stbfileid == -1) {
                                                downloadInfo.scheduleContent = false
                                            } else {
                                                downloadInfo.scheduleContent = true
                                                downloadInfo.downFileId = downloadInfo.stbfileid
                                            }
                                        } else if (attrName == c.resources.getString(R.string.kfileid)) {
                                            downloadInfo.kfileid =
                                                Integer.valueOf(parser.getAttributeValue(i))
                                            if (downloadInfo.stbfileid == -1) {
                                                downloadInfo.downFileId = downloadInfo.kfileid
                                            }
                                        } else if (attrName == c.resources.getString(R.string.kroot)) {
                                            downloadInfo.kroot = parser.getAttributeValue(i)
                                        } else if (attrName == c.resources.getString(R.string.playatonce)) {
                                            downloadInfo.playatonce = parser.getAttributeValue(i)
                                        }
                                        ++i
                                    }
                                    var addDown = true
                                    val var33: Iterator<*> = downloadList.iterator()
                                    while (var33.hasNext()) {
                                        val d = var33.next() as DownFileInfo
                                        if (d.fileName == downloadInfo.fileName && d.folderName == downloadInfo.folderName) {
                                            addDown = false
                                            break
                                        }
                                    }
                                    if (addDown && downloadInfo.fileName != null && downloadInfo.fileLength > 0L) {
                                        downloadList.add(downloadInfo)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        name = parser.name
                        if (name == "command") {
                            if (cmd != null && addCmd) {
                                if (newcommandList == null) {
                                    newcommandList = arrayListOf()
                                }
                                (newcommandList as MutableList<CommandObject>).add(cmd)
                                Utils.LOG("--------------------")
                                Utils.LOG(c.getString(R.string.Log_CmdAdditionalCommand) + " #: " + cmd.rcCommandid)
                                Utils.LOG(c.getString(R.string.Log_CmdCommandName) + ": " + cmd.command)
                                Utils.LOG(c.getString(R.string.Log_CmdExecTime) + ": " + cmd.execTime)
                                Utils.LOG(c.getString(R.string.Log_CmdParameter) + ": " + cmd.prameter)
                                Utils.LOG(c.getString(R.string.Log_CmdTotalCommandCount) + ": " + (newcommandList as List<*>).size)
                                Utils.LOG("--------------------")
                            }
                        } else if (name == c.resources.getString(R.string.properties_tag)) {
                            onMakeConfigData = false
                        } else if (name == c.resources.getString(R.string.editionProperties)) {
                            onMakeConfigDataEdition = false
                        } else {
                            if (name != "SignCast") {
                                if (name == c.resources.getString(R.string.items)) {
                                    Log.e(
                                        "XmlOptionParser",
                                        "1 executeSyncContent() checkContentFileExistence()"
                                    )
                                }
                                eventType = parser.next()
                                continue
                            }
                            val options: HashMap<String?, String?> = HashMap()
                            val optionsEdition: HashMap<String?, String?> = HashMap()
                            var i: Int
                            if (optKey.size > 0) {
                                i = 0
                                while (i < optKey.size && i < optVal.size) {
                                    options[optKey[i]] = optVal[i]
                                    ++i
                                }
                            }
                            if (optEditionKey.size > 0) {
                                i = 0
                                while (i < optEditionKey.size && i < optEditionVal.size) {
                                    optionsEdition[optEditionKey[i]] = optEditionVal[i]
                                    ++i
                                }
                            }
                            prop.onSaveConfigFile(options, optionsEdition)
                        }
                    }
                    4 -> {
                        name = parser.text
                        if (name != null && !name.isEmpty()) {
                            if (cmd != null) {
                                cmd.prameter = name
                            } else if (onMakeConfigData) {
                                optVal.add(name)
                            } else if (onMakeConfigDataEdition) {
                                optEditionVal.add(name)
                            }
                        }
                    }
                    else -> Log.d("XmlOptionParser", "EVENT TYPE = $eventType")
                }
                eventType = parser.next()
            }
            result = true
        } catch (var29: Exception) {
            Log.d("XmlOptionParser", "2 Error in ParseXML()", var29)
            result = false
        }
        return result
    }

    fun getStoreCatagory(
        pullParser: XmlPullParser,
        env: MenuCatagoryObject
    ): MenuCatagoryObject {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "storeId" -> env.storeId = pullParser.getAttributeValue(i)
                "storename" -> env.storename = pullParser.getAttributeValue(i)
                "storeimage" -> env.storeImage = pullParser.getAttributeValue(i)
                "menutype" -> env.menutype = pullParser.getAttributeValue(i)
                "background" -> env.storebackground = pullParser.getAttributeValue(i)
                "catatorynum" -> env.catagorynum = pullParser.getAttributeValue(i)
                "unit" -> env.unit = pullParser.getAttributeValue(i)
            }
        }
        return env
    }

    fun getMenuItem(
        pullParser: XmlPullParser,
        env: MenuObject
    ): MenuObject {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "id" -> env.productId = pullParser.getAttributeValue(i)
                "seq" -> env.seq = pullParser.getAttributeValue(i)
                "name" -> env.name = pullParser.getAttributeValue(i)
                "price" -> {
                    env.price = pullParser.getAttributeValue(i)
                    env.imagefile = pullParser.getAttributeValue(i)
                }
                "code" -> env.code = pullParser.getAttributeValue(i)
                "groupName" -> env.groupName = pullParser.getAttributeValue(i)
                "discount" -> env.discount = kotlin.runCatching { pullParser.getAttributeValue(i).toFloat() }.getOrDefault(1F)
                "file" -> env.imagefile = pullParser.getAttributeValue(i)
                "description" -> env.description = pullParser.getAttributeValue(i)
                "popular" -> env.popular = pullParser.getAttributeValue(i)
                "newmenu" -> env.newmenu = pullParser.getAttributeValue(i)
                "soldout" -> env.soldout = pullParser.getAttributeValue(i)
                "refill" -> env.refill = pullParser.getAttributeValue(i)
            }
        }
        return env
    }

    fun getMenuCatagory(
        pullParser: XmlPullParser,
        env: CatagoryObject
    ): CatagoryObject {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "seq" -> env.seq = pullParser.getAttributeValue(i)
                "name" -> env.name = pullParser.getAttributeValue(i)
                "image" -> env.image = pullParser.getAttributeValue(i)
                "menunum" -> env.menuNum = pullParser.getAttributeValue(i)
            }
        }
        return env
    }

    @Throws(JSONException::class)
    fun onParseOrderMenuPrintJson(orderPrtStr: String?): ArrayList<KioskOrderInfoForPrint?>? {
        val orderInfoList: ArrayList<KioskOrderInfoForPrint?> = arrayListOf()
        val json = JSONArray(orderPrtStr)
        return if (json != null && json.length() != 0) {
            for (i in 0 until json.length()) {
                val order = json.getJSONObject(i)
                val printMenu = KioskOrderInfoForPrint()
                printMenu.recommandId = order.getString("recommand")
                if (printMenu.recommandId == "0") {
                    return orderInfoList
                }
                printMenu.goodsAmt = order.getString("goodsAmt")
                printMenu.orderDate = order.getString("orderDate")
                printMenu.orderNumber = order.getString("orderNumber")
                printMenu.storeName = order.getString("storeName")
                if (printMenu.menuItems == null) {
                    printMenu.menuItems = arrayListOf()
                }
                val jsonMenu = order.getJSONArray("orderMenu")
                for (j in 0 until jsonMenu.length()) {
                    val menu = jsonMenu.getJSONObject(j)
                    val item = KioskOrderInfoForPrint.OrderMenuItem()
                    item.orderPrice = menu.getString("orderPrice")
                    item.productName = menu.getString("productName")
                    item.productId = menu.getString("productID")
                    item.orderCount = menu.getString("orderCount")
                    printMenu.menuItems.add(item)
                }
                orderInfoList.add(printMenu)
            }
            orderInfoList
        } else {
            null
        }
    }

    fun getMenuItemOption(pullParser: XmlPullParser, env: MenuOptionData): MenuOptionData? {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "id" -> env.menuOptId = pullParser.getAttributeValue(i)
                "seq" -> env.menuOptSeq = pullParser.getAttributeValue(i)
                "name" -> env.menuOptName = pullParser.getAttributeValue(i)
                "gubun" -> env.menuGubun = pullParser.getAttributeValue(i)
            }
        }
        return if (env.menuOptId != null && env.menuOptSeq != null && env.menuOptName != null && env.menuGubun != null) {
            if (!env.menuOptId.isEmpty() && !env.menuOptSeq.isEmpty() && !env.menuOptName.isEmpty() && !env.menuGubun.isEmpty()) {
                env
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getMenuItemOption(
        pullParser: XmlPullParser,
        env: CustomAddedOptionData
    ): CustomAddedOptionData? {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "price" -> env.optMenuPrice = pullParser.getAttributeValue(i)
                "name" -> env.optMenuName = pullParser.getAttributeValue(i)
            }
        }
        return if (env.optMenuName != null && env.optMenuPrice != null) {
            if (!env.optMenuPrice.isEmpty() && !env.optMenuName.isEmpty()) {
                env
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getMenuItemOption(
        pullParser: XmlPullParser,
        env: CustomRequiredOptionData
    ): CustomRequiredOptionData? {
        for (i in 0 until pullParser.attributeCount) {
            when (pullParser.getAttributeName(i)) {
                "price" -> env.optMenuPrice = pullParser.getAttributeValue(i)
                "name" -> env.optMenuName = pullParser.getAttributeValue(i)
            }
        }
        return if (env.optMenuName != null && env.optMenuPrice != null) {
            if (!env.optMenuPrice.isEmpty() && !env.optMenuName.isEmpty()) {
                env
            } else {
                null
            }
        } else {
            null
        }
    }

    companion object {
        private const val TAG = "XmlOptionParser"
        private const val LOG = false
        fun MountPointFromSchedule(fullFilename: String?): String? {
            val fInfo = File(fullFilename)
            var mountdir = ""
            if (fInfo.exists()) {
                try {
                    val factory = DocumentBuilderFactory.newInstance()
                    factory.isNamespaceAware = true
                    val dBuilder = factory.newDocumentBuilder()
                    val doc = dBuilder.parse(fInfo)
                    doc.documentElement.normalize()
                    val nList = doc.getElementsByTagName("SignCast")
                    for (temp in 0 until nList.length) {
                        val nNode = nList.item(temp)
                        Log.d("XmlOptionParser", "Current Element :" + nNode.nodeName)
                        if (nNode.nodeType.toInt() == 1) {
                            val eElement = nNode as Element
                            mountdir = eElement.getAttribute("mountdir")
                            if (mountdir != null && !mountdir.isEmpty()) {
                                break
                            }
                        }
                    }
                } catch (var10: Exception) {
                    Utils.debugException("SignCastModel.MountPointFromSchedule", var10)
                }
            }
            return mountdir
        }
    }
}