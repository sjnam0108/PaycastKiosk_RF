package kr.co.bbmc.paycast.data.repository.remote

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kr.co.bbmc.paycast.App
import kr.co.bbmc.paycast.network.ApiProvider
import kr.co.bbmc.paycast.network.model.*
import kr.co.bbmc.paycast.storeId
import kr.co.bbmc.selforderutil.ProductInfo.deviceId

@FlowPreview
@Suppress("unused")
class KioskRepository {

    private val prefManager = App.APP.dataStore

    private val baseApiProvider by lazy { ApiProvider.getBaseApi() }

    fun getLastOrderNum(): Int = 0
    // Kiosk 락 코드
    fun getPayCastLockCode(): Int = 0

    // Remote data
    // file download api
    suspend fun getStoreState(): Flow<ResServerSync> = flow {
        emit(baseApiProvider.getStoreState(storeId = storeId, deviceId = deviceId))
    }.debounce(1000L)

    suspend fun getStoreStateRes() = baseApiProvider.getStoreState(storeId = storeId, deviceId = deviceId)

    suspend fun getOrderNumber(): Flow<ResOrderNumber> = flow {
        emit(baseApiProvider.requestOrderNumber(storeId = storeId, deviceId = deviceId))
    }

    suspend fun getOrderCookCount(): Flow<ResCookCount> = flow {
        emit(baseApiProvider.requestCookCount(storeId = storeId, deviceId = deviceId))
    }

    suspend fun getCancelInfo(cancelNumber: String): Flow<ResPayCancel> = flow {
        emit(baseApiProvider.getCancelInfo(storeId = storeId, deviceId = deviceId, cancelCode = cancelNumber))
    }

    suspend fun getMenuInfo() : Flow<ResMenuInfo> = flow {
        emit(baseApiProvider.requestMenuInfo(storeId = storeId))
    }

    suspend fun getPrintMenuInfo() = flow {
        emit(baseApiProvider.getPrintMenu(storeId = storeId, deviceId = deviceId))
    }
}