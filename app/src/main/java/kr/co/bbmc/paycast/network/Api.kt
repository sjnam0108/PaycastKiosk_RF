package kr.co.bbmc.paycast.network

import kr.co.bbmc.paycast.network.model.*
import kr.co.bbmc.paycast.presentation.mainMenu.model.ResPrintInfo
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    // 주문번호 요청 api - http://s.paycast.co.kr/store/api/ordernum?storeId=2&deviceId=BBMCS999
    @GET("/store/api/ordernum")
    suspend fun requestOrderNumber(
        @Query("storeId") storeId: Int,
        @Query("deviceId") deviceId: String,
    ): ResOrderNumber

    // http://s.paycast.co.kr/info/storechgsync?storeId=2&deviceId=BBMCS999
    
    // 주문대기 번호 요청
    @GET("/cookordercount")
    suspend fun requestCookCount(
        @Query("storeId") storeId: Int,
        @Query("deviceId") deviceId: String,
    ): ResCookCount

    // 서버 싱크
    @GET("/info/storeState")
    suspend fun getStoreState(
        @Query("storeId") storeId: Int,
        @Query("deviceId") deviceId: String,
    ): ResServerSync

    // 결제 취소
    @GET("/store/api/cancelVerifiCheck")
    suspend fun getCancelInfo(
        @Query("storeId") storeId: Int,
        @Query("deviceId") deviceId: String,
        @Query("cancelCode") cancelCode: String,
    ): ResPayCancel

    // 전자 메뉴 생성 정보
    @GET("/store/api/menuInfo")
    suspend fun requestMenuInfo(
        @Query("storeId") storeId: Int
    ): ResMenuInfo

    // 광고 리스트 정보
    @GET("/store/api/bannerInfo")
    suspend fun requestBannerInfo(
        @Query("storeId") storeId: Int
    ): ResBannerInfo

    @GET("/info/printmenu")
    suspend fun getPrintMenu(
        @Query("storeId") storeId: Int,
        @Query("deviceId") deviceId: String,
    ): List<ResPrintInfo>

}