package kr.co.bbmc.paycast.util

import android.content.ContentValues
import android.os.Environment
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kr.co.bbmc.paycast.App.Companion.APP
import kr.co.bbmc.paycast.BANNER_FILE_DIRECTORY
import kr.co.bbmc.paycast.R
import kr.co.bbmc.paycast.menuDownloadFinished
import kr.co.bbmc.paycast.network.model.DownloadFiles
import kr.co.bbmc.paycast.noticeSharedFlow
import kr.co.bbmc.paycast.service.isDownloading
import kr.co.bbmc.selforderutil.*
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * 앱 하위 폴더의 파일의 path 및 이름 획득
 */
fun getSubPathFile(filename: String): String = BANNER_FILE_DIRECTORY + filename

val BASE_ROOT = APP.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
/**
 * 앱 하위 폴더의 path 획득
 */
fun getSubPath(subDir: String): String {
    if (subDir.isNotEmpty()) {
        when (val subDirStr = subDir.lowercase(Locale.getDefault())) {
            "content", "data", "debug", "banner" -> return BASE_ROOT + File.separator + subDirStr
        }
    }
    return BASE_ROOT
}

suspend fun deleteInPathFolder(targetDir: File) {
    Logger.e("START DELETE FILE : $targetDir")
    CoroutineScope(Dispatchers.IO).launch {
        if (targetDir.exists()) {
            val files = targetDir.listFiles()
            files?.map {
                async {
                    //Logger.w("Delete - File : ${it.name}")
                    it.delete()
                }
            }?.forEach { it.await() }
        }

        Logger.e("Delete finished!!")
    }
}

/**
 * URL 경로의 파일을 다운받아 경로 파일로 저장
 */
suspend fun saveFileFromUrl(url: String?, pathFile: String?) {
    coroutineScope {
        var inputStream: BufferedInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        launch(Dispatchers.IO) {
            Logger.d("start save file download!! - $url")
            runCatching {
                inputStream = BufferedInputStream(URL(url).openStream())
                fileOutputStream = FileOutputStream(pathFile)
                val dataBuffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream!!.read(dataBuffer, 0, 1024).also { bytesRead = it } != -1) {
                    fileOutputStream!!.write(dataBuffer, 0, bytesRead)
                }
                inputStream!!.close()
                fileOutputStream!!.close()
                Logger.i("Download Finish!! - $url")
            }.onFailure {
                Logger.e("Error : ${it.message}")
            }
                .also {
                    try {
                        inputStream?.close()
                        fileOutputStream?.close()
                    } catch (e1: Exception) {
                        Logger.e("Error1 : ${e1.message}")
                    }
                }
        }
    }
}

// Banner 파일 저장
suspend fun fileDownloadTask(files: List<DownloadFiles>) =
    CoroutineScope(Dispatchers.IO + baseExceptionHandler() {
        Logger.e("File download failed!!! - $it")
    }).launch {
        val filePathName = File(BANNER_FILE_DIRECTORY)
        files.map { file ->
            async(Dispatchers.IO) {
                val filename: String = file.local_filename
                Logger.w("filePathName : $filePathName // fileName : $filename")
                val contentFile = File(filePathName.absolutePath + File.separator + filename)
//                Logger.e("contentFile name: ${contentFile.absoluteFile.name}")
                when (contentFile.exists() && (contentFile.length() == file.file_size) && file.file_size > 0) {
                    false -> {
                        Logger.w("1. Start download!! : ${contentFile.absoluteFile}");
                        saveFileFromUrl(file.url, contentFile.absolutePath)
                    }
                    else -> Logger.d("download Skip : ${contentFile.absoluteFile}")
                }
            }
        }.forEach {
            it.await()
        }

        launch {
            // 갱신된 파일중에 현재 로컬파일에 저장된 파일이 없을경우 삭제처리한다.
            val localFileList = files.map { it.local_filename }
            Logger.w("localFileList : $localFileList")
            deleteFile(localFileList)
        }.join()

        //val result = getLocalFolderFileList(filePathName)
        Logger.w("2. Download finished!!!")
    }

// 내부 저장소의 파일 목록 제거
fun CoroutineScope.deleteFile(arrFiles: List<String>) = launch(Dispatchers.IO) {
    //val localContentFolder = File(getSubPath("banner"))
    val localContentFolder = File(BANNER_FILE_DIRECTORY)
    Logger.w("Delete file path : ${localContentFolder.absolutePath}")   // /storage/emulated/0/BBMC/PAYCAST/banner/ | /storage/emulated/0/Android/data/kr.co.bbmc.paycast/files/Documents/banner
    if (localContentFolder.exists() && localContentFolder.isDirectory) {
        val localFiles = localContentFolder.listFiles()
        Logger.e("Target Local files : ${localFiles?.map { it.absoluteFile }}")
        if (localFiles.isNullOrEmpty()) return@launch
        for (file in localFiles) {
            if (!arrFiles.contains(file.name)) {
                launch {
                    val delPathFile = getSubPathFile(file.name)
                    Logger.w("delete target file : $delPathFile")
                    val result = deleteFile(delPathFile)
                    Logger.e("target file(${file.name}) deleted : $result!")
                }
            }
        }
    } else { Logger.d("File or Folder not exist!!") }
}

