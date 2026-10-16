package com.example.avalon_mobile.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.avalon_mobile.model.UsuarioResumo
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Sessão autenticada. [expiresAtMillis] é derivado de `expires_in` no momento do login/refresh. */
data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
    val usuario: UsuarioResumo
)

interface SessionStore {
    fun read(): Session?
    fun write(session: Session): Boolean
    fun clear()
}

/**
 * Armazenamento seguro do token (6.2.3): os tokens são cifrados com AES-256-GCM usando uma
 * chave não exportável do Android Keystore antes de irem para o SharedPreferences.
 * O arquivo está excluído do backup (res/xml/backup_rules.xml).
 */
class SecureSessionStore(context: Context, private val gson: Gson) : SessionStore {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized
    override fun read(): Session? {
        val blob = prefs.getString(KEY_SESSION, null) ?: return null
        return runCatching { gson.fromJson(decrypt(blob), Session::class.java) }
            .getOrElse {
                // Chave invalidada (ex.: bloqueio de tela removido) ou dado corrompido: descarta.
                clear()
                null
            }
    }

    /**
     * Persiste a sessão. Se o Keystore do aparelho falhar (emuladores/ROMs com Keystore instável),
     * a sessão segue válida só em memória: o login não pode cair por causa da persistência.
     */
    @Synchronized
    override fun write(session: Session): Boolean = runCatching {
        prefs.edit().putString(KEY_SESSION, encrypt(gson.toJson(session))).apply()
    }.onFailure { Log.e(TAG, "Falha ao cifrar a sessão; mantida apenas em memória", it) }.isSuccess

    @Synchronized
    override fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(blob: String): String {
        val bytes = Base64.decode(blob, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
        return String(cipher.doFinal(bytes.copyOfRange(IV_SIZE, bytes.size)), Charsets.UTF_8)
    }

    private companion object {
        const val TAG = "SecureSessionStore"
        const val PREFS = "avalon_secure_session"
        const val KEY_SESSION = "session"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "avalon_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
    }
}
