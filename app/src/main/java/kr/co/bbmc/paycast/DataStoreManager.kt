package kr.co.bbmc.paycast

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "password_dataStore")
    // 비밀 번호 initial 값은 "0000" 이고 String 형태로 저장함
    // initial 값을 "0000" 으로 설정,
    // data Store 에도 이 값을 넣어 주어야 함,
    // CustomLockScreen ( 초기 Dialog ) 에서 확인을 누르면 data Store 에 있는 비밀 번호와 같은지 체크를 해야함
    // Change Password 를 했을 때는 새 비밀 번호 끼리만  체크를 하고 그 값이 서로 같으면 data Store 의 value 값을 변경 해주면 됨

    private val PASSWORD_KEY = stringPreferencesKey("key_name")

    /* 데이터 스토어 에 값 저장 하기 */
    suspend fun setPassword(pw: String) {
        context.dataStore.edit {
            it[PASSWORD_KEY] = pw
        }
    }

    suspend fun readPassword(): String? {
        return context.dataStore.data
            .map {
                it[PASSWORD_KEY] ?: "0000"
            }
            .first()
    }
}