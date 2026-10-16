package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.data.local.ApiConfig
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor(private val config: ApiConfig) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val destino = config.atual
        val original = chain.request()
        val url = original.url.newBuilder()
            .scheme(destino.scheme)
            .host(destino.host)
            .port(destino.port)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
