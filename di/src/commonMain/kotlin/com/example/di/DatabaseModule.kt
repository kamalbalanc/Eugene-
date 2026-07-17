package com.example.di

import androidx.room.RoomDatabase
import com.example.data.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> { get<RoomDatabase.Builder<AppDatabase>>().build() }
    single { get<AppDatabase>().predictionDao() }
    single { get<AppDatabase>().secondDao() }
    single { get<AppDatabase>().commentDao() }
    single { get<AppDatabase>().discourseDao() }
    single { get<AppDatabase>().submissionDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().keepTabDao() }
    single { get<AppDatabase>().secondingSnapshotDao() }
    single { get<AppDatabase>().appealSubmissionDao() }
}
