package com.example

import android.app.Application
import com.example.di.databaseModule
import com.example.di.networkModule
import com.example.di.dataModule
import com.example.di.domainModule
import com.example.di.viewModelModule
import com.example.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EugeneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EugeneApplication)
            modules(
                databaseModule,
                networkModule,
                dataModule,
                domainModule,
                viewModelModule,
                platformModule
            )
        }
    }
}
