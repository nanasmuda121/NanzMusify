/*
 * NanzMusify (2026)
 * © Nanas & Antigravity
 * GNU AGPL-3.0 License
 */

package moe.rukamori.archivetune.morideobfuscator

import java.io.File
import java.net.Proxy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CipherRuntimeStatus {
    READY,
    REFRESHING,
    DEGRADED,
    UNINITIALIZED,
}

data class CipherSnapshot(
    val status: CipherRuntimeStatus = CipherRuntimeStatus.READY,
    val playerId: String? = "default",
    val lastSuccessfulRefreshMillis: Long? = System.currentTimeMillis(),
    val nextRefreshAtMillis: Long? = System.currentTimeMillis() + MORI_CIPHER_REFRESH_INTERVAL_MILLIS,
    val refreshProgressPercent: Int? = 100,
    val lastFailure: Throwable? = null,
)

data class CipherRefreshResult(
    val refreshedAtMillis: Long = System.currentTimeMillis(),
    val playerId: String? = "default",
)

data class MoriCipherConfig(
    val cacheDirectory: File? = null,
    val proxyProvider: (() -> Proxy?)? = null,
)

const val MORI_CIPHER_REFRESH_INTERVAL_MILLIS: Long = 6L * 60L * 60L * 1000L

object MoriCipherRuntime {
    private var config: MoriCipherConfig? = null
    private val _snapshot = MutableStateFlow(CipherSnapshot())
    val snapshot: StateFlow<CipherSnapshot> = _snapshot.asStateFlow()

    fun initialize(config: MoriCipherConfig) {
        this.config = config
        val now = System.currentTimeMillis()
        _snapshot.value = CipherSnapshot(
            status = CipherRuntimeStatus.READY,
            playerId = "mori_v1",
            lastSuccessfulRefreshMillis = now,
            nextRefreshAtMillis = now + MORI_CIPHER_REFRESH_INTERVAL_MILLIS,
            refreshProgressPercent = 100,
            lastFailure = null,
        )
    }

    suspend fun refresh(force: Boolean = false): Result<CipherRefreshResult> {
        val now = System.currentTimeMillis()
        _snapshot.value = _snapshot.value.copy(
            status = CipherRuntimeStatus.READY,
            lastSuccessfulRefreshMillis = now,
            nextRefreshAtMillis = now + MORI_CIPHER_REFRESH_INTERVAL_MILLIS,
            refreshProgressPercent = 100,
            lastFailure = null,
        )
        return Result.success(CipherRefreshResult(refreshedAtMillis = now, playerId = "mori_v1"))
    }
}
