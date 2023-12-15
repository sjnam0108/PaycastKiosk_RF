package kr.co.bbmc.paycast.presentation.paymentKicc

import android.annotation.SuppressLint
import com.orhanobut.logger.Logger
import org.apache.commons.codec.binary.Base64
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

var Mid = "t92311158m" // 발급받은 테스트 Mid 설정(Real 전환 시 운영 Mid 설정)
var MerchantKey = "0/4GFsSd7ERVRGX9WHOzJ96GyeMTwvIaKSWUCKmN3fDklNRGw3CualCFoMPZaS99YiFGOuwtzTkrLo4bR4V+Ow==" // 발급받은 테스트 상점키 설정(Real 전환 시 운영 상점키 설정)
var EdiDate = getyyyyMMddHHmmss()
var Amt = "1004"
var EncryptData = encodeSHA256Base64(EdiDate + Mid + Amt + MerchantKey)
var today = getyyyyMMddHHmm() // 현재일자. 캐시방지용으로 사용

/* SHA256 암호화 */
fun encodeSHA256Base64(strPW: String): String {
    var passACL: String? = null
    var md: MessageDigest? = null
    try {
        md = MessageDigest.getInstance("SHA-256")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    requireNotNull(md) {
        Logger.e("Error encodeSHA256Base64 : NPE")
        return ""
    }
    md.update(strPW.toByteArray())
    val raw: ByteArray = md.digest()
    val encodedBytes: ByteArray = Base64.encodeBase64(raw)
    passACL = String(encodedBytes)
    return passACL
}

/* 현재일자 */
@SuppressLint("SimpleDateFormat")
fun getyyyyMMddHHmmss(): String {
    val yyyyMMddHHmmss = SimpleDateFormat("yyyyMMddHHmmss")
    return yyyyMMddHHmmss.format(Date())
}

/* 현재일자  */
@SuppressLint("SimpleDateFormat")
fun getyyyyMMddHHmm(): String {
    val yyyyMMddHHmm = SimpleDateFormat("yyyyMMddHHmm")
    return yyyyMMddHHmm.format(Date())
}