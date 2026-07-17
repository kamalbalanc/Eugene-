package com.example.di

import com.example.di.viewmodel.*
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::HomeFeedViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::PredictionDetailViewModel)
    viewModelOf(::CreatePredictionViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LeaderboardViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::NotificationsViewModel)
}
