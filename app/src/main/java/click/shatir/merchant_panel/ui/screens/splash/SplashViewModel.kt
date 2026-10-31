package click.shatir.merchant_panel.ui.screens.splash

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.util.Constants

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

sealed interface SplashState {
    data object Loading : SplashState
    data object GoToLogin : SplashState
    data object GoToDashboard : SplashState
    data class Error(val message: String) : SplashState
}

class SplashViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        checkAppState()
    }

    fun retry() {
        _state.value = SplashState.Loading
        checkAppState()
    }

    private fun checkAppState() {
        viewModelScope.launch {
            // 1) عرض اللوجو لمدة قصيرة
            delay(Constants.SPLASH_DELAY_MS.milliseconds)

            // 2) فحص الاتصال بالسيرفر
            val serverAvailable = checkConnection()
            if (!serverAvailable) {
                _state.value = SplashState.Error("تعذّر الاتصال بالسيرفر. تحقق من الإنترنت.")
                return@launch
            }

            // 3) فحص وجود التوكن
            val token = tokenManager.getToken()
            if (token.isNullOrBlank()) {
                _state.value = SplashState.GoToLogin
                return@launch
            }

            // 4) فحص صلاحية التوكن
            when (checkToken(token)) {
                TokenCheck.VALID -> _state.value = SplashState.GoToDashboard
                TokenCheck.INVALID -> {
                    tokenManager.clearToken()
                    _state.value = SplashState.GoToLogin
                }
                TokenCheck.ERROR -> {
                    // خطأ شبكة أثناء التحقق
                    _state.value = SplashState.Error("فشل التحقق من الجلسة. أعد المحاولة.")
                }
            }
        }
    }

    private suspend fun checkConnection(): Boolean {
        return try {

           val x= ApiClient.service.ping()
            Log.i("Ping","Try")
            true
        } catch (e: IOException) {
            android.util.Log.e("Ping", "Connection failed: ${e.javaClass.simpleName} - ${e.message}", e)
            Log.i("Ping","IOException")

            false
        } catch (e: HttpException) {
            // السيرفر ردّ — يعني متاح حتى لو الرد خطأ
            Log.i("Ping","HttpException")

            true
        } catch (e: Exception) {
            Log.i("Ping","Exception")

            false
        }
    }

    private suspend fun checkToken(token: String): TokenCheck {
        return try {
            ApiClient.service.validateToken("Bearer $token")
            TokenCheck.VALID
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) TokenCheck.INVALID else TokenCheck.ERROR
        } catch (e: IOException) {
            TokenCheck.ERROR
        } catch (e: Exception) {
            TokenCheck.ERROR
        }
    }

    private enum class TokenCheck { VALID, INVALID, ERROR }
}