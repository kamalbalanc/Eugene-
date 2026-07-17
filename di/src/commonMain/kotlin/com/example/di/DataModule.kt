package com.example.di

import com.example.data.local.PlatformSettings
import com.example.data.local.SessionStore
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.CommentRepositoryImpl
import com.example.data.repository.DiscourseRepositoryImpl
import com.example.data.repository.KeepTabRepositoryImpl
import com.example.data.repository.PreferencesRepositoryImpl
import com.example.data.repository.PredictionRepositoryImpl
import com.example.data.repository.SecondRepositoryImpl
import com.example.data.repository.SecondingSnapshotRepositoryImpl
import com.example.data.repository.SubmissionRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.CommentRepository
import com.example.domain.repository.DiscourseRepository
import com.example.domain.repository.KeepTabRepository
import com.example.domain.repository.PreferencesRepository
import com.example.domain.repository.PredictionRepository
import com.example.domain.repository.SecondRepository
import com.example.domain.repository.SecondingSnapshotRepository
import com.example.domain.repository.SubmissionRepository
import org.koin.dsl.module

val dataModule = module {
    single { PlatformSettings() }
    single { SessionStore(get()) }
    single<PredictionRepository> { PredictionRepositoryImpl(get(), get()) }
    single<SecondRepository> { SecondRepositoryImpl(get(), get()) }
    single<CommentRepository> { CommentRepositoryImpl(get(), get()) }
    single<DiscourseRepository> { DiscourseRepositoryImpl(get(), get()) }
    single<SecondingSnapshotRepository> { SecondingSnapshotRepositoryImpl(get(), get()) }
    single<SubmissionRepository> { SubmissionRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
    single<KeepTabRepository> { KeepTabRepositoryImpl(get(), get(), get()) }
}
