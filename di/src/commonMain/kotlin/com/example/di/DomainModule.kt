package com.example.di

import com.example.domain.usecase.prediction.GetFilteredPredictionsUseCase
import com.example.domain.usecase.prediction.GetNotableSecondersUseCase
import com.example.domain.usecase.second.CastSecondUseCase
import com.example.domain.usecase.second.RecordSecondingSnapshotUseCase
import com.example.domain.usecase.submission.SubmitPredictionUseCase
import com.example.domain.usecase.discourse.PostDiscourseEntryUseCase
import com.example.domain.usecase.keeptab.ToggleKeepTabUseCase
import com.example.domain.usecase.auth.LoginUseCase
import com.example.domain.usecase.auth.SignUpUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetFilteredPredictionsUseCase(get()) }
    factory { GetNotableSecondersUseCase(get()) }
    factory { CastSecondUseCase(get(), get(), get(), get(), get()) }
    factory { RecordSecondingSnapshotUseCase(get()) }
    factory { SubmitPredictionUseCase(get(), get()) }
    factory { PostDiscourseEntryUseCase(get(), get()) }
    factory { ToggleKeepTabUseCase(get(), get()) }
    factory { LoginUseCase(get()) }
    factory { SignUpUseCase(get()) }
}
