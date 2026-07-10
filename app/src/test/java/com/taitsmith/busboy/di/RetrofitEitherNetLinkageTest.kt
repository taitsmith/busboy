package com.taitsmith.busboy.di

import com.slack.eithernet.ApiResult
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import com.taitsmith.busboy.api.ApiInterface
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitEitherNetLinkageTest {

    @Test
    fun eithernetFactoriesLinkAgainstRetrofit() {
        val client = OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.SECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl("http://127.0.0.1:1/")
            .addConverterFactory(ApiResultConverterFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .client(client)
            .build()
            .create(ApiInterface::class.java)

        val result = runBlocking { api.getStopPredictionList("1", null, "token") }

        result.shouldBeTypeOf<ApiResult.Failure.NetworkFailure>()
    }
}
