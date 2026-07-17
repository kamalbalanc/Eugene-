package com.example.di

import com.example.data.remote.BuildFlags
import com.example.data.remote.EugeneApiService
import com.example.data.remote.FakeEugeneApiService
import com.example.data.remote.FakeNetworkConfig
import com.example.data.remote.KtorEugeneApiService
import com.example.data.repository.NetworkMonitorImpl
import com.example.domain.repository.NetworkMonitor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

val networkModule = module {
    single { FakeNetworkConfig() }
    single {
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }
    single<EugeneApiService> {
        if (BuildFlags.USE_FAKE_BACKEND) {
            FakeEugeneApiService(get())
        } else {
            KtorEugeneApiService(get())
        }
    }
    single<NetworkMonitor> { NetworkMonitorImpl() }
}