/**
 * 지정된 파일 삭제
 */
fun deleteFile(pathFile: String): Boolean {
    return runCatching {
        val file = File(pathFile)
        if(file.isFile) file.delete()
        true
    }.getOrDefault(false)
}


@FlowPreview
fun ftpFileDownloads(downloadList: MutableList<DownFileInfo>, callback: (String) -> Unit) {
    // variables
    var newSchedule = ""
    var playAtOnce = false
    var completeY = "|"

    val removeList = mutableListOf<DownFileInfo>()

    val targetUrl = APP.stbOpt?.ftpHost ?: ""
    val port = APP.stbOpt?.ftpPort ?: 80
    val user = APP.stbOpt?.ftpUser
    val password = APP.stbOpt?.ftpPassword
    val ftpUtil = FTPUtil()
    val propUtil = PropUtil()

    FTPUtil.ftpClient = FTPClient()
    isDownloading = true
    if (!FTPUtil().connect(targetUrl, port) || !FTPUtil().login(targetUrl, port, user, password)) {
        Logger.e("FTP Login Failed!! - $targetUrl // $port")
        isDownloading = false
        menuDownloadFinished.tryEmit(false)
        return
    }

    Logger.i("0. downLoad list : ${downloadList.size}")
    Logger.d("1. connect success - try login - $targetUrl:$port // user : $user // pw: $password")
    downloadList.forEach {
        val targetFile = File(FileUtils.BBMC_PAYCAST_DIRECTORY + it.LocalFolderName() + it.fileName)
        if (targetFile.exists()) {
            if (targetFile.length() != it.fileLength) {
                targetFile.delete()
            } else {
                removeList.add(it)
            }
        } else {
            val d = File(FileUtils.BBMC_PAYCAST_DIRECTORY + it.LocalFolderName())
            if (!d.exists()) {
                d.mkdir()
                Logger.d("Directory make dir = " + d.name)
            }
        }

        var fos: FileOutputStream? = null
        try {
            targetFile.createNewFile()
            fos = FileOutputStream(targetFile)
            FTPUtil.ftpClient.enterLocalPassiveMode()
            FTPUtil.ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
        } catch (e: IOException) {
            Logger.e("Error : FileOutputStream create Failed : ${e.message}")
            e.printStackTrace()
        }

        when (it.fileName == targetFile.name
                && targetFile.length() == it.fileLength //조건변경... 파일을 받기전에 항상 사이즈가 없음..
                && it.fileLength > 0) {
            false -> {
                if (it.fileName.endsWith(".self")) {
                    val s = it.folderName.replace("Menu/", "")
                    it.folderName = s
                }
                try {
                    val chresult = FTPUtil.ftpClient.changeWorkingDirectory("/" + it.folderName)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val result = FTPUtil.ftpClient.retrieveFile(it.fileName, fos)
                if (result) {
                    val size = it.fileLength
                    fos?.close()
                    if (size == targetFile.length()) it.completed = true
                } else {
                    it.completed = false
                    val log = String.format("FtpThread() file download fail name=%s", it.fileName)
                    FileUtils.writeLog(log, "PayCastLog")
                }
            }
            else -> Logger.d("1-2.download Skip : ${it.fileName}")
        }

        noticeSharedFlow.tryEmit("메뉴 파일을 다운로드 중입니다...")
        FTPUtil.ftpClient.changeWorkingDirectory("/")
        if (it.completed) {
            removeList.add(it)
            completeY += it.downFileId.toString() + "|"
            if (it.fileName.endsWith(".self") && (it.playatonce != null)) {
                playAtOnce = it.playatonce == "Y"
                if (playAtOnce) {
                    newSchedule = it.fileName
                }
            }
            val temp = reportServerSyncV2(
                completeY,
                "|",
                APP.stbOpt!!,
                propUtil,
                APP.applicationContext
            )

            val url = ServerReqUrl.getServerSyncContentReportUrl(
                APP.stbOpt,
                APP.applicationContext
            )
            val res = NetworkUtil.HttpResponseString(url, temp, propUtil)
            if (res.isNotEmpty()) {
                val result = res == "Y"
                if (result) {
                    completeY = "|"
                }
            }
        } else {
            try {
                Utils.LOG(APP.getString(R.string.Log_DownloadFile) + ": " + APP.getString(R.string.Log_Failure) + "(" + FTPUtil.ftpClient.printWorkingDirectory() + it.fileName)
            } catch (e: IOException) {
                e.printStackTrace()
                Logger.e(ContentValues.TAG, "ftp download fail.... 5 ")
            }
        }

        try {
            if (it.scheduleContent) FTPUtil.ftpClient.changeWorkingDirectory("/" + APP.stbOpt?.storeCatId + "/")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Logger.e("changeWorkingDirectory failed : ${e.message}")
        }
    }

    noticeSharedFlow.tryEmit("잠시만 기다려주세요...")
    // 5. 파일다운로드 완료시 로그아웃
    ftpUtil.logout()
    ftpUtil.disconnect()

    // 6. 파일삭제
    callback.invoke(newSchedule)
    isDownloading = false
    if (playAtOnce) {
        playAtOnce = false
        newSchedule = ""
    }
    menuDownloadFinished.tryEmit(true)
}