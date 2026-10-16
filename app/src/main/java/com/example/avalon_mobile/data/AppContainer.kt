package com.example.avalon_mobile.data

import android.content.Context
import com.example.avalon_mobile.BuildConfig
import com.example.avalon_mobile.data.local.SecureSessionStore
import com.example.avalon_mobile.data.local.SessionManager
import com.example.avalon_mobile.data.remote.ApiCall
import com.example.avalon_mobile.data.remote.AuthInterceptor
import com.example.avalon_mobile.data.remote.AuthRefreshApi
import com.example.avalon_mobile.data.remote.AvalonApi
import com.example.avalon_mobile.data.remote.TokenAuthenticator
import com.example.avalon_mobile.data.remote.TokenRefresher
import com.example.avalon_mobile.data.repository.AuthRepository
import com.example.avalon_mobile.data.repository.AvalonRepository
import com.example.avalon_mobile.data.repository.MockAuthRepository
import com.example.avalon_mobile.data.repository.MockAvalonRepository
import com.example.avalon_mobile.data.repository.RemoteAuthRepository
import com.example.avalon_mobile.data.repository.RemoteAvalonRepository
import com.example.avalon_mobile.util.PdfFactory
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Injeção de dependências manual (escopo de aplicação). */
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val gson = Gson()

    val sessionManager = SessionManager(SecureSessionStore(context.applicationContext, gson))
    val pdfFactory = PdfFactory()

    private val logging = HttpLoggingInterceptor().apply {
        // Só cabeçalhos e sem o token: o corpo do login carrega a senha.
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
    }

    private val baseClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private fun retrofit(client: OkHttpClient) = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val refresher = TokenRefresher(sessionManager, retrofit(baseClient).create(AuthRefreshApi::class.java))

    private val api: AvalonApi = retrofit(
        baseClient.newBuilder()
            .addInterceptor(AuthInterceptor(sessionManager, refresher))
            .authenticator(TokenAuthenticator(sessionManager, refresher))
            .build()
    ).create(AvalonApi::class.java)

    private val apiCall = ApiCall(gson)

    val authRepository: AuthRepository =
        if (BuildConfig.USE_MOCK_AUTH) MockAuthRepository(sessionManager, appScope)
        else RemoteAuthRepository(api, apiCall, sessionManager, appScope)

    val avalonRepository: AvalonRepository =
        if (BuildConfig.USE_MOCK_API) MockAvalonRepository(pdfFactory) { sessionManager.current?.usuario }
        else RemoteAvalonRepository(api, apiCall, sessionManager)
}
