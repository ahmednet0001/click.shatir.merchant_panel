package click.shatir.merchant_panel.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit {
            remove(KEY_TOKEN)
                .remove(KEY_STORE_ID)
                .remove(KEY_PACKAGE_TYPE)
        }
    }

    fun saveStoreId(id: Int) {
        prefs.edit {
            if (getStoreId() != id) {
                remove(KEY_PACKAGE_TYPE)
            }
            putInt(KEY_STORE_ID, id)
        }
    }

    fun getStoreId(): Int? {
        val id = prefs.getInt(KEY_STORE_ID, -1)
        return if (id == -1) null else id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectedStoreIdFlow(): Flow<Int?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_STORE_ID) trySend(getStoreId())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getStoreId())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun categoriesRevisionFlow(): Flow<Long> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CATEGORIES_REVISION) {
                trySend(prefs.getLong(KEY_CATEGORIES_REVISION, 0L))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getLong(KEY_CATEGORIES_REVISION, 0L))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun packageTypeFlow(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_PACKAGE_TYPE) trySend(getPackageType())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getPackageType())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun notifyCategoriesChanged() {
        val nextRevision = prefs.getLong(KEY_CATEGORIES_REVISION, 0L) + 1L
        prefs.edit { putLong(KEY_CATEGORIES_REVISION, nextRevision) }
    }

    fun clearStoreSelection() {
        prefs.edit {
            remove(KEY_STORE_ID)
                .remove(KEY_PACKAGE_TYPE)
        }
    }

    fun savePackageType(type: String) {
        prefs.edit { putString(KEY_PACKAGE_TYPE, type) }
    }

    fun getPackageType(): String? = prefs.getString(KEY_PACKAGE_TYPE, null)

    fun isSimplePackage(): Boolean =
        getPackageType()?.equals("simple", ignoreCase = true) == true

    fun hasAdvancedProductFeatures(): Boolean =
        getPackageType()?.trim()?.let { !it.equals("simple", ignoreCase = true) } ?: false

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_STORE_ID = "current_store_id"
        private const val KEY_PACKAGE_TYPE = "package_type"
        private const val KEY_CATEGORIES_REVISION = "categories_revision"
    }
}