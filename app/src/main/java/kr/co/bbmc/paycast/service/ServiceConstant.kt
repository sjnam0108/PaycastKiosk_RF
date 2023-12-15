package kr.co.bbmc.paycast.service

// network
const val CHECK_PACKAGE_TIMEOUT = 30000 //10000;   //5sec
const val MAX_FCM_TIMER = 60000 //1 min
const val MAX_CONNECTION_TIMEOUT = 10000 //10초
const val MAX_SOCKET_TIMEOUT = 10000 //10초

// log
const val TAG = "MainService"
const val LOG = true

//For communication with activity
const val CONNECT = 0
const val DISCONNECT = 1
const val SEND_VALUE = 2
const val SELF_SEND_VALUE = 3
const val SHOW_MESSAGE = 4
const val QUIT_MESSAGE = 5

// 다운로드 진행 중
@JvmField
var isDownloading = false
const val isTrackUploadTime = false
@JvmField
var kioskContentDownloading = false
const val isUploading = false //upload 진행 중
